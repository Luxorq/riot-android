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

import butterknife.BindView;
import im.vector.Matrix;
import im.vector.R;

public class PasswordChangeActivity extends MXCActionBarActivity {
    @BindView(R.id.close)
    View mClose;
    @BindView(R.id.accept)
    View mAccept;
    private View mLoadingView;

    @BindView(R.id.old_password)
    AppCompatEditText mOldPassword;
    @BindView(R.id.new_password)
    AppCompatEditText mNewPassword;
    @BindView(R.id.confirm_password)
    AppCompatEditText mConfirmPassword;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_change_password;
    }

    @Override
    public int getTitleRes() {
        return R.string.settings_change_password;
    }

    @Override
    public void initUiAndData() {
        Intent intent = getIntent();
        mSession = getSession(intent);

        if (null == mSession) {
            mSession = Matrix.getInstance(PasswordChangeActivity.this).getDefaultSession();
        }

        if (mSession == null) {
            finish();
            return;
        }
        mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPasswordUpdateClick(v);
            }
        });
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAccept.setEnabled(false);
        mAccept.setAlpha(0.5f);

        mConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String oldPwd = mOldPassword.getText().toString().trim();
                String newPwd = mNewPassword.getText().toString().trim();
                String newConfirmPwd = mConfirmPassword.getText().toString().trim();
                mAccept.setEnabled((oldPwd.length() > 0) && (newPwd.length() > 0) && TextUtils.equals(newPwd, newConfirmPwd));
                mAccept.setAlpha(mAccept.isEnabled() ? 1f : 0.5f);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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


    private void onPasswordUpdateClick(View view) {
        String oldPwd = mOldPassword.getText().toString().trim();
        String newPwd = mNewPassword.getText().toString().trim();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

        displayLoadingView();

        mSession.updatePassword(oldPwd, newPwd, new ApiCallback<Void>() {
            private void onDone(final int textId) {
                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingView();
                            Toast.makeText(PasswordChangeActivity.this,
                                    getString(textId),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onSuccess(Void info) {
                onDone(R.string.settings_password_updated);
                if (!isFinishing()) {
                    finish();
                }
            }

            @Override
            public void onNetworkError(Exception e) {
                onDone(R.string.settings_fail_to_update_password);
            }

            @Override
            public void onMatrixError(MatrixError e) {
                onDone(R.string.settings_fail_to_update_password);
            }

            @Override
            public void onUnexpectedError(Exception e) {
                onDone(R.string.settings_fail_to_update_password);
            }
        });
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
}
