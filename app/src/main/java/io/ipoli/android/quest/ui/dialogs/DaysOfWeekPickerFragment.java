package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.ipoli.android.Constants;
import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class DaysOfWeekPickerFragment extends DialogFragment {

    private static final String TAG = "days-of-week-picker-dialog";
    private static final String SELECTED_DAYS = "selected_days";

    private OnDaysOfWeekPickedListener textPickedListener;

    private List<Integer> preSelectedDays;
    private AlertDialog alertDialog;

    public static DaysOfWeekPickerFragment newInstance(OnDaysOfWeekPickedListener listener) {
        return newInstance(new HashSet<>(), listener);
    }

    public static DaysOfWeekPickerFragment newInstance(Set<Integer> selectedDays, OnDaysOfWeekPickedListener listener) {
        DaysOfWeekPickerFragment fragment = new DaysOfWeekPickerFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList(SELECTED_DAYS, new ArrayList<>(selectedDays));
        fragment.setArguments(args);
        fragment.textPickedListener = listener;
        return fragment;
    }

    public interface OnDaysOfWeekPickedListener {
        void onDaysOfWeekPicked(Set<Integer> selectedDays);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preSelectedDays = getArguments().getIntegerArrayList(SELECTED_DAYS);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean[] checkedDays = new boolean[Constants.DAYS_OF_WEEK.length];
        for (int selectedDay : preSelectedDays) {
            if (selectedDay == DateTimeConstants.SUNDAY) {
                checkedDays[0] = true;
            } else {
                checkedDays[selectedDay] = true;
            }
        }
        List<String> daysOfWeek = sundayBeforeMondayDaysOfWeek();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setMultiChoiceItems(daysOfWeek.toArray(new String[daysOfWeek.size()]), checkedDays, null)
                .setTitle(R.string.challenge_days_question)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    SparseBooleanArray selectedPositions = alertDialog.getListView().getCheckedItemPositions();
                    Set<Integer> selectedDays = new LinkedHashSet<>();
                    for (int i = 0; i < alertDialog.getListView().getAdapter().getCount(); i++) {
                        if (selectedPositions.get(i)) {
                            if (i == 0) {
                                // turn sunday (index 0) to Joda Time Sunday (index 7)
                                selectedDays.add(DateTimeConstants.SUNDAY);
                            } else {
                                selectedDays.add(i);
                            }
                        }
                    }
                    textPickedListener.onDaysOfWeekPicked(selectedDays);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        alertDialog = builder.create();
        return alertDialog;
    }

    @NonNull
    private List<String> sundayBeforeMondayDaysOfWeek() {
        List<String> daysOfWeek = new ArrayList<>(Arrays.asList(Constants.DAYS_OF_WEEK));
        daysOfWeek.add(0, daysOfWeek.get(daysOfWeek.size() - 1));
        daysOfWeek.remove(daysOfWeek.size() - 1);
        return daysOfWeek;
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
