package im.vector.activity;

import org.matrix.androidsdk.rest.callback.ApiFailureCallback;

public interface ApiDownloadCallback<T> extends ApiFailureCallback {
    void onSuccess(T var1, boolean exists);
}
