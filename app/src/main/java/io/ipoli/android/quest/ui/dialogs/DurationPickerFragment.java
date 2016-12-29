package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.app.ui.formatters.DurationFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/14/16.
 */
public class DurationPickerFragment extends DialogFragment {
    private static final String TAG = "duration-picker-dialog";
    private static final String DURATION = "duration";

    private int duration;
    private int selectedDurationIndex;

    private OnDurationPickedListener durationPickedListener;

    public interface OnDurationPickedListener {
        void onDurationPicked(int duration);
    }

    public static DurationPickerFragment newInstance(OnDurationPickedListener durationPickedListener) {
        return newInstance(-1, durationPickedListener);
    }

    public static DurationPickerFragment newInstance(int duration, OnDurationPickedListener durationPickedListener) {
        DurationPickerFragment fragment = new DurationPickerFragment();
        Bundle args = new Bundle();
        args.putInt(DURATION, Math.max(duration, Constants.QUEST_MIN_DURATION));
        fragment.setArguments(args);
        fragment.durationPickedListener = durationPickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            duration = getArguments().getInt(DURATION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int[] availableDurations = Constants.DURATIONS;
        List<String> questDurations = new ArrayList<>();
        selectedDurationIndex = -1;
        for (int i = 0; i < availableDurations.length; i++) {
            int d = availableDurations[i];
            questDurations.add(DurationFormatter.formatReadable(d));
            if (d == duration) {
                selectedDurationIndex = i;
            }
        }

        if (selectedDurationIndex == -1) {
            selectedDurationIndex = 0;
            questDurations.add(0, DurationFormatter.formatReadable(duration));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.quest_duration_question)
                .setSingleChoiceItems(questDurations.toArray(new String[questDurations.size()]), selectedDurationIndex, (dialog, which) -> {
                    selectedDurationIndex = which;
                })
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    int duration = new DurationMatcher().parseShort(questDurations.get(selectedDurationIndex));
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
