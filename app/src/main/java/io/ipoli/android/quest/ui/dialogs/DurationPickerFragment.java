package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.DurationMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/14/16.
 */
public class DurationPickerFragment extends DialogFragment {
    private static final String TAG = "duration-picker-dialog";

    private int selectedDuration;

    private OnDurationPickedListener durationPickedListener;

    public interface OnDurationPickedListener {
        void onDurationPicked(int duration);
    }

    public static DurationPickerFragment newInstance(OnDurationPickedListener onDurationPickedListener) {
        DurationPickerFragment fragment = new DurationPickerFragment();
        fragment.durationPickedListener = onDurationPickedListener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] questDurations = getResources().getStringArray(R.array.quest_durations);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        selectedDuration = 0;
        builder.setIcon(R.drawable.logo)
               .setTitle("Pick duration")
               .setSingleChoiceItems(questDurations, selectedDuration, (dialog, which) -> {
                   selectedDuration = which;
               })
               .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                   int duration = new DurationMatcher().parseShort(questDurations[selectedDuration]);
                   durationPickedListener.onDurationPicked(duration);
               })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();

    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

}
