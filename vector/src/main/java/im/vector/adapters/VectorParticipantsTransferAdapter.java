package im.vector.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.util.Log;

import java.util.ArrayList;
import java.util.List;

import im.vector.Matrix;
import im.vector.R;
import im.vector.util.UtilsKt;
import im.vector.util.VectorUtils;

public class VectorParticipantsTransferAdapter extends BaseExpandableListAdapter {
    private static final String LOG_TAG = VectorParticipantsTransferAdapter.class.getSimpleName();

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    private final MXSession mSession;

    private final int mCellLayoutResourceId;
    private final int mHeaderLayoutResourceId;

    private final List<Room> mRooms = new ArrayList<>();

    public VectorParticipantsTransferAdapter(Context context,
                                             int cellLayoutResourceId,
                                             int headerLayoutResourceId,
                                             MXSession session) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mCellLayoutResourceId = cellLayoutResourceId;
        mHeaderLayoutResourceId = headerLayoutResourceId;
        mSession = session;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return getGroupTitle();
    }

    private String getGroupTitle() {
        return mContext.getString(R.string.main_list);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return getGroupTitle().hashCode();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mRooms.size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition >= 0 && childPosition < mRooms.size() && childPosition >= 0) {
            return mRooms.get(childPosition);
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Object item = getChild(groupPosition, childPosition);
        if (null != item) {
            return item.hashCode();
        }
        return 0L;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(mHeaderLayoutResourceId, null);
        }
        TextView sectionNameTxtView = convertView.findViewById(R.id.people_header_text_view);
        sectionNameTxtView.setText(getGroupTitle());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mCellLayoutResourceId, parent, false);
        }

        if (childPosition >= mRooms.size()) {
            Log.e(LOG_TAG, "## getChildView() : invalid child position");
            return convertView;
        }
        final Room room = mRooms.get(childPosition);
        final RoomMember member = UtilsKt.findDirectChatMember(room, mSession.getMyUserId());
        final ParticipantAdapterItem participant = new ParticipantAdapterItem(member);
        final ImageView thumbView = convertView.findViewById(R.id.filtered_list_avatar);
        final TextView nameTextView = convertView.findViewById(R.id.filtered_list_name);
        final TextView statusTextView = convertView.findViewById(R.id.filtered_list_status);
        final ImageView matrixUserBadge = convertView.findViewById(R.id.filtered_list_matrix_user);
        final View onlineView = convertView.findViewById(R.id.online_status);

        if ((null == thumbView) || (null == nameTextView) || (null == statusTextView) || (null == matrixUserBadge)) {
            Log.e(LOG_TAG, "## getChildView() : some ui items are null");
            return convertView;
        }
        participant.displayAvatar(mSession, thumbView);

        String roomName = VectorUtils.getRoomDisplayName(mContext, mSession, mRooms.get(childPosition));
        nameTextView.setText(roomName);
        participant.displayAvatar(mSession, thumbView);

        String status;
        User user = null;
        MXSession matchedSession = null;
        List<MXSession> sessions = Matrix.getMXSessions(mContext);

        for (MXSession session : sessions) {
            if (null == user) {
                matchedSession = session;
                user = session.getDataHandler().getUser(participant.mUserId);
            }
        }

        if (null != user) {
            status = VectorUtils.getUserOnlineStatus(mContext, matchedSession, participant.mUserId, new SimpleApiCallback<Void>() {
                @Override
                public void onSuccess(Void info) {
                }
            }, onlineView);
            statusTextView.setText(TextUtils.isEmpty(status) ? participant.mUserId : status);
        }
        convertView.findViewById(R.id.filtered_list_add_button).setVisibility(View.VISIBLE);
        return convertView;
    }

    public void refreshData(List<Room> rooms) {
        mRooms.clear();
        mRooms.addAll(rooms);
        notifyDataSetChanged();
    }
}