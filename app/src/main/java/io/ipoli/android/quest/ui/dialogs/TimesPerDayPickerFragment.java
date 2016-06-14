package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/14/16.
 */
public class TimesPerDayPickerFragment extends DialogFragment {
    private static final String TAG = "times-per-day-picker-dialog";

    private int selectedTimesPerDay;

    private OnTimesPerDayPickedListener timesPerDayPickedListener;

    public interface OnTimesPerDayPickedListener {
        void onTimesPerDayPicked(int timesPerDay);
    }

    public static TimesPerDayPickerFragment newInstance(OnTimesPerDayPickedListener onTimesPerDayPickedListener) {
        TimesPerDayPickerFragment fragment = new TimesPerDayPickerFragment();
        fragment.timesPerDayPickedListener = onTimesPerDayPickedListener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] questTimesPerDay = getResources().getStringArray(R.array.quest_times_per_day);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        selectedTimesPerDay = 0;
        builder.setIcon(R.drawable.logo)
               .setTitle("How many times per day?")
               .setSingleChoiceItems(questTimesPerDay, selectedTimesPerDay, (dialog, which) -> {
                   selectedTimesPerDay = which;
               })
               .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                   int timesPerDay = new TimesPerDayMatcher().parse(questTimesPerDay[selectedTimesPerDay]);
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
