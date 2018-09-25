package im.vector.util;

import android.support.annotation.NonNull;

import org.matrix.androidsdk.call.IMXCall;

import java.util.List;

import im.vector.CallsCallback;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class DB {
    public static KedrCallHistory getCallByRoom(String roomId){
        return Realm.getDefaultInstance().where(KedrCallHistory.class).equalTo("roomId", roomId).findFirst();
    }

    static void addCall(final IMXCall call, final long startTime) {
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                realm.copyToRealm(KedrCallHistory.initWithCall(call, startTime));
            }
        });
    }

    public static void removeCall(final String callId, final CallsCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmResults<KedrCallHistory> rows = realm.where(KedrCallHistory.class).equalTo("callId", callId).findAll();
                rows.deleteAllFromRealm();
                if (callback != null){
                    callback.onResult(null);
                }
            }
        });
    }

    public static List<KedrCallHistory> getCalls() {
        Realm realm = Realm.getDefaultInstance();
        return realm.copyFromRealm(realm.where(KedrCallHistory.class).sort("date", Sort.DESCENDING).findAll());
    }

    public static void getCalls(final CallsCallback callback) {
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (callback != null) {
                    callback.onResult(realm.copyFromRealm(realm.where(KedrCallHistory.class).sort("date", Sort.DESCENDING).findAll()));
                }
            }
        });
    }
}
