package im.vector;

import java.util.List;

import im.vector.util.KedrCallHistory;

public interface CallsCallback {
    void onResult(List<KedrCallHistory> calls);
}
