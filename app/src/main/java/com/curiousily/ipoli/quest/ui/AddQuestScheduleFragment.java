package com.curiousily.ipoli.quest.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.AddQuestActivity;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.Recurrence;
import com.curiousily.ipoli.quest.events.QuestBuiltEvent;
import com.curiousily.ipoli.ui.dialogs.DatePickerFragment;
import com.curiousily.ipoli.ui.dialogs.TimePickerFragment;
import com.curiousily.ipoli.ui.events.DateSelectedEvent;
import com.curiousily.ipoli.ui.events.TimeSelectedEvent;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/12/15.
 */
public class AddQuestScheduleFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.add_quest_duration)
    Spinner duration;

    @Bind(R.id.add_quest_times_per_day)
    SeekBar timesPerDay;

    @Bind(R.id.add_quest_times_per_day_label)
    TextView timesPerDayLabel;

    @Bind(R.id.add_quest_start_time)
    Button startTime;

    @Bind(R.id.add_quest_due_date)
    Button dueDate;

    @Bind(R.id.add_quest_recurrence_interval)
    Spinner recurrenceInterval;

    @Bind(R.id.add_quest_recurrence_frequency)
    Spinner recurrenceFrequency;

    @Bind(R.id.add_quest_included_days_layout)
    ViewGroup includedDaysLayout;

    private Quest quest;

    private static final SparseArray<Quest.WeekDay> CHECK_BOX_TO_WEEK_DAY = new SparseArray<Quest.WeekDay>() {{
        put(R.id.add_quest_repeat_monday, Quest.WeekDay.MONDAY);
        put(R.id.add_quest_repeat_tuesday, Quest.WeekDay.TUESDAY);
        put(R.id.add_quest_repeat_wednesday, Quest.WeekDay.WEDNESDAY);
        put(R.id.add_quest_repeat_thursday, Quest.WeekDay.THURSDAY);
        put(R.id.add_quest_repeat_friday, Quest.WeekDay.FRIDAY);
        put(R.id.add_quest_repeat_saturday, Quest.WeekDay.SATURDAY);
        put(R.id.add_quest_repeat_sunday, Quest.WeekDay.SUNDAY);
    }};

    private static final SparseArray<Recurrence.Frequency> SPINNER_TO_FREQUENCY = new SparseArray<Recurrence.Frequency>() {{
        put(0, Recurrence.Frequency.WEEKLY);
        put(1, Recurrence.Frequency.MONTHLY);
        put(2, Recurrence.Frequency.YEARLY);
    }};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @OnClick(R.id.add_quest_start_time)
    public void onStartTimeClick(Button button) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    @OnClick(R.id.add_quest_due_date)
    public void onDueDateClick(Button button) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_add_quest_schedule, container, false);
        ButterKnife.bind(this, view);

        String[] durationOptions = getResources().getStringArray(R.array.duration_options);
        duration.setSelection(durationOptions.length / 2);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        AddQuestActivity activity = (AddQuestActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(R.string.add_quest_title);

        timesPerDay.setOnSeekBarChangeListener(this);

        if (quest.type != Quest.QuestType.RECURRENT) {
            view.findViewById(R.id.add_quest_recurrence_layout).setVisibility(View.GONE);
            includedDaysLayout.setVisibility(View.GONE);
        }

        if (quest.type == Quest.QuestType.RECURRENT) {
            view.findViewById(R.id.add_quest_due_layout).setVisibility(View.GONE);
            Calendar maxDueDate = Calendar.getInstance();
            maxDueDate.set(Calendar.YEAR, 9999);
            quest.due = maxDueDate.getTime();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.get().register(this);
    }

    @Override
    public void onPause() {
        EventBus.get().unregister(this);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_quest_schedule, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                quest.duration = Constants.DURATION_TEXT_INDEX_TO_MINUTES[duration.getSelectedItemPosition()];
                quest.recurrence = createRecurrence();
                EventBus.post(new QuestBuiltEvent(quest));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private Recurrence createRecurrence() {
        Recurrence recurrence = new Recurrence();
        recurrence.interval = recurrenceInterval.getSelectedItemPosition() + 1;
        recurrence.frequency = SPINNER_TO_FREQUENCY.get(recurrenceFrequency.getSelectedItemPosition());
        recurrence.timesPerDay = timesPerDay.getProgress() + 1;
        addIncludedDays(includedDaysLayout, recurrence);
        return recurrence;
    }

    private void addIncludedDays(ViewGroup root, Recurrence recurrence) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                addIncludedDays((ViewGroup) child, recurrence);
            } else if (isCheckedCheckBox(child)) {
                addIncludedDayIfChecked(recurrence, child);
            }
        }
    }

    private void addIncludedDayIfChecked(Recurrence recurrence, View child) {
        Quest.WeekDay weekDay = CHECK_BOX_TO_WEEK_DAY.get(child.getId());
        recurrence.includedDays.add(weekDay.name());
    }

    private boolean isCheckedCheckBox(View child) {
        return child instanceof CheckBox && ((CheckBox) child).isChecked();
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
        timesPerDayLabel.setText(String.format("x%d per day", value + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Subscribe
    public void onDueDateChanged(DateSelectedEvent e) {
        Calendar due = Calendar.getInstance();
        due.setTime(e.date);

        Calendar time = Calendar.getInstance();
        time.setTime(quest.due);

        time.set(Calendar.YEAR, due.get(Calendar.YEAR));
        time.set(Calendar.MONTH, due.get(Calendar.MONTH));
        time.set(Calendar.DAY_OF_MONTH, due.get(Calendar.DAY_OF_MONTH));

        quest.due = time.getTime();
        SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_UI_DATE_FORMAT, Locale.getDefault());
        dueDate.setText(format.format(quest.due));
    }

    @Subscribe
    public void onStartTimeChanged(TimeSelectedEvent e) {
        Calendar due = Calendar.getInstance();
        due.setTime(quest.due);
        Calendar time = Calendar.getInstance();
        time.setTime(e.time);

        due.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        due.set(Calendar.MINUTE, time.get(Calendar.MINUTE));

        quest.due = due.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT, Locale.getDefault());
        startTime.setText(formatter.format(quest.due));
    }
}
