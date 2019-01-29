package im.vector.activity;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.ExpandableListView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.util.BingRulesManager;
import org.matrix.androidsdk.util.Log;

import java.util.List;
import java.util.Map;

import im.vector.Matrix;
import im.vector.R;
import im.vector.VectorApp;
import im.vector.adapters.ParticipantAdapterItem;
import im.vector.adapters.VectorParticipantsTransferAdapter;
import im.vector.util.DB;
import im.vector.util.HomeRoomsViewModel;
import im.vector.util.KedrRoom;
import im.vector.util.UtilsKt;
import im.vector.util.VectorUtils;

import static im.vector.util.UtilsKt.isHome;

public class VectorRoomTransferMembersActivity extends VectorAppCompatActivity {
    private static final String LOG_TAG = VectorRoomTransferMembersActivity.class.getSimpleName();

    private String mMatrixId;

    private ExpandableListView mListView;

    private VectorParticipantsTransferAdapter mAdapter;

    private final MXEventListener mEventsListener = new MXEventListener() {
        @Override
        public void onPresenceUpdate(final Event event, final User user) {
            runOnUiThread(() -> {
                Map<Integer, List<Integer>> visibleChildViews = VectorUtils.getVisibleChildViews(mListView, mAdapter);

                for (Integer groupPosition : visibleChildViews.keySet()) {
                    List<Integer> childPositions = visibleChildViews.get(groupPosition);

                    for (Integer childPosition : childPositions) {
                        Object item = mAdapter.getChild(groupPosition, childPosition);

                        if (item instanceof ParticipantAdapterItem) {
                            ParticipantAdapterItem participantAdapterItem = (ParticipantAdapterItem) item;

                            if (TextUtils.equals(user.user_id, participantAdapterItem.mUserId)) {
                                mAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
            });
        }


    };
    private MXSession mSession;
    private HomeRoomsViewModel roomsResult;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_vector_transfer_members;
    }

    @Override
    public void initUiAndData() {
        super.initUiAndData();

        if (CommonActivityUtils.shouldRestartApp(this)) {
            Log.e(LOG_TAG, "Restart the application.");
            CommonActivityUtils.restartApp(this);
            return;
        }

        if (CommonActivityUtils.isGoingToSplash(this)) {
            Log.d(LOG_TAG, "onCreate : Going to splash screen");
            return;
        }

        Intent intent = getIntent();

        if (intent.hasExtra(VectorRoomInviteMembersActivity.EXTRA_MATRIX_ID)) {
            mMatrixId = intent.getStringExtra(VectorRoomInviteMembersActivity.EXTRA_MATRIX_ID);
        }

        mSession = Matrix.getInstance(getApplicationContext()).getSession(mMatrixId);

        if ((null == mSession) || !mSession.isAlive()) {
            finish();
            return;
        }
        roomsResult = new HomeRoomsViewModel(mSession);
        setWaitingView(findViewById(R.id.search_in_progress_view));

        mListView = findViewById(R.id.room_details_members_list);
        mListView.setGroupIndicator(null);

        mAdapter = new VectorParticipantsTransferAdapter(this,
                R.layout.adapter_item_vector_add_participants,
                R.layout.adapter_item_vector_people_header,
                mSession);
        mListView.setAdapter(mAdapter);

        mListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Object item = mAdapter.getChild(groupPosition, childPosition);
            if (item instanceof Room) {
                transferToList((Room) item);
                return true;
            }
            return false;
        });
        DB.getRooms(this::refreshList);
    }

    private void refreshList(List<KedrRoom> list) {
        HomeRoomsViewModel.Result result = roomsResult.update();
        List<Room> chats = UtilsKt.filterRoomOut(result.getDirectChats(), list);
        runOnUiThread(() -> {
            mAdapter.refreshData(chats);
            mListView.expandGroup(0);
            hideWaitingView();
        });
    }

    private void transferToList(Room item) {
        DB.saveRoom(VectorApp.currentPin, item.getRoomId(), list -> {
            refreshList(list);
            runOnUiThread(() -> showMuteDialog(item));
        });
    }

    private void showMuteDialog(Room room) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.disable_notifications_title))
                .setMessage(getString(R.string.disable_notifications_message, VectorUtils.getRoomDisplayName(this, mSession, room)))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mSession.getDataHandler().getBingRulesManager().updateRoomNotificationState(room.getRoomId(),
                            BingRulesManager.RoomNotificationState.MUTE,
                            new BingRulesManager.onBingRuleUpdateListener() {
                                @Override
                                public void onBingRuleUpdateSuccess() {
                                }

                                @Override
                                public void onBingRuleUpdateFailure(final String errorMessage) {

                                }
                            });
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
                .create().show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSession.getDataHandler().addListener(mEventsListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSession.getDataHandler().removeListener(mEventsListener);
    }
}