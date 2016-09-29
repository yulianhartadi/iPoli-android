package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.adapters.QuestDetailsAdapter;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.commands.StopQuestCommand;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.EditNoteRequestEvent;
import io.ipoli.android.quest.events.StartQuestTapEvent;
import io.ipoli.android.quest.events.StopQuestTapEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.ui.dialogs.TextPickerFragment;
import io.ipoli.android.quest.ui.formatters.TimerFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/26/16.
 */

public class QuestDetailActivity extends BaseActivity implements Chronometer.OnChronometerTickListener, TextPickerFragment.OnTextPickedListener {

    @BindView(R.id.root_container)
    ViewGroup rootLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.quest_details_progress)
    ProgressBar timerProgress;

    @BindView(R.id.quest_details_time)
    Chronometer timer;

    @BindView(R.id.quest_details)
    RecyclerView details;

    @BindView(R.id.quest_details_timer)
    FloatingActionButton timerButton;

    private Quest quest;

    private QuestDetailsAdapter adapter;

    @Inject
    QuestPersistenceService questPersistenceService;

    private String questId;

    private boolean questHasDuration;

    private boolean isTimerRunning;
    private int elapsedSeconds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!IntentUtils.hasExtra(getIntent(), Constants.QUEST_ID_EXTRA_KEY)) {
            finish();
            return;
        }

        questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);

        setContentView(R.layout.activity_quest_detail);

        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
        }
        collapsingToolbarLayout.setTitleEnabled(false);

        details.setLayoutManager(new LinearLayoutManager(this));
        details.setHasFixedSize(true);

        quest = new Quest("Hello world");

        List<SubQuest> subQuests = new ArrayList<>();
        subQuests.add(new SubQuest("Prepare Barbell"));
        subQuests.add(new SubQuest("Do 10 pull-ups"));
        subQuests.add(new SubQuest("Do 10 push-ups"));

        quest.setSubQuests(subQuests);

        List<Note> notes = new ArrayList<>();
        notes.add(new Note("Workout hard even though Vihar is not the smartest cat in the world!"));
        notes.add(new Note(Note.Type.URL, "Visit Medium", "https://medium.com/"));
        notes.add(new Note(Note.Type.INTENT, "Learn English on Duolingo", "https://medium.com/"));
        quest.setNotes(notes);

        adapter = new QuestDetailsAdapter(this, quest, eventBus);
        details.setAdapter(adapter);

//        adapter.setSubQuests(subQuests);
    }

    @Override
    protected void onStart() {
        super.onStart();
        questPersistenceService.listenById(questId, quest -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(quest.getName());
            }
            QuestNotificationScheduler.stopTimer(questId, this);
            this.quest = quest;
            initUI();
            if (Quest.isStarted(quest)) {
                elapsedSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - quest.getActualStartDate().getTime());
                resumeTimer();
                timerButton.setImageResource(R.drawable.ic_stop_white_32dp);
            }
        });
    }

    @OnClick(R.id.quest_details_timer)
    public void onTimerTap(View v) {
        if (isTimerRunning) {
            eventBus.post(new StopQuestTapEvent(quest));
            stopTimer();
            new StopQuestCommand(this, quest, questPersistenceService).execute();
            resetTimerUI();
            timerButton.setImageResource(R.drawable.ic_play_arrow_white_32dp);
        } else {
            eventBus.post(new StartQuestTapEvent(quest));
            new StartQuestCommand(this, quest, questPersistenceService).execute();
            startTimer();
            timerButton.setImageResource(R.drawable.ic_stop_white_32dp);
        }
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
    protected void onStop() {
        questPersistenceService.removeAllListeners();
        super.onStop();
    }

    private void setBackgroundColors(Category category) {
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbarLayout.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
    }

    @Subscribe
    public void onEditNoteRequest(EditNoteRequestEvent e) {
        TextPickerFragment.newInstance((String) e.note.getText(), R.string.pick_note_title, this).show(getSupportFragmentManager());
    }

    @Override
    public void onTextPicked(String text) {
        List<Note> notes = quest.getTextNotes();
        if (!notes.isEmpty()) {
            notes.get(0).setText(text);
        }
        adapter.updateNotes(quest.getNotes());
    }

    private void initUI() {
        setBackgroundColors(Quest.getCategory(quest));
        questHasDuration = quest.getDuration() > 0;
        resetTimerUI();
        elapsedSeconds = 0;
    }

    private void resetTimerUI() {
        timer.setBase(0);
        int minuteDuration = questHasDuration ? quest.getDuration() : 0;
        timer.setText(TimerFormatter.format(TimeUnit.MINUTES.toMillis(minuteDuration)));
        timerProgress.setProgress(0);
        long totalTime = questHasDuration ?
                TimeUnit.MINUTES.toMillis(quest.getDuration()) :
                TimeUnit.MINUTES.toMillis(Constants.QUEST_WITH_NO_DURATION_TIMER_MINUTES);
        timerProgress.setMax((int) TimeUnit.MILLISECONDS.toSeconds(totalTime));
        timerProgress.setSecondaryProgress((int) TimeUnit.MILLISECONDS.toSeconds(totalTime));
    }

    private void startTimer() {
        elapsedSeconds = 0;
        resumeTimer();
    }

    private void resumeTimer() {
        timer.setOnChronometerTickListener(this);
        timer.start();
        isTimerRunning = true;
    }

    private void stopTimer() {
        timer.setOnChronometerTickListener(null);
        timer.stop();
        isTimerRunning = false;
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        long nowMillis = quest.getActualStartDate().getTime() + TimeUnit.SECONDS.toMillis(elapsedSeconds);
        long questDurationSeconds = TimeUnit.MINUTES.toSeconds(quest.getDuration());

        timerProgress.setProgress((int) getTimerProgress(elapsedSeconds));

        if (questHasDuration && isOverdue(questDurationSeconds)) {
            showOverdueTime(questDurationSeconds);
        } else if (questHasDuration) {
            showCountDownTime(nowMillis);
        } else {
            showCountUpTime(nowMillis);
        }

        elapsedSeconds++;
    }

    private void showOverdueTime(long questDurationSeconds) {
        long overdueMillis = TimeUnit.SECONDS.toMillis(elapsedSeconds - questDurationSeconds);
        timer.setText("+" + TimerFormatter.format(overdueMillis));
    }

    private void showCountDownTime(long nowMillis) {
        long endTimeMillis = quest.getActualStartDate().getTime() + TimeUnit.MINUTES.toMillis(quest.getDuration());
        timer.setText(TimerFormatter.format(endTimeMillis - nowMillis));
    }

    private void showCountUpTime(long nowMillis) {
        long timerMillis = nowMillis - quest.getActualStartDate().getTime();
        timer.setText(TimerFormatter.format(timerMillis));
    }

    private boolean isOverdue(long questDurationSeconds) {
        return questDurationSeconds < elapsedSeconds;
    }

    private long getTimerProgress(long elapsedSeconds) {
        if (questHasDuration) {
            // the progress is set to max if elapsed seconds is larger than max progress
            return elapsedSeconds;
        } else {
            long defaultDurationSeconds = TimeUnit.MINUTES.toSeconds(Constants.QUEST_WITH_NO_DURATION_TIMER_MINUTES);
            return elapsedSeconds % defaultDurationSeconds;
        }
    }
}
