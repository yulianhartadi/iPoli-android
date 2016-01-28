package io.ipoli.android.quest.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.squareup.otto.Bus;

import java.util.Calendar;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.events.DateSelectedEvent;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Inject
    Bus eventBus;

    public DatePickerFragment() {
        App.getAppComponent(getActivity()).inject(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.Theme_iPoli_AlertDialog, this, year, month, day);
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
        eventBus.post(new DateSelectedEvent(c.getTime()));
    }
}
