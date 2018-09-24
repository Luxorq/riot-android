package im.vector.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import org.matrix.androidsdk.data.Room;

import java.util.Collections;
import java.util.List;

import im.vector.CallsCallback;
import im.vector.R;
import im.vector.util.DB;
import im.vector.util.HomeRoomsViewModel;
import im.vector.util.KedrCallHistory;
import im.vector.view.CallSectionView;
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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calls, container, false);
    }

    @Override
    void refreshData(HomeRoomsViewModel.Result result) {
        final CallSectionView sectionView = (CallSectionView) mDirectChatsSection;
        mActivity.hideWaitingView();
        DB.getCalls(new CallsCallback() {
            @Override
            public void onResult(final List<KedrCallHistory> calls) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sectionView.setCalls(calls);
                    }
                });
            }
        });
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