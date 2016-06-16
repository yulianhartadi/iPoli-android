package io.ipoli.android.quest.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.DateUtils;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, DialogInterface.OnClickListener {

    public static final String TAG = "date-picker-dialog";

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String DISABLE_PAST_DAY_SELECTION = "disable_past_day_selection";

    private OnDatePickedListener datePickedListener;

    public interface OnDatePickedListener {
        void onDatePicked(Date date);
    }

    public static DatePickerFragment newInstance(boolean disablePastDateSelection, OnDatePickedListener onDatePickedListener) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return newInstance(year, month, day, disablePastDateSelection, onDatePickedListener);
    }

    public static DatePickerFragment newInstance(OnDatePickedListener onDatePickedListener) {
        return newInstance(false, onDatePickedListener);
    }

    public static DatePickerFragment newInstance(int year, int month, int day, boolean disablePastDaySelection, OnDatePickedListener onDatePickedListener) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt(YEAR, year);
        args.putInt(MONTH, month);
        args.putInt(DAY, day);
        args.putBoolean(DISABLE_PAST_DAY_SELECTION, disablePastDaySelection);
        fragment.setArguments(args);
        fragment.datePickedListener = onDatePickedListener;
        return fragment;
    }

    public static DatePickerFragment newInstance(Date date, boolean disablePastDaySelection, OnDatePickedListener onDatePickedListener) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return newInstance(year, month, day, disablePastDaySelection, onDatePickedListener);
    }

    public static DatePickerFragment newInstance(Date date, OnDatePickedListener onDatePickedListener) {
        return newInstance(date, false, onDatePickedListener);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int year = args.getInt(YEAR);
        int month = args.getInt(MONTH);
        int day = args.getInt(DAY);
        boolean disablePastDaySelection = args.getBoolean(DISABLE_PAST_DAY_SELECTION);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.Theme_iPoli_AlertDialog, this, year, month, day);
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getString(R.string.unknown_choice), this);
        if (disablePastDaySelection) {
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        }
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        final Calendar c = Calendar.getInstance();
        c.setTime(DateUtils.getTodayAtMidnight().getTime());
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        datePickedListener.onDatePicked(c.getTime());
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        datePickedListener.onDatePicked(null);
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
