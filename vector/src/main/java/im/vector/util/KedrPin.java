package im.vector.util;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class KedrPin extends RealmObject {
    @PrimaryKey
    private String pin;
    @LinkingObjects("roomPin")
    private final RealmResults<KedrRoom> rooms = null;

    public static KedrPin createPin(String pin) {
        KedrPin room = new KedrPin();
        room.pin = pin;
        return room;
    }

    public String getPin() {
        return pin;
    }

    public RealmList<KedrRoom> getRooms() {
        RealmList<KedrRoom> results = new RealmList<>();
        if (rooms != null) {
            results.addAll(rooms);
        }
        return results;
    }
}