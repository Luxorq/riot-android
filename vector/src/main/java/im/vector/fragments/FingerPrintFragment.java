package im.vector.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import butterknife.OnClick;
import im.vector.R;

public class FingerPrintFragment extends Fragment {
    FloatingActionButton fab;

    TextView result;

    TextView hardwarePresent;

    TextView fingerprintsRegistered;

    private boolean running;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fingerprint, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClick();
            }
        });
        result = view.findViewById(R.id.result);
        hardwarePresent = view.findViewById(R.id.hardware_present);
        fingerprintsRegistered = view.findViewById(R.id.fingerprints_registered);
        hardwarePresent.setText(String.valueOf(Reprint.isHardwarePresent()));
        fingerprintsRegistered.setText(String.valueOf(Reprint.hasFingerprintRegistered()));
        running = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        cancel();
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        if (running) {
            cancel();
        } else {
            start();
        }
    }

    private void start() {
        running = true;
        result.setText("Listening");
        fab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.vector_close_widget));
        startTraditional();
    }

    private void startTraditional() {
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

    private void cancel() {
        result.setText("Cancelled");
        running = false;
        fab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.settings_authpassword));
        Reprint.cancelAuthentication();
    }

    private void showSuccess() {
        result.setText("Success");
        fab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.settings_authpassword));
        running = false;
    }

    private void showError(AuthenticationFailureReason failureReason, boolean fatal,
                           CharSequence errorMessage, int errorCode) {
        result.setText(errorMessage);

        if (fatal) {
            fab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.settings_authpassword));
            running = false;
        }
    }
}
