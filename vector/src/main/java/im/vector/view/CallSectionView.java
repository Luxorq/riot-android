package im.vector.view;

import android.content.Context;
import android.util.AttributeSet;

import org.matrix.androidsdk.data.Room;

import java.util.List;

import im.vector.adapters.AbsAdapter;
import im.vector.adapters.AbsFilterableAdapter;
import im.vector.adapters.CallsAdapter;
import im.vector.adapters.HomeRoomAdapter;
import im.vector.util.KedrCallHistory;

public class CallSectionView extends HomeSectionView {


    public CallSectionView(Context context) {
        super(context);
    }

    public CallSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CallSectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setRooms(List<Room> rooms) {
        //super.setRooms(rooms);
    }

    public void setCalls(List<KedrCallHistory> calls) {
        if (mAdapter != null) {
            CallsAdapter adapter = (CallsAdapter) mAdapter;
            adapter.setCalls(calls);
        }
    }

    @Override
    protected AbsFilterableAdapter getAdapter(int itemResId, HomeRoomAdapter.OnSelectRoomListener onSelectRoomListener, AbsAdapter.RoomInvitationListener invitationListener, AbsAdapter.MoreRoomActionListener moreActionListener, int tab) {
        return new CallsAdapter(getContext(), onSelectRoomListener);
    }
}
