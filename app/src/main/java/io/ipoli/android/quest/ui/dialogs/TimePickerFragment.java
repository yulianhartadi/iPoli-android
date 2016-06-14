package io.ipoli.android.quest.ui.dialogs;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.Time;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener, DialogInterface.OnClickListener {

    public static final String TAG = "time-picker-dialog";
    private OnTimePickedListener timePickedListener;

    public interface OnTimePickedListener {
        void onTimePicked(Time time);
    }

    public static TimePickerFragment newInstance(OnTimePickedListener timePickedListener) {
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.timePickedListener = timePickedListener;
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(getActivity(), R.style.Theme_iPoli_AlertDialog, this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getString(R.string.unknown_choice), this);
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