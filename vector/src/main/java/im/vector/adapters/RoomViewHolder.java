/*
 * Copyright 2017 Vector Creations Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.adapters.MessageRow;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.ReceiptData;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.util.Log;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.vector.R;
import im.vector.VectorApp;
import im.vector.util.KedrCallHistory;
import im.vector.util.RoomUtils;
import im.vector.util.VectorUtils;

import static im.vector.util.RoomUtils.millisecToTime;

public class RoomViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = RoomViewHolder.class.getSimpleName();

    @BindView(R.id.room_avatar)
    ImageView vRoomAvatar;

    @BindView(R.id.room_name)
    TextView vRoomName;

    @BindView(R.id.room_name_server)
    @Nullable
    TextView vRoomNameServer;

    @BindView(R.id.room_message)
    @Nullable
    TextView vRoomLastMessage;

    @BindView(R.id.room_update_date)
    @Nullable
    TextView vRoomTimestamp;

    @BindView(R.id.indicator_unread_message)
    @Nullable
    View vRoomUnreadIndicator;

    @BindView(R.id.room_unread_count)
    TextView vRoomUnreadCount;

    @BindView(R.id.direct_chat_indicator)
    @Nullable
    View mDirectChatIndicator;

    @BindView(R.id.room_avatar_encrypted_icon)
    View vRoomEncryptedIcon;

    @BindView(R.id.room_more_action_click_area)
    @Nullable
    View vRoomMoreActionClickArea;

    @BindView(R.id.room_more_action_anchor)
    @Nullable
    View vRoomMoreActionAnchor;

    @Nullable
    @BindView(R.id.img)
    AppCompatImageView vImg;


    public RoomViewHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    /**
     * Refresh the holder layout
     *
     * @param room                   the room
     * @param isDirectChat           true when the room is a direct chat one
     * @param isInvitation           true when the room is an invitation one
     * @param moreRoomActionListener
     */
    public void populateViews(final Context context, final MXSession session, final Room room,
                              final boolean isDirectChat, final boolean isInvitation,
                              final AbsAdapter.MoreRoomActionListener moreRoomActionListener) {
        // sanity check
        if (null == room) {
            Log.e(LOG_TAG, "## populateViews() : null room");
            return;
        }

        if (null == session) {
            Log.e(LOG_TAG, "## populateViews() : null session");
            return;
        }

        if (null == session.getDataHandler()) {
            Log.e(LOG_TAG, "## populateViews() : null dataHandler");
            return;
        }

        IMXStore store = session.getDataHandler().getStore(room.getRoomId());

        if (null == store) {
            Log.e(LOG_TAG, "## populateViews() : null Store");
            return;
        }

        final RoomSummary roomSummary = store.getSummary(room.getRoomId());

        if (null == roomSummary) {
            Log.e(LOG_TAG, "## populateViews() : null roomSummary");
            return;
        }

        int unreadMsgCount = roomSummary.getUnreadEventsCount();
        int highlightCount;
        int notificationCount;

        // Setup colors
        int mFuchsiaColor = ContextCompat.getColor(context, R.color.vector_fuchsia_color);
        int mGreenColor = ContextCompat.getColor(context, R.color.vector_green_color);
        int mSilverColor = ContextCompat.getColor(context, R.color.vector_silver_color);

        highlightCount = roomSummary.getHighlightCount();
        notificationCount = roomSummary.getNotificationCount();

        // fix a crash reported by GA
        if ((null != room.getDataHandler()) && room.getDataHandler().getBingRulesManager().isRoomMentionOnly(room.getRoomId())) {
            notificationCount = highlightCount;
        }

        int bingUnreadColor;
        if (isInvitation || (0 != highlightCount)) {
            bingUnreadColor = mFuchsiaColor;
        } else if (0 != notificationCount) {
            bingUnreadColor = mFuchsiaColor;
        } else if (0 != unreadMsgCount) {
            bingUnreadColor = mFuchsiaColor;
        } else {
            bingUnreadColor = Color.TRANSPARENT;
        }

        if (isInvitation || (notificationCount > 0)) {
            vRoomUnreadCount.setText(isInvitation ? "!" : RoomUtils.formatUnreadMessagesCounter(notificationCount));
            vRoomUnreadCount.setTypeface(null, Typeface.NORMAL);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(100);
            shape.setColor(bingUnreadColor);
            vRoomUnreadCount.setBackground(shape);
            vRoomUnreadCount.setVisibility(View.VISIBLE);
        } else {
            vRoomUnreadCount.setVisibility(View.GONE);
        }

        String roomName = VectorUtils.getRoomDisplayName(context, session, room);
        if (vRoomNameServer != null) {
            // This view holder is for the home page, we have up to two lines to display the name
            if (MXSession.isRoomAlias(roomName)) {
                // Room alias, split to display the server name on second line
                final String[] roomAliasSplitted = roomName.split(":");
                final String firstLine = roomAliasSplitted[0] + ":";
                final String secondLine = roomAliasSplitted[1];
                vRoomName.setLines(1);
                vRoomName.setText(firstLine);
                vRoomNameServer.setText(secondLine);
                vRoomNameServer.setVisibility(View.VISIBLE);
                vRoomNameServer.setTypeface(null, (0 != unreadMsgCount) ? Typeface.NORMAL : Typeface.NORMAL);
            } else {
                // Allow the name to take two lines
                vRoomName.setLines(2);
                vRoomNameServer.setVisibility(View.GONE);
                vRoomName.setText(roomName);
            }
        } else {
            vRoomName.setText(roomName);
        }
        vRoomName.setTypeface(null, (0 != unreadMsgCount) ? Typeface.NORMAL : Typeface.NORMAL);

        VectorUtils.loadRoomAvatar(context, session, vRoomAvatar, room);
        // get last message to be displayed
        if (vRoomLastMessage != null) {
            boolean hideMessage = PreferenceManager.getDefaultSharedPreferences(VectorApp.getInstance()).getBoolean("message_" + room.getRoomId(), false);
            CharSequence lastMsgToDisplay = RoomUtils.getRoomMessageToDisplay(context, session, roomSummary);
            if (!hideMessage || TextUtils.isEmpty(lastMsgToDisplay)) {
                vRoomLastMessage.setText(lastMsgToDisplay);
            } else {
                vRoomLastMessage.setText(context.getString(R.string.message));
            }
        }

        if (mDirectChatIndicator != null) {
            mDirectChatIndicator.setVisibility(isDirectChat ? View.VISIBLE : View.INVISIBLE);
        }
        vRoomEncryptedIcon.setVisibility(View.INVISIBLE);

        if (vRoomUnreadIndicator != null) {
            // set bing view background colour
            vRoomUnreadIndicator.setBackgroundColor(bingUnreadColor);
            vRoomUnreadIndicator.setVisibility(roomSummary.isInvited() ? View.INVISIBLE : View.INVISIBLE);
        }

        if (vRoomTimestamp != null) {
            vRoomTimestamp.setText(RoomUtils.getRoomTimestamp(context, roomSummary.getLatestReceivedEvent()));
        }

        if (vRoomMoreActionClickArea != null && vRoomMoreActionAnchor != null) {
            vRoomMoreActionClickArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != moreRoomActionListener) {
                        moreRoomActionListener.onMoreActionClick(vRoomMoreActionAnchor, room);
                    }
                }
            });
        }
    }

    private boolean isRead(MXSession session, Room room, RoomSummary summary, String userId) {
        if (TextUtils.isEmpty(userId)) {
            return false;
        }

        if (!session.isAlive()) {
            return false;
        }

        final String eventId = summary.getLatestReceivedEvent().eventId;

        IMXStore store = session.getDataHandler().getStore();
        List<ReceiptData> receipts = store.getEventReceipts(room.getRoomId(), eventId, true, true);

        if ((null == receipts) || receipts.isEmpty()) {
            return false;
        }
        return userId.equals(receipts.get(0).userId);
    }

    public void populateViews(final Context context, final MXSession session, final Room room,
                              final boolean isDirectChat, final boolean isInvitation) {
        // sanity check
        if (null == room) {
            Log.e(LOG_TAG, "## populateViews() : null room");
            return;
        }

        if (null == session) {
            Log.e(LOG_TAG, "## populateViews() : null session");
            return;
        }

        if (null == session.getDataHandler()) {
            Log.e(LOG_TAG, "## populateViews() : null dataHandler");
            return;
        }

        IMXStore store = session.getDataHandler().getStore(room.getRoomId());

        if (null == store) {
            Log.e(LOG_TAG, "## populateViews() : null Store");
            return;
        }

        final RoomSummary roomSummary = store.getSummary(room.getRoomId());

        if (null == roomSummary) {
            Log.e(LOG_TAG, "## populateViews() : null roomSummary");
            return;
        }

        int unreadMsgCount = roomSummary.getUnreadEventsCount();
        int highlightCount;
        int notificationCount;

        // Setup colors
        int mFuchsiaColor = ContextCompat.getColor(context, R.color.vector_fuchsia_color);
        int mGreenColor = ContextCompat.getColor(context, R.color.vector_green_color);
        int mSilverColor = ContextCompat.getColor(context, R.color.vector_silver_color);

        highlightCount = roomSummary.getHighlightCount();
        notificationCount = roomSummary.getNotificationCount();

        // fix a crash reported by GA
        if ((null != room.getDataHandler()) && room.getDataHandler().getBingRulesManager().isRoomMentionOnly(room.getRoomId())) {
            notificationCount = highlightCount;
        }

        int bingUnreadColor;
        if (isInvitation || (0 != highlightCount)) {
            bingUnreadColor = mFuchsiaColor;
        } else if (0 != notificationCount) {
            bingUnreadColor = mGreenColor;
        } else if (0 != unreadMsgCount) {
            bingUnreadColor = mSilverColor;
        } else {
            bingUnreadColor = Color.TRANSPARENT;
        }

        if (isInvitation || (notificationCount > 0)) {
            vRoomUnreadCount.setText(isInvitation ? "!" : RoomUtils.formatUnreadMessagesCounter(notificationCount));
            vRoomUnreadCount.setTypeface(null, Typeface.NORMAL);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(100);
            shape.setColor(bingUnreadColor);
            vRoomUnreadCount.setBackground(shape);
            vRoomUnreadCount.setVisibility(View.GONE);
        } else {
            vRoomUnreadCount.setVisibility(View.GONE);
        }

        String roomName = VectorUtils.getRoomDisplayName(context, session, room);
        if (vRoomNameServer != null) {
            // This view holder is for the home page, we have up to two lines to display the name
            if (MXSession.isRoomAlias(roomName)) {
                // Room alias, split to display the server name on second line
                final String[] roomAliasSplitted = roomName.split(":");
                final String firstLine = roomAliasSplitted[0] + ":";
                final String secondLine = roomAliasSplitted[1];
                vRoomName.setLines(1);
                vRoomName.setText(firstLine);
                vRoomNameServer.setText(secondLine);
                vRoomNameServer.setVisibility(View.VISIBLE);
                vRoomNameServer.setTypeface(null, (0 != unreadMsgCount) ? Typeface.NORMAL : Typeface.NORMAL);
            } else {
                // Allow the name to take two lines
                vRoomName.setLines(2);
                vRoomNameServer.setVisibility(View.GONE);
                vRoomName.setText(roomName);
            }
        } else {
            vRoomName.setText(roomName);
        }
        vRoomName.setTypeface(null, (0 != unreadMsgCount) ? Typeface.NORMAL : Typeface.NORMAL);

        VectorUtils.loadRoomAvatar(context, session, vRoomAvatar, room);

        String userId = "";
        ArrayList<RoomMember> members = (ArrayList<RoomMember>) room.getActiveMembers();
        for (RoomMember member : members) {
            if (!member.getUserId().equals(session.getMyUserId())) {
                userId = member.getUserId();
                itemView.setTag(userId);
                break;
            }
        }

        if (null != vRoomLastMessage) {
            final String finalUserId = userId;
            vRoomLastMessage.setText(VectorUtils.getUserOnlineStatus(context, session, userId, new SimpleApiCallback<Void>() {
                @Override
                public void onSuccess(Void info) {
                    vRoomLastMessage.setText(VectorUtils.getUserOnlineStatus(context, session, finalUserId, null, vImg));
                }
            }, vImg));
        }

        if (mDirectChatIndicator != null) {
            mDirectChatIndicator.setVisibility(View.INVISIBLE);
        }
        vRoomEncryptedIcon.setVisibility(View.INVISIBLE);

        if (vRoomUnreadIndicator != null) {
            vRoomUnreadIndicator.setVisibility(View.INVISIBLE);
        }

        if (vRoomTimestamp != null) {
            vRoomTimestamp.setVisibility(View.INVISIBLE);
            vRoomTimestamp.setText(RoomUtils.getRoomTimestamp(context, roomSummary.getLatestReceivedEvent()));
        }
    }

    public void populateViews(final Context context, final MXSession session, final Room room, KedrCallHistory call) {
        // sanity check
        if (null == room) {
            Log.e(LOG_TAG, "## populateViews() : null room");
            return;
        }

        if (null == session) {
            Log.e(LOG_TAG, "## populateViews() : null session");
            return;
        }

        if (null == session.getDataHandler()) {
            Log.e(LOG_TAG, "## populateViews() : null dataHandler");
            return;
        }

        IMXStore store = session.getDataHandler().getStore(room.getRoomId());

        if (null == store) {
            Log.e(LOG_TAG, "## populateViews() : null Store");
            return;
        }

        final RoomSummary roomSummary = store.getSummary(room.getRoomId());

        if (null == roomSummary) {
            Log.e(LOG_TAG, "## populateViews() : null roomSummary");
            return;
        }

        int mFuchsiaColor = ContextCompat.getColor(context, R.color.vector_fuchsia_color);
        int mGreenColor = ContextCompat.getColor(context, R.color.vector_green_color);
        int mSilverColor = ContextCompat.getColor(context, R.color.vector_silver_color);


        String roomName = VectorUtils.getRoomDisplayName(context, session, room);
        if (vRoomNameServer != null) {
            // This view holder is for the home page, we have up to two lines to display the name
            if (MXSession.isRoomAlias(roomName)) {
                // Room alias, split to display the server name on second line
                final String[] roomAliasSplitted = roomName.split(":");
                final String firstLine = roomAliasSplitted[0] + ":";
                final String secondLine = roomAliasSplitted[1];
                vRoomName.setLines(1);
                vRoomName.setText(firstLine);
                vRoomNameServer.setText(secondLine);
                vRoomNameServer.setVisibility(View.VISIBLE);
                vRoomNameServer.setTypeface(null, Typeface.NORMAL);
            } else {
                // Allow the name to take two lines
                vRoomName.setLines(2);
                vRoomNameServer.setVisibility(View.GONE);
                vRoomName.setText(roomName);
            }
        } else {
            vRoomName.setText(roomName);
        }
        vRoomName.setTypeface(null, Typeface.NORMAL);

        VectorUtils.loadRoomAvatar(context, session, vRoomAvatar, room);

        String userId = "";
        ArrayList<RoomMember> members = (ArrayList<RoomMember>) room.getActiveMembers();
        for (RoomMember member : members) {
            if (!member.getUserId().equals(session.getMyUserId())) {
                userId = member.getUserId();
                itemView.setTag(userId);
                break;
            }
        }
        vImg.setVisibility(View.VISIBLE);
        if (null != vRoomLastMessage) {
            switch (call.getType()) {
                case KedrCallHistory.TYPE_INVITE:

                    vImg.setImageResource(R.drawable.call_cancelled);
                    vRoomLastMessage.setText(R.string.call_invite);
                    break;
                case KedrCallHistory.TYPE_MISSED:
                    vImg.setImageResource(R.drawable.call_missed);
                    vRoomLastMessage.setText(R.string.call_missed);
                    break;
                case KedrCallHistory.TYPE_OUTBOUND:
                    vImg.setImageResource(R.drawable.call_outgoing);
                    vRoomLastMessage.setText(context.getString(R.string.call_outbound, millisecToTime(call.getDuration())));
                    break;
                case KedrCallHistory.TYPE_INBOUND:
                    vImg.setImageResource(R.drawable.call_incommig);
                    vRoomLastMessage.setText(context.getString(R.string.call_incoming, millisecToTime(call.getDuration())));
                    break;
                default:
                    break;
            }
        }

        if (mDirectChatIndicator != null) {
            mDirectChatIndicator.setVisibility(View.INVISIBLE);
        }
        vRoomEncryptedIcon.setVisibility(View.INVISIBLE);

        if (vRoomUnreadIndicator != null) {
            vRoomUnreadIndicator.setVisibility(View.INVISIBLE);
        }

        if (vRoomTimestamp != null) {
            vRoomTimestamp.setText(RoomUtils.getRoomTimestamp(context, call.getDate()));
        }
    }


}