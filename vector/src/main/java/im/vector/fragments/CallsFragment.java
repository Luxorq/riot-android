package im.vector.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.matrix.androidsdk.call.IMXCall;
import org.matrix.androidsdk.crypto.MXCryptoError;
import org.matrix.androidsdk.crypto.data.MXDeviceInfo;
import org.matrix.androidsdk.crypto.data.MXUsersDevicesMap;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.callback.ApiCallback;
import org.matrix.androidsdk.rest.model.MatrixError;

import java.util.Collections;
import java.util.List;

import im.vector.R;
import im.vector.VectorApp;
import im.vector.activity.CommonActivityUtils;
import im.vector.activity.VectorCallViewActivity;
import im.vector.util.DB;
import im.vector.util.HomeRoomsViewModel;
import im.vector.util.KedrCallHistory;
import im.vector.util.PermissionsToolsKt;
import im.vector.view.CallSectionView;
import im.vector.view.HomeSectionView;

import static im.vector.util.UtilsKt.isHome;

public class CallsFragment extends HomeFragment {
    protected static final String LOG_TAG = CallsFragment.class.getSimpleName();
    private KedrCallHistory mCall;

    public static CallsFragment newInstance() {
        return new CallsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calls, container, false);
    }

    @Override
    void refreshData(HomeRoomsViewModel.Result result) {
        final CallSectionView sectionView = (CallSectionView) mDirectChatsSection;
        mActivity.hideWaitingView();
        DB.getCalls(isHome(requireActivity()), VectorApp.currentPin, calls -> mActivity.runOnUiThread(() -> sectionView.setCalls(calls)));
    }

    @Override
    protected List<HomeSectionView> initSections() {
        return Collections.singletonList(mDirectChatsSection);
    }

    @Override
    public void onSelectRoom(Room room, int position, String userId) {
        KedrCallHistory call = DB.getCallByRoom(room.getRoomId());
        if (call != null) {
            performItemAction(call);
        }
    }

    @Override
    public void onLongClickRoom(View v, Room room, final int position) {
        DB.removeCall((String) v.getTag(), calls -> mActivity.runOnUiThread(() -> onRoomResultUpdated(mActivity.getRoomsViewModel().getResult())));
    }

    protected int getSectionRes() {
        return R.string.bottom_action_calls;
    }

    protected int getTab() {
        return 1;
    }

    public void performItemAction(KedrCallHistory call) {
        if (!mSession.isAlive()) {
            Log.e(LOG_TAG, "performItemAction : the session is not anymore valid");
            return;
        }
        mCall = call;
        startCheckCallPermissions(call.isVideoCall());
    }

    private void startCheckCallPermissions(boolean aIsVideoCall) {
        final int requestCode;
        final int permissions;
        if (aIsVideoCall) {
            permissions = PermissionsToolsKt.PERMISSIONS_FOR_VIDEO_IP_CALL;
            requestCode = PermissionsToolsKt.PERMISSION_REQUEST_CODE_VIDEO_CALL;
        } else {
            permissions = PermissionsToolsKt.PERMISSIONS_FOR_AUDIO_IP_CALL;
            requestCode = PermissionsToolsKt.PERMISSION_REQUEST_CODE_AUDIO_CALL;
        }

        if (PermissionsToolsKt.checkPermissions(permissions, this, requestCode)) {
            startCall();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (0 == permissions.length) {
            org.matrix.androidsdk.util.Log.d(LOG_TAG, "## onRequestPermissionsResult(): cancelled " + requestCode);
        } else if (requestCode == PermissionsToolsKt.PERMISSION_REQUEST_CODE_AUDIO_CALL) {
            if (PermissionsToolsKt.onPermissionResultAudioIpCall(mActivity, grantResults)) {
                startCall();
            }
        } else if (requestCode == PermissionsToolsKt.PERMISSION_REQUEST_CODE_VIDEO_CALL) {
            if (PermissionsToolsKt.onPermissionResultVideoIpCall(mActivity, grantResults)) {
                startCall();
            }
        }
    }

    private void startCall() {
        if (!mSession.isAlive()) {
            org.matrix.androidsdk.util.Log.e(LOG_TAG, "startCall : the session is not anymore valid");
            return;
        }

        // create the call object
        mSession.mCallsManager.createCallInRoom(mCall.getRoomId(), mCall.isVideoCall(), new ApiCallback<IMXCall>() {
            @Override
            public void onSuccess(final IMXCall call) {
                mActivity.runOnUiThread(() -> {
                    final Intent intent = new Intent(getActivity(), VectorCallViewActivity.class);

                    intent.putExtra(VectorCallViewActivity.EXTRA_MATRIX_ID, mSession.getCredentials().userId);
                    intent.putExtra(VectorCallViewActivity.EXTRA_CALL_ID, call.getCallId());

                    mActivity.runOnUiThread(() -> startActivity(intent));
                });
            }

            @Override
            public void onNetworkError(Exception e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                org.matrix.androidsdk.util.Log.e(LOG_TAG, "## startCall() failed " + e.getMessage(), e);
            }

            @Override
            public void onMatrixError(MatrixError e) {
                if (e instanceof MXCryptoError) {
                    MXCryptoError cryptoError = (MXCryptoError) e;

                    if (MXCryptoError.UNKNOWN_DEVICES_CODE.equals(cryptoError.errcode)) {
                        CommonActivityUtils.displayUnknownDevicesDialog(mSession,
                                mActivity,
                                (MXUsersDevicesMap<MXDeviceInfo>) cryptoError.mExceptionData,
                                () -> startCall());

                        return;
                    }
                }

                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                org.matrix.androidsdk.util.Log.e(LOG_TAG, "## startCall() failed " + e.getMessage());
            }

            @Override
            public void onUnexpectedError(Exception e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                org.matrix.androidsdk.util.Log.e(LOG_TAG, "## startCall() failed " + e.getMessage(), e);
            }
        });
    }
}