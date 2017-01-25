package io.ipoli.android.app.ui.dialogs;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.Time;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener, DialogInterface.OnClickListener {

    public static final String TAG = "time-picker-dialog";
    private static final String MINUTES_AFTER_MIDNIGHT = "minutes_after_midnight";
    private static final String ENABLE_UNKNOWN_TIME_SELECTION = "enable_unknown_time_selection";

    private Time time;
    private boolean enableUnknownTimeSelection;
    private OnTimePickedListener timePickedListener;

    public interface OnTimePickedListener {
        void onTimePicked(Time time);
    }

    public static TimePickerFragment newInstance(OnTimePickedListener timePickedListener) {
        return newInstance(Time.now(), timePickedListener);
    }

    public static TimePickerFragment newInstance(Time time, OnTimePickedListener timePickedListener) {
        return newInstance(true, time, timePickedListener);
    }

    public static TimePickerFragment newInstance(boolean enableUnknownTimeSelection, OnTimePickedListener timePickedListener) {
        return newInstance(enableUnknownTimeSelection, null, timePickedListener);
    }

    public static TimePickerFragment newInstance(boolean enableUnknownTimeSelection, Time time, OnTimePickedListener timePickedListener) {
        TimePickerFragment fragment = new TimePickerFragment();
        if(time == null) {
            time = Time.now();
        }
        Bundle args = new Bundle();
        args.putInt(MINUTES_AFTER_MIDNIGHT, time.toMinuteOfDay());
        args.putBoolean(ENABLE_UNKNOWN_TIME_SELECTION, enableUnknownTimeSelection);
        fragment.setArguments(args);
        fragment.timePickedListener = timePickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            time = Time.of(getArguments().getInt(MINUTES_AFTER_MIDNIGHT));
            enableUnknownTimeSelection = getArguments().getBoolean(ENABLE_UNKNOWN_TIME_SELECTION);
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), R.style.Theme_iPoli_AlertDialog, this, time.getHours(), time.getMinutes(),
                DateFormat.is24HourFormat(getActivity()));
        if(enableUnknownTimeSelection) {
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getString(R.string.unknown_choice), this);
        }
        return dialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timePickedListener.onTimePicked(Time.at(hourOfDay, minute));
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        timePickedListener.onTimePicked(null);
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}