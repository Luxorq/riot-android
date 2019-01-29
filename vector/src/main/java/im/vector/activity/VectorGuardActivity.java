package im.vector.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import im.vector.KedrPinCallback;
import im.vector.R;
import im.vector.VectorApp;
import im.vector.util.DB;
import im.vector.util.KedrPin;
import im.vector.util.PreferencesManager;

import static im.vector.activity.util.RequestCodesKt.GUARD_REQUEST_CODE;
import static im.vector.util.UtilsKt.isHome;

public class VectorGuardActivity extends VectorAppCompatActivity {
    private static final String MODE_GUARD = "mode_guard";
    public static final String MODE_NEW = "mode_new";
    public static final String MODE_CHANGE = "mode_change";
    public static final String MODE_ASK = "mode_ask";
    private static final String MODE_SECRET = "mode_secret";
    public static boolean isLaunched;

    @BindView(R.id.auth_or_home)
    ImageView authOrHome;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.timer)
    TextView timer;

    @BindView(R.id.parent)
    ViewGroup parent;

    @BindView(R.id.blocked)
    ViewGroup parentBlocked;

    private int wrongPassCounter;

    String mode;
    String pass;

    private StringBuilder builder = new StringBuilder();
    private List<View> dotsList = new ArrayList<>();

    private CountDownTimer ctdTimer;
    private long timeLeft;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 1; i < 7; i++) {
            dotsList.add(parent.getChildAt(i));
        }

        int titleRes;
        mode = getIntent().getStringExtra(MODE_GUARD);
        if (mode.equalsIgnoreCase(MODE_NEW)) {
            titleRes = R.string.enter_new_secret;
        } else if (mode.equalsIgnoreCase(MODE_CHANGE)) {
            titleRes = R.string.enter_ask_secret;
        } else if (mode.equalsIgnoreCase(MODE_SECRET)) {
            titleRes = R.string.enter_ask_secret_tab;
        } else {
            titleRes = R.string.enter_pin_empty;
        }
        title.setText(titleRes);

        if (mode.equalsIgnoreCase(MODE_ASK)) {
            authOrHome.setVisibility(PreferencesManager.isTouchId(this) ? View.VISIBLE : View.INVISIBLE);
            authOrHome.setOnClickListener(v -> {
                boolean hwOk = Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered();
                String postfix = hwOk ? getString(R.string.available) : getString(R.string.not_available);
                Toast.makeText(VectorGuardActivity.this, getString(R.string.scanner_is, postfix), Toast.LENGTH_LONG).show();
            });
            if (!Reprint.isHardwarePresent() || !Reprint.hasFingerprintRegistered()) {
                Toast.makeText(this, getString(R.string.fingerprint_error_hw_not_available), Toast.LENGTH_LONG).show();
            }
        } else if (mode.equalsIgnoreCase(MODE_SECRET) && !TextUtils.isEmpty(VectorApp.currentPin)) {
            if (!isHome(this)) {
                authOrHome.setVisibility(View.VISIBLE);
                authOrHome.setImageResource(R.drawable.settings_default_list);
                authOrHome.setOnClickListener(v -> lockAndExit(PreferencesManager.getDefaultPin(this)));
            }
        }
        findViewById(R.id.del).setOnClickListener(v -> {
            builder.setLength(Math.max(0, builder.length() - 1));
            updateDotListSelectionState();
        });
    }

    private void updateDotListSelectionState() {
        for (View dotView : dotsList) {
            dotView.setSelected(dotsList.indexOf(dotView) < builder.length());
        }
    }

    @OnClick({R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.zero})
    void updateDots(TextView view) {
        builder.append(view.getText().toString());
        builder.setLength(Math.min(dotsList.size(), builder.length()));

        updateDotListSelectionState();

        if (builder.length() != dotsList.size()) {
            return;
        }

        String pinValue = builder.toString();

        switch (mode) {
            case MODE_NEW:
                repeatPassword(pinValue);
                break;
            case MODE_ASK:
                if (PreferencesManager.getDefaultPin(this).equals(pinValue)) {
                    lockAndExit(pinValue);
                    return;
                }
                DB.findKedrPin(pinValue, entity -> {
                    if (entity != null) {
                        lockAndExit(entity.getPin());
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                            wrongPassCounter++;
                            shake();
                            reset(dotsList);
                            if (wrongPassCounter >= 3) {
                                parentBlocked.setVisibility(View.VISIBLE);
                                parent.setVisibility(View.GONE);
                                timeLeft = 0;
                                checkTimer(0);
                                PreferencesManager.setGuardTime(VectorGuardActivity.this, timeLeft);
                            }
                        });
                    }
                });
                break;
            case MODE_CHANGE:
                repeatPassword(pinValue);
                break;
            case MODE_SECRET:
                if (PreferencesManager.getDefaultPin(this).equals(pinValue)) {
                    lockAndExit(pinValue);
                    return;
                }
                DB.findKedrPin(pinValue, entity -> {
                    if (entity == null) {
                        runOnUiThread(() -> repeatPassword(pinValue));
                    } else {
                        lockAndExit(pinValue);
                    }
                });
        }
    }

    private void repeatPassword(String pin) {
        if (TextUtils.isEmpty(pass)) {
            resetDots(dotsList);
            title.setText(R.string.enter_change_secret);
            pass = pin;
            builder.setLength(0);
            return;
        }
        if (!pin.equals(pass)) {
            Toast.makeText(this, getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
            reset(dotsList);
            return;
        }
        if (mode.equals(MODE_NEW)) {
            PreferencesManager.setDefaultPin(this, pin);
            lockAndExit(pin);
            return;
        }
        if (mode.equals(MODE_SECRET)) {
            DB.saveKedrPin(KedrPin.createPin(pin));
            lockAndExit(pin, RESULT_OK);
            return;
        }
        if (mode.equals(MODE_CHANGE)) {
            DB.findKedrPin(pin, entity -> runOnUiThread(() -> {
                if (entity != null || pin.equals(PreferencesManager.getDefaultPin(VectorGuardActivity.this))) {
                    Toast.makeText(this, getString(R.string.wrong_pin), Toast.LENGTH_LONG).show();
                    reset(dotsList);
                } else {
                    if (isHome(this)) {
                        PreferencesManager.setDefaultPin(this, pin);
                        lockAndExit(pin);
                    } else {
                        DB.saveKedrPin(KedrPin.createPin(pin));
                        lockAndExit(pin);
                    }
                }
            }));
        }
    }

    private void resetDots(List<View> dotsList) {
        for (View dotView : dotsList) {
            dotView.setSelected(false);
        }
    }

    private void lockAndExit(String pin, int resultCode) {
        VectorApp.currentPin = pin;
        VectorApp.locked = false;
        setResult(resultCode);
        finish();
        isLaunched = false;
    }

    private void lockAndExit(String pin) {
        lockAndExit(pin, RESULT_CANCELED);
    }

    private void reset(List<View> viewList) {
        title.setText(R.string.enter_ask_secret);
        resetDots(viewList);
        builder.setLength(0);
        pass = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onPause() {
        super.onPause();
        cancel();
    }


    private void start() {
        startTraditional();
        long savedTime = PreferencesManager.getGuardTime(this);
        if (savedTime == 0) {
            parent.setVisibility(View.VISIBLE);
            parentBlocked.setVisibility(View.GONE);
        } else {
            parentBlocked.setVisibility(View.VISIBLE);
            parent.setVisibility(View.GONE);
            checkTimer(savedTime);
        }
    }

    private void startTraditional() {
        if (mode.equals(MODE_ASK) && PreferencesManager.isTouchId(this)) {
            Reprint.authenticate(new AuthenticationListener() {
                @Override
                public void onSuccess(int moduleTag) {
                    lockAndExit(PreferencesManager.getDefaultPin(VectorGuardActivity.this));
                }

                @Override
                public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                                      CharSequence errorMessage, int moduleTag, int errorCode) {
                    Toast.makeText(VectorGuardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void cancel() {
        PreferencesManager.setGuardTime(this, timeLeft);
        Reprint.cancelAuthentication();
        if (ctdTimer != null) {
            ctdTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        if (mode.equals(MODE_CHANGE) || mode.equals(MODE_SECRET)) {
            super.onBackPressed();
        }
    }

    public static boolean checkGuardEnabled() {
        return VectorApp.locked;
    }

    @Override
    public int getLayoutRes() {
        String mode = getIntent().getStringExtra(MODE_GUARD);
        if (mode.equalsIgnoreCase(MODE_CHANGE) || mode.equalsIgnoreCase(MODE_SECRET)) {
            return R.layout.activity_guard_invert;
        }
        return R.layout.activity_guard;
    }

    public static void startWithMode(final Activity activity, final String mode) {
        isLaunched = true;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent guardIntent = new Intent(activity, VectorGuardActivity.class);
            guardIntent.putExtra(VectorGuardActivity.MODE_GUARD, mode);
            guardIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivityForResult(guardIntent, GUARD_REQUEST_CODE);
        }, 300);
    }

    public static void startSecretMode(final Activity activity) {
        isLaunched = true;
        Intent guardIntent = new Intent(activity, VectorGuardActivity.class);
        guardIntent.putExtra(VectorGuardActivity.MODE_GUARD, MODE_SECRET);
        activity.startActivityForResult(guardIntent, GUARD_REQUEST_CODE);
    }

    private void checkTimer(long savedTime) {
        long time = savedTime;
        if (time == 0) {
            time = 60000 - savedTime;
        }
        if (time <= 0) return;
        ctdTimer = new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                String time = String.format(Locale.getDefault(), "%02d : %02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                );
                timer.setText(time);
                timeLeft = millisUntilFinished;
            }

            public void onFinish() {
                wrongPassCounter = 0;
                timeLeft = 0;
                parent.setVisibility(View.VISIBLE);
                parentBlocked.setVisibility(View.GONE);
                PreferencesManager.setGuardTime(VectorGuardActivity.this, timeLeft);
            }

        }.start();
    }

    private void shake() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
        }
    }
}