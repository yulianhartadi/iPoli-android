package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.squareup.otto.Bus;

import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.ui.formatters.DueDateFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/16.
 */
public class RecurrencePickerFragment extends DialogFragment implements DatePickerFragment.OnDatePickedListener {

    private static final String TAG = "recurrence-picker-dialog";
    public static final int FREQUENCY_DAILY = 0;
    public static final int FREQUENCY_WEEKLY = 1;
    public static final int FREQUENCY_MONTHLY = 2;
    private static final String RECURRENCE = "recurrence";

    private static final Map<WeekDay, Integer> weekDayToCheckBoxId = new HashMap<WeekDay, Integer>() {{
        put(WeekDay.MO, R.id.monday);
        put(WeekDay.TU, R.id.tuesday);
        put(WeekDay.WE, R.id.wednesday);
        put(WeekDay.TH, R.id.thursday);
        put(WeekDay.FR, R.id.friday);
        put(WeekDay.SA, R.id.saturday);
        put(WeekDay.SU, R.id.sunday);
    }};

    public interface OnRecurrencePickedListener {
        void onRecurrencePicked(Recurrence recurrence);
    }

    @Inject
    Bus eventBus;

    @BindView(R.id.recurrence_frequency)
    Spinner recurrenceFrequency;

    @BindView(R.id.day_of_week_container)
    ViewGroup dayOfWeekContainer;

    @BindView(R.id.day_of_month_container)
    ViewGroup dayOfMonthContainer;

    @BindView(R.id.day_of_month)
    Spinner dayOfMonth;

    @BindView(R.id.recurrence_until)
    Button until;

    private Unbinder unbinder;
    private OnRecurrencePickedListener recurrencePickerListener;

    private Recurrence recurrence;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getActivity()).inject(this);
        Bundle args = getArguments();
        String recurrenceJson = args.getString(RECURRENCE);
        if (!TextUtils.isEmpty(recurrenceJson)) {
            recurrence = new Gson().fromJson(recurrenceJson, Recurrence.class);
        } else {
            recurrence = Recurrence.create();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.fragment_recurrence_picker, null);
        unbinder = ButterKnife.bind(this, view);

        List<String> daysOfMonth = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            daysOfMonth.add(String.valueOf(i));
        }
        dayOfMonth.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, daysOfMonth));

        recurrenceFrequency.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Daily", "Weekly", "Monthly"}));

        int selection = 0;
        switch (recurrence.getRecurrenceType()) {
            case DAILY:
                selection = 0;
                break;
            case WEEKLY:
                selection = 1;
                try {
                    Recur recur = new Recur(recurrence.getRrule());
                    for (Object obj : recur.getDayList()) {
                        WeekDay weekDay = (WeekDay) obj;
                        ((CheckBox) view.findViewById(weekDayToCheckBoxId.get(weekDay))).setChecked(true);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case MONTHLY:
                selection = 2;
                try {
                    Recur recur = new Recur(recurrence.getRrule());
                    int daySelected = (int) recur.getMonthDayList().get(0);
                    dayOfMonth.setSelection(daySelected - 1, false);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }

        recurrenceFrequency.setSelection(selection, false);

        builder.setView(view)
                .setIcon(R.drawable.logo)
                .setTitle("Pick repeating pattern")
                .setPositiveButton(getString(R.string.done), (dialog, which) -> {
                    recurrencePickerListener.onRecurrencePicked(recurrence);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setNeutralButton(getString(R.string.do_not_repeat), (dialog, which) -> {
                    recurrencePickerListener.onRecurrencePicked(null);
                });

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    @OnItemSelected(R.id.recurrence_frequency)
    public void onFrequencySelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case FREQUENCY_DAILY:
                dayOfWeekContainer.setVisibility(View.GONE);
                dayOfMonthContainer.setVisibility(View.GONE);
                break;
            case FREQUENCY_WEEKLY:
                dayOfWeekContainer.setVisibility(View.VISIBLE);
                dayOfMonthContainer.setVisibility(View.GONE);
                break;
            case FREQUENCY_MONTHLY:
                dayOfWeekContainer.setVisibility(View.GONE);
                dayOfMonthContainer.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnItemSelected(R.id.day_of_month)
    public void onDayOfMonthSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("DayOfMonthSel", position + " ");
    }

    @OnClick(R.id.recurrence_until)
    public void onUntilTapped() {
        DatePickerFragment.newInstance(this).show(getFragmentManager());
    }

    @Override
    public void onDatePicked(Date date) {
        String text = getString(R.string.end_of_time);
        if (date != null) {
            text = DateUtils.isToday(date) ? getString(R.string.today) : DueDateFormatter.format(date);
        }
        until.setText(text);
        until.setTag(date);
    }

    public static RecurrencePickerFragment newInstance(OnRecurrencePickedListener listener) {
        return newInstance(listener, null);
    }

    public static RecurrencePickerFragment newInstance(OnRecurrencePickedListener listener, Recurrence recurrence) {
        RecurrencePickerFragment fragment = new RecurrencePickerFragment();
        fragment.recurrencePickerListener = listener;
        if (recurrence != null) {
            Bundle args = new Bundle();
            args.putString(RECURRENCE, new Gson().toJson(recurrence));
            fragment.setArguments(args);
        }
        return fragment;
    }
}
