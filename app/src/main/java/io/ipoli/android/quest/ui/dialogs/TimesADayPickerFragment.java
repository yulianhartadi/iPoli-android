package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.formatters.TimesADayFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/14/16.
 */
public class TimesADayPickerFragment extends DialogFragment {
    private static final String TAG = "times-a-day-picker-dialog";
    private static final String TIMES_A_DAY = "timesADay";

    private int timesADay;
    private int selectedTimesADayIndex;

    private OnTimesADayPickedListener timesADayPickedListener;

    public interface OnTimesADayPickedListener {
        void onTimesADayPicked(int timesADay);
    }

    public static TimesADayPickerFragment newInstance(OnTimesADayPickedListener timesADayPickedListener) {
        return newInstance(1, timesADayPickedListener);
    }

    public static TimesADayPickerFragment newInstance(int timesADay, OnTimesADayPickedListener timesADayPickedListener) {
        TimesADayPickerFragment fragment = new TimesADayPickerFragment();
        Bundle args = new Bundle();
        args.putInt(TIMES_A_DAY, timesADay);
        fragment.setArguments(args);
        fragment.timesADayPickedListener = timesADayPickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            timesADay = getArguments().getInt(TIMES_A_DAY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int maxTimesADay = Constants.MAX_TIMES_A_DAY_COUNT;

        int[] availableTimes = new int[maxTimesADay];
        String[] availableTimesReadable = new String[maxTimesADay];
        selectedTimesADayIndex = -1;
        for (int i = 1; i <= maxTimesADay; i++) {
            availableTimes[i - 1] = i;
            availableTimesReadable[i - 1] = TimesADayFormatter.formatReadable(i);
            if (i == timesADay) {
                selectedTimesADayIndex = i - 1;
            }
        }

        if (selectedTimesADayIndex == -1) {
            selectedTimesADayIndex = 0;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.quest_times_a_day_question)
                .setSingleChoiceItems(availableTimesReadable, selectedTimesADayIndex, (dialog, which) -> {
                    selectedTimesADayIndex = which;
                })
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> timesADayPickedListener.onTimesADayPicked(availableTimes[selectedTimesADayIndex]))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();

    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
