package im.vector.util;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class KedrRoom extends RealmObject {
    @PrimaryKey
    private String roomId;
    private KedrPin roomPin;

    public static KedrRoom createRoom(String roomId, KedrPin pin) {
        KedrRoom room = new KedrRoom();
        room.roomId = roomId;
        room.roomPin = pin;
        return room;
    }

    public String getPin() {
        return roomPin.getPin();
    }

    public String getRoomId() {
        return roomId;
    }
}