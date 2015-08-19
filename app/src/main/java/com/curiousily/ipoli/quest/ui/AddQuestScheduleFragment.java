package com.curiousily.ipoli.quest.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.AddQuestActivity;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.events.QuestBuiltEvent;
import com.curiousily.ipoli.ui.DatePickerFragment;
import com.curiousily.ipoli.ui.TimePickerFragment;

import java.util.ArrayList;
import java.util.List;

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

    private Quest quest;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_add_quest_schedule, container, false);
        ButterKnife.bind(this, view);

        List<String> list = new ArrayList<>();
        list.add("1 minute");
        list.add("2 minutes");
        list.add("3 minutes");
        list.add("5 minutes");
        list.add("10 minutes");
        list.add("15 minutes");
        list.add("20 minutes");
        list.add("25 minutes");
        list.add("30 minutes");
        list.add("45 minutes");
        list.add("1 hour");
        list.add("1 hour and 30 minutes");
        list.add("2 hours");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, list);
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
                EventBus.get().post(new QuestBuiltEvent(quest));
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
}
