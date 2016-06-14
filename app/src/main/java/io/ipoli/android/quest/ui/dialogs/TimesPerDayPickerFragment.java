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

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.TimesPerDayFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/14/16.
 */
public class TimesPerDayPickerFragment extends DialogFragment {
    private static final String TAG = "times-per-day-picker-dialog";
    private static final String TIMES_PER_DAY = "times_per_day";

    private static final int[] AVAILABLE_TIMES_PER_DAY = {2, 3, 4, 5, 6, 7};

    private int timesPerDay;
    private int selectedTimesPerDayIndex;

    private OnTimesPerDayPickedListener timesPerDayPickedListener;

    public interface OnTimesPerDayPickedListener {
        void onTimesPerDayPicked(int timesPerDay);
    }

    public static TimesPerDayPickerFragment newInstance(OnTimesPerDayPickedListener timesPerDayPickedListener) {
        return newInstance(AVAILABLE_TIMES_PER_DAY[0], timesPerDayPickedListener);
    }

    public static TimesPerDayPickerFragment newInstance(int timesPerDay, OnTimesPerDayPickedListener timesPerDayPickedListener) {
        TimesPerDayPickerFragment fragment = new TimesPerDayPickerFragment();
        Bundle args = new Bundle();
        args.putInt(TIMES_PER_DAY, timesPerDay);
        fragment.setArguments(args);
        fragment.timesPerDayPickedListener = timesPerDayPickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            timesPerDay = getArguments().getInt(TIMES_PER_DAY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<String> questTimesPerDay = new ArrayList<>();
        selectedTimesPerDayIndex = -1;
        for (int i = 0; i < AVAILABLE_TIMES_PER_DAY.length; i++) {
            int t = AVAILABLE_TIMES_PER_DAY[i];
            questTimesPerDay.add(TimesPerDayFormatter.formatReadable(t));
            if (t == timesPerDay) {
                selectedTimesPerDayIndex = i;
            }
        }

        if(selectedTimesPerDayIndex == -1) {
            selectedTimesPerDayIndex = 0;
            questTimesPerDay.add(0, DurationFormatter.formatReadable(timesPerDay));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
               .setTitle("How many times per day?")
               .setSingleChoiceItems(questTimesPerDay.toArray(new String[questTimesPerDay.size()]), selectedTimesPerDayIndex, (dialog, which) -> {
                   selectedTimesPerDayIndex = which;
               })
               .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                   int timesPerDay = new TimesPerDayMatcher().parse(questTimesPerDay.get(selectedTimesPerDayIndex));
                   timesPerDayPickedListener.onTimesPerDayPicked(timesPerDay);
               })
               .setNegativeButton(R.string.cancel, (dialog, which) -> {

               });
        return builder.create();

    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

}
