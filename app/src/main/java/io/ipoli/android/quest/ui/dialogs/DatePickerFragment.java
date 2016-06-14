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
    private OnDatePickedListener datePickedListener;

    public interface OnDatePickedListener {
        void onDatePicked(Date date);
    }

    public static DatePickerFragment newInstance(OnDatePickedListener onDatePickedListener) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return newInstance(year, month, day, onDatePickedListener);
    }

    public static DatePickerFragment newInstance(int year, int month, int day, OnDatePickedListener onDatePickedListener) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt(YEAR, year);
        args.putInt(MONTH, month);
        args.putInt(DAY, day);
        fragment.setArguments(args);
        fragment.datePickedListener = onDatePickedListener;
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int year = args.getInt(YEAR);
        int month = args.getInt(MONTH);
        int day = args.getInt(DAY);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.Theme_iPoli_AlertDialog, this, year, month, day);
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getString(R.string.unknown_choice), this);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
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
