package io.ipoli.android.quest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.events.DateSelectedEvent;
import io.ipoli.android.quest.events.TimeSelectedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;

public class EditQuestActivity extends BaseActivity {
    SimpleDateFormat dueDateFormat = new SimpleDateFormat("dd.MM.yy");
    SimpleDateFormat startTimeFormat = new SimpleDateFormat("HH:mm");

    @Bind(R.id.edit_quest_container)
    CoordinatorLayout rootContainer;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quest_name)
    EditText nameText;

    @Bind(R.id.duration_hours)
    EditText durationHours;

    @Bind(R.id.duration_mins)
    EditText durationMins;

    @Bind(R.id.quest_due_date)
    Button dueDateBtn;

    @Bind(R.id.quest_start_time)
    Button startTimeBtn;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    private Quest quest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quest);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appComponent().inject(this);

        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        initUI(questId);
    }

    private void initUI(String questId) {
        quest = questPersistenceService.findById(questId);
        nameText.setText(quest.getName());
        nameText.setSelection(nameText.getText().length());

        int duration = quest.getDuration();
        long hours = 0;
        long mins = 0;
        if (duration > 0) {
            hours = TimeUnit.MINUTES.toHours(quest.getDuration());
            mins = duration - hours * 60;
        }
        durationHours.setText(String.format("%02d", hours));
        durationMins.setText(String.format("%02d", mins));

        setStartTimeText(quest.getStartTime());
        setDueDateText(quest.getDue());

        durationHours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int hours = Integer.parseInt(durationHours.getText().toString());
                    durationHours.setText(String.format("%02d", hours));
                }
            }
        });

        durationMins.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    int mins = Integer.parseInt(durationMins.getText().toString());
                    durationMins.setText(String.format("%02d", mins));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @OnClick(R.id.quest_due_date)
    public void onDueDateClick(Button button) {
        DialogFragment f = new DatePickerFragment();
        f.show(this.getSupportFragmentManager(), "datePicker");
    }

    @OnClick(R.id.quest_start_time)
    public void onStartTimeClick(Button button) {
        DialogFragment f = new TimePickerFragment();
        f.show(this.getSupportFragmentManager(), "timePicker");
    }

    @OnClick(R.id.save_quest)
    public void onSaveQuest(FloatingActionButton button){
        String name = nameText.getText().toString().trim();
        int hours = Integer.parseInt(durationHours.getText().toString());
        int mins = Integer.parseInt(durationMins.getText().toString());
        int duration = hours * 60 + mins;

        quest.setName(name);
        quest.setDuration(duration);
        if(quest.getDue() != null) {
            quest.setStatus(Status.PLANNED.name());
        }
        quest = questPersistenceService.save(quest);

        Intent data = new Intent();
        data.putExtras(getIntent());
        setResult(RESULT_OK, data);
        finish();
    }

    @Subscribe
    public void onDueDateSelected(DateSelectedEvent e) {
        setDueDateText(e.date);
        quest.setDue(e.date);
    }

    @Subscribe
    public void onStartTimeSelected(TimeSelectedEvent e) {
        setStartTimeText(e.time);
        quest.setStartTime(e.time);
    }

    private void setDueDateText(Date date) {
        String text = "";
        if (date == null) {
            text = "Due date";
        } else {
            text = DateUtils.isToday(date.getTime()) ? "today" : dueDateFormat.format(date);
        }
        dueDateBtn.setText(text);
    }

    private void setStartTimeText(Date time) {
        if (time == null) {
            startTimeBtn.setText("Start time");
        } else {
            startTimeBtn.setText(startTimeFormat.format(time));
        }
    }
}
