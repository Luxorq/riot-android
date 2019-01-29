package im.vector.util;

import org.matrix.androidsdk.call.IMXCall;

import java.util.List;

import im.vector.CallsCallback;
import im.vector.ChatListCallback;
import im.vector.ChatListsCallback;
import im.vector.KedrPinCallback;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class DB {
    public static KedrCallHistory getCallByRoom(String roomId) {
        return Realm.getDefaultInstance().where(KedrCallHistory.class).equalTo("roomId", roomId).findFirst();
    }

    static void addCall(final IMXCall call, final long startTime) {
        Realm.getDefaultInstance().executeTransactionAsync(realm -> realm.copyToRealm(KedrCallHistory.initWithCall(call, startTime)));
    }

    public static void removeCall(final String callId, final CallsCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(realm -> {
            RealmResults<KedrCallHistory> rows = realm.where(KedrCallHistory.class).equalTo("callId", callId).findAll();
            rows.deleteAllFromRealm();
            if (callback != null) {
                callback.onResult(null);
            }
        });
    }

    public static void getCalls(boolean isHome, final String pin, final CallsCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(realm -> {
            List<KedrCallHistory> calls = realm.copyFromRealm(realm.where(KedrCallHistory.class).sort("date", Sort.DESCENDING).findAll());
            KedrPin kedrPin = realm.where(KedrPin.class).equalTo("pin", pin).findFirst();
            if (kedrPin == null) {
                if (callback != null) {
                    callback.onResult(calls);
                }
                return;
            }
            kedrPin = realm.copyFromRealm(kedrPin);
            if (callback != null) {
                if (isHome) {
                    callback.onResult(UtilsKt.filterCallsIn(calls, kedrPin.getRooms()));
                } else {
                    callback.onResult(UtilsKt.filterCallsOut(calls, kedrPin.getRooms()));
                }
            }
        });
    }

    public static void getRoomsWithPin(boolean isHome, String pin, ChatListsCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(realm -> {
            if (callback != null) {
                RealmQuery<KedrRoom> query = realm.where(KedrRoom.class);
                if (isHome) {
                    query.notEqualTo("roomPin.pin", pin);
                } else {
                    query.equalTo("roomPin.pin", pin);
                }
                callback.onResult(realm.copyFromRealm(query.findAll()));
            }
        });
    }

    public static void getRooms(ChatListsCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(realm -> {
            if (callback != null) {
                callback.onResult(realm.copyFromRealm(realm.where(KedrRoom.class).findAll()));
            }
        });
    }

    public static void saveRoom(String pin, String roomId, ChatListsCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(realm -> {
            KedrRoom room = realm.where(KedrRoom.class).equalTo("roomId", roomId).findFirst();
            if (room == null) {
                room = KedrRoom.createRoom(roomId, KedrPin.createPin(pin));
            }
            realm.insertOrUpdate(room);
            if (callback != null) {
                callback.onResult(realm.copyFromRealm(realm.where(KedrRoom.class).findAll()));
            }
        });
    }

    public static void sendRoomToHome(String roomId, ChatListCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(realm -> {
            KedrRoom room = realm.where(KedrRoom.class).equalTo("roomId", roomId).findFirst();
            if (room != null) {
                room.deleteFromRealm();
            }
            if (callback != null) {
                callback.onResult(room);
            }
        });
    }


    public static void findKedrPin(String pin, KedrPinCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(realm -> {
            if (callback != null) {
                callback.onResult(realm.where(KedrPin.class).equalTo("pin", pin).findFirst());
            }
        });
    }

    public static void saveKedrPin(KedrPin entity) {
        if (entity == null || !entity.isValid()) return;
        Realm.getDefaultInstance().executeTransactionAsync(realm -> realm.insertOrUpdate(entity));
    }

    public static void saveRoom(KedrRoom entity, boolean async) {
        if (entity == null || !entity.isValid()) return;
        if (async) {
            Realm.getDefaultInstance().executeTransactionAsync(realm -> realm.insertOrUpdate(entity));
        } else {
            Realm.getDefaultInstance().insertOrUpdate(entity);
        }
    }
}