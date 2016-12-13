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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.ipoli.android.Constants.DaysOfWeek;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.StringUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class DaysOfWeekPickerFragment extends DialogFragment {

    private static final String TAG = "days-of-week-picker-dialog";
    private static final String SELECTED_DAYS = "selected_days";
    private static final String TITLE = "title";

    private OnDaysOfWeekPickedListener textPickedListener;

    private List<Integer> preSelectedDays;
    private AlertDialog alertDialog;

    @StringRes
    private int title;

    public static DaysOfWeekPickerFragment newInstance(@StringRes int title, OnDaysOfWeekPickedListener listener) {
        return newInstance(title, new HashSet<>(), listener);
    }

    public static DaysOfWeekPickerFragment newInstance(@StringRes int title, Set<Integer> selectedDays, OnDaysOfWeekPickedListener listener) {
        DaysOfWeekPickerFragment fragment = new DaysOfWeekPickerFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList(SELECTED_DAYS, new ArrayList<>(selectedDays));
        args.putInt(TITLE, title);
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
        title = getArguments().getInt(TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean[] checkedDays = new boolean[DaysOfWeek.values().length];

        String[] daysOfWeek = new String[DaysOfWeek.values().length];

        for (int i = 0; i < DaysOfWeek.values().length; i++) {
            DaysOfWeek dayOfWeek = DaysOfWeek.values()[i];
            if (preSelectedDays.contains(dayOfWeek.getIsoOrder())) {
                checkedDays[i] = true;
            }
            daysOfWeek[i] = StringUtils.capitalize(dayOfWeek.name());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setMultiChoiceItems(daysOfWeek, checkedDays, null)
                .setTitle(title)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    SparseBooleanArray selectedPositions = alertDialog.getListView().getCheckedItemPositions();
                    Set<Integer> selectedDays = new HashSet<>();
                    for (int i = 0; i < alertDialog.getListView().getAdapter().getCount(); i++) {
                        if (selectedPositions.get(i)) {
                            selectedDays.add(DaysOfWeek.values()[i].getIsoOrder());
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
