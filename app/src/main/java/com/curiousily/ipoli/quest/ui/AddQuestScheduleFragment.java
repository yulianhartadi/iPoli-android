package com.curiousily.ipoli.quest.ui;

import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.AddQuestActivity;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.events.QuestBuiltEvent;
import com.curiousily.ipoli.ui.DatePickerFragment;
import com.curiousily.ipoli.ui.TimePickerFragment;
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

    @Bind(R.id.add_quest_notes)
    EditText notes;

    @Bind(R.id.add_quest_due_date)
    Button dueDate;

    @Bind(R.id.add_quest_start_time)
    Button startTime;

    private Quest quest;

    private static final SparseArray<Quest.Repeat> CHECK_BOX_TO_REPEAT = new SparseArray<Quest.Repeat>() {{
        put(R.id.add_quest_repeat_monday, Quest.Repeat.MONDAY);
        put(R.id.add_quest_repeat_tuesday, Quest.Repeat.TUESDAY);
        put(R.id.add_quest_repeat_wednesday, Quest.Repeat.WEDNESDAY);
        put(R.id.add_quest_repeat_thursday, Quest.Repeat.THURSDAY);
        put(R.id.add_quest_repeat_friday, Quest.Repeat.FRIDAY);
        put(R.id.add_quest_repeat_saturday, Quest.Repeat.SATURDAY);
        put(R.id.add_quest_repeat_sunday, Quest.Repeat.SUNDAY);
    }};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @OnClick(R.id.add_quest_due_date)
    public void onDueDateClick(Button button) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    @OnClick(R.id.add_quest_start_time)
    public void onStartTimeClick(Button button) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    @OnClick({R.id.add_quest_repeat_monday, R.id.add_quest_repeat_tuesday, R.id.add_quest_repeat_wednesday, R.id.add_quest_repeat_thursday, R.id.add_quest_repeat_friday, R.id.add_quest_repeat_saturday, R.id.add_quest_repeat_sunday})
    public void onRequestRepeatClick(CheckBox checkBox) {
        Quest.Repeat repeatDay = CHECK_BOX_TO_REPEAT.get(checkBox.getId());
        if (checkBox.isChecked()) {
            quest.repeats.add(repeatDay);
        } else {
            quest.repeats.remove(repeatDay);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_add_quest_schedule, container, false);
        ButterKnife.bind(this, view);


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.duration_options));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duration.setAdapter(dataAdapter);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        AddQuestActivity activity = (AddQuestActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(R.string.add_quest_title);

        timesPerDay.setOnSeekBarChangeListener(this);

        return view;
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
                quest.duration = duration.getSelectedItemPosition();
                quest.timesPerDay = timesPerDay.getProgress();
                quest.notes = notes.getText().toString();
                EventBus.post(new QuestBuiltEvent(quest));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
        timesPerDayLabel.setText("x" + (value + 1) + " per day");
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
        SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT, Locale.getDefault());
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
