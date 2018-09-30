package im.vector.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.matrix.androidsdk.rest.callback.ApiCallback;
import org.matrix.androidsdk.rest.model.MatrixError;
import org.matrix.androidsdk.util.Log;

import butterknife.BindView;
import im.vector.Matrix;
import im.vector.R;

import static im.vector.activity.VectorMemberDetailsActivity.EXTRA_ROOM_ID;

public class EditRoomActivity extends MXCActionBarActivity {
    private static final String LOG_TAG = "EditRoom";
    @BindView(R.id.close)
    View mClose;
    @BindView(R.id.accept)
    View mAccept;
    private View mLoadingView;

    @BindView(R.id.name)
    AppCompatEditText mName;
    @BindView(R.id.note)
    AppCompatEditText mNote;
    private int counter;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_edit_room;
    }

    @Override
    public int getTitleRes() {
        return R.string.edit_room;
    }

    @Override
    public void initUiAndData() {
        Intent intent = getIntent();
        mSession = getSession(intent);
        mRoom = mSession.getDataHandler().getRoom(intent.getStringExtra(EXTRA_ROOM_ID), false);
        if (null == mSession) {
            mSession = Matrix.getInstance(EditRoomActivity.this).getDefaultSession();
        }

        if (mSession == null) {
            finish();
            return;
        }
        if ((null == mRoom)) {
            finish();
            return;
        }
        mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditClick(v);
            }
        });
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mName.setText(mRoom.getState().name);
        mNote.setText(mRoom.getTopic());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void onEditClick(View view) {
        onRoomNameChanged();
        onRoomTopicChanged();
    }

    private void displayLoadingView() {
        if (null == mLoadingView) {
            mLoadingView = findViewById(R.id.vector_settings_spinner_views);
        }

        if (null != mLoadingView) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingView() {
        if (null != mLoadingView) {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    private void onRoomNameChanged() {
        // sanity check
        if ((null == mRoom) || (null == mSession)) {
            counter++;
            return;
        }

        // get new and previous values
        String previousName = mRoom.getState().name;
        String newName = mName.getText().toString();
        // update only, if values are different
        if (!TextUtils.equals(previousName, newName)) {
            displayLoadingView();

            Log.d(LOG_TAG, "##onRoomNamePreferenceChanged to " + newName);
            mRoom.updateName(newName, mUpdateCallback);
            return;
        }
        counter++;
    }

    private void onRoomTopicChanged() {
        // sanity check
        if (null == mRoom || null == mSession) {
            counter++;
            return;
        }

        // get new and previous values
        String previousTopic = mRoom.getTopic();
        String newTopic = mNote.getText().toString();
        // update only, if values are different
        if (!TextUtils.equals(previousTopic, newTopic)) {
            displayLoadingView();
            Log.d(LOG_TAG, "## update topic to " + newTopic);
            mRoom.updateTopic(newTopic, mUpdateCallback);
            return;
        }
        counter++;

    }

    private final ApiCallback<Void> mUpdateCallback = new ApiCallback<Void>() {

        private void onDone(final String message) {
            if (!isFinishing()) {
                counter++;
                if (counter > 1) {
                    finish();
                    return;
                }
                if (!TextUtils.isEmpty(message)) {
                    Toast.makeText(EditRoomActivity.this, message, Toast.LENGTH_LONG).show();
                }

                // ensure that the response has been sent in the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoadingView();
                    }
                });
            }
        }

        @Override
        public void onSuccess(Void info) {
            Log.d(LOG_TAG, "##update succeed");
            onDone(null);
        }

        @Override
        public void onNetworkError(Exception e) {
            Log.w(LOG_TAG, "##NetworkError " + e.getLocalizedMessage());
            onDone(e.getLocalizedMessage());
        }

        @Override
        public void onMatrixError(MatrixError e) {
            Log.w(LOG_TAG, "##MatrixError " + e.getLocalizedMessage());
            onDone(e.getLocalizedMessage());
        }

        @Override
        public void onUnexpectedError(Exception e) {
            Log.w(LOG_TAG, "##UnexpectedError " + e.getLocalizedMessage());
            onDone(e.getLocalizedMessage());
        }
    };
}
