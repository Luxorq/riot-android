package im.vector;

import java.util.List;

import im.vector.util.KedrRoom;
import io.realm.RealmList;

public interface ChatListsCallback {
    void onResult(List<KedrRoom> entity);
}