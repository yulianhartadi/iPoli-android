package io.ipoli.android.quest.ui.dialogs;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.squareup.otto.Bus;

import java.util.Calendar;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.events.TimeSelectedEvent;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener, DialogInterface.OnClickListener {

    @Inject
    Bus eventBus;

    public TimePickerFragment() {
        App.getAppComponent(getActivity()).inject(this);
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
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        eventBus.post(new TimeSelectedEvent(c.getTime()));
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        eventBus.post(new TimeSelectedEvent(null));
    }
}
