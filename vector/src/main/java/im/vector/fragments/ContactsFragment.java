package im.vector.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import org.matrix.androidsdk.data.Room;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import im.vector.R;
import im.vector.activity.VectorMemberDetailsActivity;
import im.vector.activity.VectorRoomActivity;
import im.vector.adapters.HomeRoomAdapter;
import im.vector.util.HomeRoomsViewModel;
import im.vector.util.PreferencesManager;
import im.vector.util.RoomUtils;
import im.vector.view.HomeSectionView;

public class ContactsFragment extends HomeFragment implements HomeRoomAdapter.OnSelectRoomListener {
    @BindView(R.id.invite_contacts)
    View mInvite;
    protected static final String LOG_TAG = ContactsFragment.class.getSimpleName();

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
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
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    void initViews() {
        super.initViews();
        mInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSession != null && mSession.getMyUser() != null) {
                    SettingsFragment.inviteContacts(ContactsFragment.this, mSession.getMyUser());
                }
            }
        });
    }

    @Override
    void refreshData(HomeRoomsViewModel.Result result) {
        final boolean pinMissedNotifications = PreferencesManager.pinMissedNotifications(getActivity());
        final boolean pinUnreadMessages = PreferencesManager.pinUnreadMessages(getActivity());
        final Comparator<Room> notificationComparator = RoomUtils.getNotifCountRoomsComparator(mSession, pinMissedNotifications, pinUnreadMessages);
        sortAndDisplay(result.getDirectChats(), notificationComparator, mDirectChatsSection);
        mActivity.hideWaitingView();
    }

    @Override
    protected List<HomeSectionView> initSections() {
        return Collections.singletonList(mDirectChatsSection);
    }

    @Override
    public void onSelectRoom(Room room, int position, String userId) {
        openDetails(room, userId);
    }

    @Override
    public void onLongClickRoom(View v, Room room, int position) {
    }

    private void openDetails(Room room, String userId) {
        Intent roomDetailsIntent = new Intent(getActivity(), VectorMemberDetailsActivity.class);
        roomDetailsIntent.putExtra(VectorMemberDetailsActivity.EXTRA_ROOM_ID, room.getRoomId());
        roomDetailsIntent.putExtra(VectorMemberDetailsActivity.EXTRA_MEMBER_ID, userId);
        roomDetailsIntent.putExtra(VectorMemberDetailsActivity.EXTRA_MATRIX_ID, mSession.getCredentials().userId);
        getActivity().startActivityForResult(roomDetailsIntent, VectorRoomActivity.GET_MENTION_REQUEST_CODE);
    }

    protected int getSectionRes() {
        return R.string.bottom_action_contacts;
    }

    protected int getTab() {
        return 2;
    }
}