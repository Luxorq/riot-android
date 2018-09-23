package im.vector.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.matrix.androidsdk.data.MyUser;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.listeners.MXMediaUploadListener;
import org.matrix.androidsdk.rest.callback.ApiCallback;
import org.matrix.androidsdk.rest.model.MatrixError;
import org.matrix.androidsdk.rest.model.bingrules.BingRule;
import org.matrix.androidsdk.util.BingRulesManager;
import org.matrix.androidsdk.util.Log;
import org.matrix.androidsdk.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import im.vector.R;
import im.vector.VectorApp;
import im.vector.activity.PasswordChangeActivity;
import im.vector.activity.VectorAppCompatActivity;
import im.vector.activity.VectorHomeActivity;
import im.vector.activity.VectorMediasPickerActivity;
import im.vector.util.PermissionsToolsKt;
import im.vector.util.VectorUtils;

import static org.matrix.androidsdk.rest.model.bingrules.BingRule.RULE_ID_DISABLE_ALL;

public class SettingsFragment extends AbsHomeFragment {
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();
    @BindView(R.id.avatar_img)
    ImageView mAvatarView;
    @BindView(R.id.action_bar_header_room_title)
    TextView mTitle;
    @BindView(R.id.invite_contacts)
    View mInvite;
    @BindView(R.id.change_password)
    TextView mChangePassword;
    @BindView(R.id.push_notifications)
    TextView mPushNotifications;
    @BindView(R.id.enable_push)
    SwitchCompat mSwitch;

    private final MXEventListener mEventsListener = new MXEventListener() {

    };
    private View mLoadingView;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        VectorHomeActivity activity = (VectorHomeActivity) getActivity();
        activity.mSearchView.setVisibility(View.GONE);
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPrimaryColor = ContextCompat.getColor(getActivity(), R.color.bg_grey);
        mSecondaryColor = ContextCompat.getColor(getActivity(), R.color.primary_color_dark);
        getVectorActivity().getSupportActionBar().setTitle(R.string.room_sliding_menu_settings);
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSession.getDataHandler().addListener(mEventsListener);
        initData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSession.getDataHandler().removeListener(mEventsListener);
    }

    @Override
    protected List<Room> getRooms() {
        return new ArrayList<>(mSession.getDataHandler().getStore().getRooms());
    }

    @Override
    protected void onFilter(String pattern, final OnFilterListener listener) {

    }

    @Override
    protected void onResetFilter() {

    }

    private void initViews() {
        final MyUser myUser = mSession.getMyUser();
        VectorUtils.loadUserAvatar(getActivity(), mSession, mAvatarView, myUser.getAvatarUrl(), myUser.user_id, myUser.displayname);
        mTitle.setText(myUser.displayname);
        mAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionsToolsKt.checkPermissions(PermissionsToolsKt.PERMISSION_CAMERA, getActivity(), PermissionsToolsKt.PERMISSION_REQUEST_CODE)) {
                    Intent intent = new Intent(getActivity(), VectorMediasPickerActivity.class);
                    intent.putExtra(VectorMediasPickerActivity.EXTRA_AVATAR_MODE, true);
                    startActivityForResult(intent, VectorUtils.TAKE_IMAGE);
                }
            }
        });
        mInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteContacts(SettingsFragment.this, myUser);
            }
        });
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(getActivity(), PasswordChangeActivity.class);
                startActivity(sendIntent);
            }
        });

        BingRule rule = mSession.getDataHandler().pushRules().findDefaultRule(RULE_ID_DISABLE_ALL);
        if (null != rule) {
            mSwitch.setChecked(!rule.isEnabled);
        }

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                BingRule rule = mSession.getDataHandler().pushRules().findDefaultRule(RULE_ID_DISABLE_ALL);
                if (null != rule) {
                    final CompoundButton.OnCheckedChangeListener listener = this;
                    displayLoadingView();
                    mSession.getDataHandler().getBingRulesManager().updateEnableRuleStatus(rule, !rule.isEnabled, new BingRulesManager.onBingRuleUpdateListener() {
                        private void onDone() {
                            hideLoadingView();
                        }

                        @Override
                        public void onBingRuleUpdateSuccess() {
                            onDone();
                        }

                        @Override
                        public void onBingRuleUpdateFailure(String errorMessage) {
                            onDone();
                            if (getActivity() != null) {
                                mSwitch.setOnCheckedChangeListener(null);
                                mSwitch.setChecked(!isChecked);
                                mSwitch.setOnCheckedChangeListener(listener);
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    private void initData() {
        if ((null == mSession) || (null == mSession.getDataHandler())) {
            Log.e(LOG_TAG, "## initData() : null session");
        }

        if (null == mSession.getDataHandler().getStore()) {
            Log.e(LOG_TAG, "## initData() : null store");
            return;
        }
        mActivity.hideWaitingView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case VectorUtils.TAKE_IMAGE:
                    Uri thumbnailUri = VectorUtils.getThumbnailUriFromIntent(getActivity(), data, mSession.getMediasCache());

                    if (null != thumbnailUri) {
                        displayLoadingView();

                        ResourceUtils.Resource resource = ResourceUtils.openResource(getActivity(), thumbnailUri, null);

                        if (null != resource) {
                            mSession.getMediasCache().uploadContent(resource.mContentStream, null, resource.mMimeType, null, new MXMediaUploadListener() {

                                @Override
                                public void onUploadError(final String uploadId, final int serverResponseCode, final String serverErrorMessage) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onCommonDone(serverResponseCode + " : " + serverErrorMessage);
                                        }
                                    });
                                }

                                @Override
                                public void onUploadComplete(final String uploadId, final String contentUri) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSession.getMyUser().updateAvatarUrl(contentUri, new ApiCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void info) {
                                                    onCommonDone(null);
                                                    refreshDisplay();
                                                }

                                                @Override
                                                public void onNetworkError(Exception e) {
                                                    onCommonDone(e.getLocalizedMessage());
                                                }

                                                @Override
                                                public void onMatrixError(final MatrixError e) {
                                                    if (MatrixError.M_CONSENT_NOT_GIVEN.equals(e.errcode)) {
                                                        if (null != getActivity()) {
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    hideLoadingView();
                                                                    ((VectorAppCompatActivity) getActivity()).getConsentNotGivenHelper().displayDialog(e);
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        onCommonDone(e.getLocalizedMessage());
                                                    }
                                                }

                                                @Override
                                                public void onUnexpectedError(Exception e) {
                                                    onCommonDone(e.getLocalizedMessage());
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }
                    break;
            }
        }
    }

    private void onCommonDone(final String errorMessage) {
        if (null != getActivity()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(errorMessage)) {
                        Toast.makeText(VectorApp.getInstance(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                    hideLoadingView();
                }
            });
        }
    }

    private void displayLoadingView() {
        // search the loading view from the upper view
        if (null == mLoadingView) {
            View parent = getView();

            while ((parent != null) && (mLoadingView == null)) {
                mLoadingView = parent.findViewById(R.id.vector_settings_spinner_views);
                parent = (View) parent.getParent();
            }
        }

        if (null != mLoadingView) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingView() {
        if (null != mLoadingView) {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    private void refreshDisplay() {
        refreshAvatar();
    }

    public void refreshAvatar() {
        if ((null != mAvatarView) && (null != mSession)) {
            MyUser myUser = mSession.getMyUser();
            VectorUtils.loadUserAvatar(getActivity(), mSession, mAvatarView, myUser.getAvatarUrl(), myUser.user_id, myUser.displayname);
        }
    }

    public static void inviteContacts(Fragment fragment, MyUser myUser){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, fragment.getString(R.string.invite_text, myUser.displayname));
        sendIntent.setType("text/plain");
        fragment.startActivity(sendIntent);
    }

    public static void inviteContacts(Activity activity, MyUser myUser){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.invite_text, myUser.displayname));
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }
}
