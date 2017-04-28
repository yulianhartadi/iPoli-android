package io.ipoli.android.app.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.format.TextStyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class DaysOfWeekPickerFragment extends DialogFragment {

    private static final String TAG = "days-of-week-picker-dialog";
    private static final String SELECTED_DAYS = "selected_days";
    private static final String TITLE = "title";

    private OnDaysOfWeekPickedListener textPickedListener;

    private List<DayOfWeek> preSelectedDays;
    private AlertDialog alertDialog;

    @StringRes
    private int title;

    public static DaysOfWeekPickerFragment newInstance(@StringRes int title, OnDaysOfWeekPickedListener listener) {
        return newInstance(title, new HashSet<>(), listener);
    }

    public static DaysOfWeekPickerFragment newInstance(@StringRes int title, Set<DayOfWeek> selectedDays, OnDaysOfWeekPickedListener listener) {
        DaysOfWeekPickerFragment fragment = new DaysOfWeekPickerFragment();
        Bundle args = new Bundle();
        ArrayList<Integer> days = new ArrayList<>();
        for (DayOfWeek day : selectedDays) {
            days.add(day.getValue());
        }
        args.putIntegerArrayList(SELECTED_DAYS, days);
        args.putInt(TITLE, title);
        fragment.setArguments(args);
        fragment.textPickedListener = listener;
        return fragment;
    }

    public interface OnDaysOfWeekPickedListener {
        void onDaysOfWeekPicked(Set<DayOfWeek> selectedDays);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<Integer> selectedDays = getArguments().getIntegerArrayList(SELECTED_DAYS);
        preSelectedDays = new ArrayList<>();
        for (int dayOfWeek : selectedDays) {
            preSelectedDays.add(DayOfWeek.of(dayOfWeek));
        }
        title = getArguments().getInt(TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean[] checkedDays = new boolean[DayOfWeek.values().length];

        String[] dayOfWeekNames = new String[DayOfWeek.values().length];

        for (int i = 0; i < DayOfWeek.values().length; i++) {
            DayOfWeek dayOfWeek = DayOfWeek.values()[i];
            if (preSelectedDays.contains(dayOfWeek)) {
                checkedDays[i] = true;
            }
            dayOfWeekNames[i] = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setMultiChoiceItems(dayOfWeekNames, checkedDays, null)
                .setTitle(title)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    SparseBooleanArray selectedPositions = alertDialog.getListView().getCheckedItemPositions();
                    Set<DayOfWeek> selectedDays = new HashSet<>();
                    for (int i = 0; i < alertDialog.getListView().getAdapter().getCount(); i++) {
                        if (selectedPositions.get(i)) {
                            selectedDays.add(DayOfWeek.values()[i]);
                        }
                    }
                    textPickedListener.onDaysOfWeekPicked(selectedDays);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        alertDialog = builder.create();
        return alertDialog;
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
