package im.vector.util;

import android.util.Log;

import com.google.gson.JsonObject;

import org.matrix.androidsdk.call.IMXCall;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.rest.model.RoomMember;

import javax.annotation.Nonnull;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class KedrCallHistory extends RealmObject {
    @Ignore
    public static final int TYPE_INVITE = 0;
    @Ignore
    public static final int TYPE_MISSED = 1;
    @Ignore
    public static final int TYPE_OUTBOUND = 2;
    @Ignore
    public static final int TYPE_INBOUND = 3;

    private String callId;
    private String roomId;
    private String userId;
    private long duration;
    private long date;
    private int type;
    private boolean videoCall;
    @Ignore
    private boolean isValid;

    static KedrCallHistory initWithCall(IMXCall call, long startTime) {
        KedrCallHistory item = new KedrCallHistory();
        item.callId = call.getCallId();
        Room room = call.getRoom();
        item.roomId = call.getRoom().getRoomId();
        for (RoomMember member : room.getMembers()) {
            if (!member.getUserId().equals(call.getSession().getMyUserId())) {
                item.userId = member.getUserId();
                break;
            }
        }
        long time = System.currentTimeMillis();
        item.date = startTime > 0 ? startTime : time;
        item.duration = time - item.date;
        if (call.isIncoming()) {
            item.type = item.duration <= 0 ? TYPE_MISSED : TYPE_INBOUND;
        } else {
            item.type = item.duration <= 0 ? TYPE_INVITE : TYPE_OUTBOUND;
        }
        item.videoCall = call.isVideo();
        Log.i("CallHistoryAdded", item.toString());
        return item;
    }

    @Nonnull
    public String getCallId() {
        return callId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUserId() {
        return userId;
    }

    public long getDuration() {
        return duration;
    }

    public long getDate() {
        return date;
    }

    public int getType() {
        return type;
    }

    public boolean isVideoCall() {
        return videoCall;
    }

    @Override
    public String toString() {
        return "KedrCallHistory{" +
                "callId='" + callId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", userId='" + userId + '\'' +
                ", duration=" + duration +
                ", date=" + date +
                ", type=" + type +
                ", videoCall=" + videoCall +
                ", isValid=" + isValid +
                '}';
    }
}
