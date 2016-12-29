package io.ipoli.android.app.rate;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.rate.events.RateDialogFeedbackDeclinedEvent;
import io.ipoli.android.app.rate.events.RateDialogFeedbackSentEvent;
import io.ipoli.android.app.rate.events.RateDialogLoveTappedEvent;
import io.ipoli.android.app.rate.events.RateDialogRateTappedEvent;
import io.ipoli.android.app.rate.events.RateDialogShownEvent;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/18/16.
 */
public class RateDialog extends DialogFragment {
    private static final String TAG = "rate-dialog";
    private static final String STATE = "state";

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    private int appRun;

    public enum State {
        INITIAL, RATE_APP, FEEDBACK;
    }

    private State state;

    public RateDialog() {
        App.getAppComponent(getContext()).inject(this);
    }

    public static RateDialog newInstance(State state) {
        RateDialog fragment = new RateDialog();
        Bundle args = new Bundle();
        args.putString(STATE, state.name());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(STATE)) {
            state = State.valueOf(getArguments().getString(STATE));
        } else {
            state = State.INITIAL;
        }

        appRun = localStorage.readInt(Constants.KEY_APP_RUN_COUNT);
        eventBus.post(new RateDialogShownEvent(appRun));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog;
        switch (state) {
            case RATE_APP:
                dialog = createRateAppDialog();
                break;
            case FEEDBACK:
                dialog = createFeedbackDialog();
                break;
            default:
                dialog = createInitialDialog();
        }
        return dialog;
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    private void showNewDialog(State state) {
        RateDialog d = RateDialog.newInstance(state);
        d.show(getActivity().getSupportFragmentManager());
    }

    private Dialog createInitialDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(getString(R.string.rate_dialog_initial_title))
                .setMessage(getString(R.string.rate_dialog_initial_message))
                .setPositiveButton(getString(R.string.dialog_yes), (dialog, which) -> {
                    eventBus.post(new RateDialogLoveTappedEvent(appRun, DialogAnswer.YES));
                    showNewDialog(State.RATE_APP);
                })
                .setNegativeButton(getString(R.string.dialog_no), (dialog, which) -> {
                    eventBus.post(new RateDialogLoveTappedEvent(appRun, DialogAnswer.NO));
                    showNewDialog(State.FEEDBACK);
                })
                .setNeutralButton(getString(R.string.rate_dialog_never_ask_again), (dialog, which) -> {
                    eventBus.post(new RateDialogLoveTappedEvent(appRun, DialogAnswer.NEVER_AGAIN));
                    saveNeverShowAgain();
                });

        return builder.create();
    }


    private Dialog createRateAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(getString(R.string.rate_dialog_rate_title))
                .setMessage(getString(R.string.rate_dialog_rate_message))
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                    eventBus.post(new RateDialogRateTappedEvent(appRun, DialogAnswer.YES));
                    saveNeverShowAgain();
                    rateApp();
                })
                .setNegativeButton(R.string.dialog_no, (dialog, which) -> {
                    eventBus.post(new RateDialogRateTappedEvent(appRun, DialogAnswer.NO));
                });

        return builder.create();
    }

    private Dialog createFeedbackDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.fragment_rate_dialog_feedback, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
                .setIcon(R.drawable.logo)
                .setTitle(getString(R.string.rate_dialog_feedback_title))
                .setPositiveButton(getString(R.string.rate_dialog_feedback_send), (dialog, which) -> {
                    EditText feedback = (EditText) v.findViewById(R.id.feedback);
                    String feedbackText = feedback.getText().toString();
                    if(!StringUtils.isEmpty(feedbackText)) {
                        eventBus.post(new RateDialogFeedbackSentEvent(feedbackText, appRun));
                        Toast.makeText(getContext(), R.string.rate_dialog_feedback_thanks, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.rate_dialog_feedback_no), (dialog, which) -> {
                    eventBus.post(new RateDialogFeedbackDeclinedEvent(appRun));
                });

        return builder.create();
    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent linkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(linkToMarket);
    }

    private void saveNeverShowAgain() {
        localStorage.saveBool(RateDialogConstants.KEY_SHOULD_SHOW_RATE_DIALOG, false);
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.SlideInDialogAnimation;
    }
}
