package io.ipoli.android.quest;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.events.DateSelectedEvent;
import io.ipoli.android.quest.events.TimeSelectedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;

public class EditQuestActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quest_name)
    EditText name;

    @Bind(R.id.quest_due_date)
    Button dueDate;

    @Bind(R.id.quest_start_time)
    Button startTime;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    private PlanDayQuestAdapter planDayQuestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quest);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appComponent().inject(this);


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

    @Subscribe
    public void onDueDateSelected(DateSelectedEvent e) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
        dueDate.setText("Due " + dateFormat.format(e.date));
    }

    @Subscribe
    public void onStartTimeSelected(TimeSelectedEvent e) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm");
        startTime.setText("Starts " + dateFormat.format(e.time));
    }
}
