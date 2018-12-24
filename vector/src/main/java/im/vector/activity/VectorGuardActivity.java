package im.vector.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import im.vector.R;
import im.vector.VectorApp;
import im.vector.util.PreferencesManager;

public class VectorGuardActivity extends VectorAppCompatActivity {
    private static String MODE_GUARD = "mode_guard";
    public static String MODE_NEW = "mode_new";
    public static String MODE_CHANGE = "mode_change";
    public static String MODE_ASK = "mode_ask";

    @BindView(R.id.first_digit)
    ImageView firstDot;
    @BindView(R.id.second_digit)
    ImageView secondDot;
    @BindView(R.id.third_digit)
    ImageView thirdDot;
    @BindView(R.id.fourth_digit)
    ImageView fourthDot;
    @BindView(R.id.fifth_digit)
    ImageView fifthDot;
    @BindView(R.id.six_digit)
    ImageView sixDot;

    @BindView(R.id.one)
    TextView one;
    @BindView(R.id.two)
    TextView two;
    @BindView(R.id.three)
    TextView three;
    @BindView(R.id.four)
    TextView four;
    @BindView(R.id.five)
    TextView five;
    @BindView(R.id.six)
    TextView six;
    @BindView(R.id.seven)
    TextView seven;
    @BindView(R.id.eight)
    TextView eight;
    @BindView(R.id.nine)
    TextView nine;
    @BindView(R.id.zero)
    TextView zero;

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

    private StringBuilder sb = new StringBuilder();
    private CountDownTimer ctdTimer;
    private long timeLeft;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int titleRes;
        mode = getIntent().getStringExtra(MODE_GUARD);
        if (mode.equalsIgnoreCase(MODE_NEW)) {
            titleRes = R.string.enter_new_secret;
        } else if (mode.equalsIgnoreCase(MODE_CHANGE)) {
            titleRes = R.string.enter_ask_secret;
        } else {
            titleRes = R.string.enter_ask_secret;
        }
        title.setText(titleRes);

        one.setOnClickListener(this::updateDots);
        two.setOnClickListener(this::updateDots);
        three.setOnClickListener(this::updateDots);
        four.setOnClickListener(this::updateDots);
        five.setOnClickListener(this::updateDots);
        six.setOnClickListener(this::updateDots);
        seven.setOnClickListener(this::updateDots);
        eight.setOnClickListener(this::updateDots);
        nine.setOnClickListener(this::updateDots);
        zero.setOnClickListener(this::updateDots);

        ImageView auth = findViewById(R.id.auth);
        if (mode.equalsIgnoreCase(MODE_ASK)) {
            auth.setVisibility(PreferencesManager.isTouchId(this) ? View.VISIBLE : View.INVISIBLE);
            auth.setOnClickListener(v -> {
                boolean isOk = Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered();
                String postfix = isOk ? getString(R.string.available) : getString(R.string.not_available);
                Toast.makeText(VectorGuardActivity.this, getString(R.string.scanner_is, postfix), Toast.LENGTH_LONG).show();
            });
            if (!Reprint.isHardwarePresent() || !Reprint.hasFingerprintRegistered()) {
                Toast.makeText(this, getString(R.string.fingerprint_error_hw_not_available), Toast.LENGTH_LONG).show();
            }
        }
        ImageView del = findViewById(R.id.del);
        del.setOnClickListener(v -> {
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
            updateDots(v);
        });
    }

    private void updateDots(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            sb.append(textView.getText().toString());
        }
        View[] arr = {firstDot, secondDot, thirdDot, fourthDot, fifthDot, sixDot};
        for (View anArr : arr) {
            anArr.setSelected(false);
        }
        sb.setLength(Math.min(6, sb.length()));
        for (int i = 0; i < sb.length(); i++) {
            arr[i].setSelected(true);
        }
        if (sb.length() == 6) {
            if (mode.equals(MODE_NEW)) {
                saveAndExt();
            } else if (mode.equals(MODE_ASK)) {
                if (sb.toString().equalsIgnoreCase(PreferencesManager.getGuardPass(this))) {
                    saveAndExt();
                } else {
                    wrongPassCounter++;
                    shake();
                    reset(arr);
                    if (wrongPassCounter >= 3) {
                        timeLeft = 0;
                        PreferencesManager.setGuardTime(this, timeLeft);
                        parentBlocked.setVisibility(View.VISIBLE);
                        parent.setVisibility(View.GONE);
                        checkTimer(0);
                    }
                }
            } else if (mode.equals(MODE_CHANGE)) {
                if (TextUtils.isEmpty(pass)) {
                    pass = sb.toString();
                    title.setText(R.string.enter_change_secret);
                    for (View anArr : arr) {
                        anArr.setSelected(false);
                    }
                    sb.setLength(0);
                } else {
                    if (sb.toString().equals(pass)) {
                        saveAndExt();
                    } else {
                        reset(arr);
                    }
                }
            }
        }
    }

    private void saveAndExt() {
        VectorApp.locked = false;
        PreferencesManager.setGuardPass(this, sb.toString());
        finish();
    }

    private void reset(View[] arr) {
        Toast.makeText(this, getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
        title.setText(R.string.enter_ask_secret);
        for (View anArr : arr) {
            anArr.setSelected(false);
        }
        sb.setLength(0);
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
                    showSuccess();
                }

                @Override
                public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                                      CharSequence errorMessage, int moduleTag, int errorCode) {
                    showError(failureReason, fatal, errorMessage, errorCode);
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

    private void showSuccess() {
        VectorApp.locked = false;
        finish();
    }

    private void showError(AuthenticationFailureReason failureReason, boolean fatal,
                           CharSequence errorMessage, int errorCode) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {

    }

    public static boolean checkGuardEnabled() {
        return VectorApp.locked;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_guard;
    }

    public static void startWithMode(Activity activity, String mode) {
        new Handler().postDelayed(() -> {
            Intent guardIntent = new Intent(activity, VectorGuardActivity.class);
            guardIntent.putExtra(VectorGuardActivity.MODE_GUARD, mode);
            guardIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(guardIntent);
        }, 300);
    }

    private void checkTimer(long savedTime) {
        long time = savedTime;
        if (time == 0) {
            time = 60000 - savedTime;
        }
        if (time <= 0) return;
        ctdTimer = new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                String time = String.format("%02d : %02d",
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
                PreferencesManager.setGuardTime(VectorGuardActivity.this, timeLeft);
                parent.setVisibility(View.VISIBLE);
                parentBlocked.setVisibility(View.GONE);
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