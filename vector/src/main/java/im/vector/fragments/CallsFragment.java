package im.vector.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import org.matrix.androidsdk.data.Room;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.vector.R;
import im.vector.util.HomeRoomsViewModel;
import im.vector.util.PreferencesManager;
import im.vector.util.RoomUtils;
import im.vector.view.HomeSectionView;

public class CallsFragment extends HomeFragment {
    protected static final String LOG_TAG = CallsFragment.class.getSimpleName();

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
        //openDetails(room, userId);
    }

    @Override
    public void onLongClickRoom(View v, Room room, int position) {
    }

    protected int getSectionRes() {
        return R.string.bottom_action_calls;
    }

    protected int getTab() {
        return 1;
    }
}