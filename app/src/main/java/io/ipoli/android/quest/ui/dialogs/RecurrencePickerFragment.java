package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/16.
 */
public class RecurrencePickerFragment extends DialogFragment {

    private static final String TAG = "recurrence-picker-dialog";

    @Inject
    Bus eventBus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getActivity()).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflater.inflate(R.layout.fragment_recurrence_picker, null))
                .setIcon(R.drawable.logo)
                .setTitle("Pick repeating pattern")
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {

                })
                .setNeutralButton(getString(R.string.help_dialog_more_help), (dialog, which) -> {
                });
        return builder.create();

    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

}
