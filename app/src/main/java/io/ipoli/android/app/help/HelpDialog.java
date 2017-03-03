package io.ipoli.android.app.help;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.help.events.HelpDialogShownEvent;
import io.ipoli.android.app.help.events.MoreHelpTappedEvent;
import io.ipoli.android.app.utils.EmailUtils;
import io.ipoli.android.app.utils.LocalStorage;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/26/16.
 */
public class HelpDialog extends DialogFragment {
    private static final String TAG = "help-dialog";
    private static final String SCREEN = "screen";
    private static final String LAYOUT_RES = "layout_res";
    private static final String TITLE = "title";

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    private int appRun;

    @LayoutRes
    private int layout;

    @StringRes
    private int title;

    private String screen;

    public HelpDialog() {
        App.getAppComponent(getContext()).inject(this);
    }

    public static HelpDialog newInstance(@LayoutRes int layout, @StringRes int title, String screen) {
        HelpDialog fragment = new HelpDialog();
        Bundle args = new Bundle();
        args.putInt(LAYOUT_RES, layout);
        args.putInt(TITLE, title);
        args.putString(SCREEN, screen);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() == null) {
            dismiss();
        }

        layout = getArguments().getInt(LAYOUT_RES);
        title = getArguments().getInt(TITLE);
        screen = getArguments().getString(SCREEN);

        appRun = localStorage.readInt(Constants.KEY_APP_RUN_COUNT);
        eventBus.post(new HelpDialogShownEvent(screen, appRun));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createDialog(layout, title);
    }
    
    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    private Dialog createDialog(@LayoutRes int layout, @StringRes int title) {
        return createDialog(layout, getString(title));
    }

    private Dialog createDialog(@LayoutRes int layout, String title) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
                .setIcon(R.drawable.logo)
                .setTitle(title)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {

                })
                .setNeutralButton(getString(R.string.help_dialog_more_help), (dialog, which) -> {
                    eventBus.post(new MoreHelpTappedEvent(screen, appRun));
                    EmailUtils.send(getContext(), getString(R.string.help_wanted_email_subject), localStorage.readString(Constants.KEY_PLAYER_ID), getString(R.string.help_wanted_chooser_title));
                });
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.SlideInDialogAnimation;
    }
}
