package io.ipoli.android.quest.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.events.DateSelectedEvent;
import io.ipoli.android.quest.events.TimeSelectedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;
import io.ipoli.android.quest.ui.formatters.DueDateFormatter;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;

public class EditQuestActivity extends BaseActivity {
    public static final String DUE_DATE_MILLIS_EXTRA_KEY = "quest_due";

    @Bind(R.id.edit_quest_container)
    CoordinatorLayout rootContainer;

    @Bind(R.id.appbar)
    AppBarLayout appBar;

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

    @Bind(R.id.quest_context_name)
    TextView contextName;

    @Bind(R.id.quest_context_container)
    LinearLayout contextContainer;

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
            hours = TimeUnit.MINUTES.toHours(duration);
            mins = duration - hours * 60;
        }
        durationHours.setText(getString(R.string.hours, hours));
        durationMins.setText(getString(R.string.minutes, mins));

        setStartTimeText(quest.getStartTime());

        setDueDateText(quest.getDue());

        durationHours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int hours = Integer.parseInt(durationHours.getText().toString());
                    durationHours.setText(getString(R.string.hours, hours));
                }
            }
        });

        durationMins.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int minutes = Integer.parseInt(durationMins.getText().toString());
                    durationMins.setText(getString(R.string.minutes, minutes));
                }
            }
        });

        initContextUI();

    }

    private void initContextUI() {
        changeContext(Quest.getContext(quest));

        final QuestContext[] ctxs = QuestContext.values();
        for (int i = 0; i < contextContainer.getChildCount(); i++) {
            final ImageView iv = (ImageView) contextContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) iv.getBackground();
            drawable.setColor(ContextCompat.getColor(this, ctxs[i].resLightColor));

            final QuestContext ctx = ctxs[i];
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSelectedContextCheck();
                    changeContext(ctx);
                }
            });

        }
    }

    private void changeContext(QuestContext ctx) {
        setBackgroundColors(ctx);
        Quest.setContext(quest, ctx);
        setSelectedContext();
    }

    private void setSelectedContext() {
        getCurrentContextImageView().setImageResource(R.drawable.ic_done_white_24dp);
        setContextName();
    }

    private void removeSelectedContextCheck() {
        getCurrentContextImageView().setImageDrawable(null);
    }

    private ImageView getCurrentContextImageView() {
        String ctxId = "quest_context_" + quest.getContext().toLowerCase();
        int ctxResId = getResources().getIdentifier(ctxId, "id", getPackageName());
        return (ImageView) findViewById(ctxResId);
    }

    private void setBackgroundColors(QuestContext ctx) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, ctx.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, ctx.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, ctx.resDarkColor));
    }

    private void setContextName() {
        contextName.setText(quest.getContext().substring(0, 1).toUpperCase() + quest.getContext().substring(1).toLowerCase());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onSaveQuest(FloatingActionButton button) {
        String name = nameText.getText().toString().trim();
        int hours = Integer.parseInt(durationHours.getText().toString());
        int mins = Integer.parseInt(durationMins.getText().toString());
        int duration = hours * 60 + mins;

        quest.setName(name);
        quest.setDuration(duration);
        if (quest.getDue() != null) {
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
            text = getString(R.string.due_date_default);
        } else {
            text = DateUtils.isToday(date) ? getString(R.string.today) : DueDateFormatter.format(date);
        }
        dueDateBtn.setText(text);
    }

    private void setStartTimeText(Date time) {
        if (time == null) {
            startTimeBtn.setText(R.string.start_time_default);
        } else {
            startTimeBtn.setText(StartTimeFormatter.format(time));
        }
    }
}
