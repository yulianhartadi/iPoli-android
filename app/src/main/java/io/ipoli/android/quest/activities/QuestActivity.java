package io.ipoli.android.quest.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.note.events.OpenNoteEvent;
import io.ipoli.android.quest.adapters.QuestDetailsAdapter;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.commands.StopQuestCommand;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DoneQuestTapEvent;
import io.ipoli.android.quest.events.EditNoteRequestEvent;
import io.ipoli.android.quest.events.StartQuestTapEvent;
import io.ipoli.android.quest.events.StopQuestTapEvent;
import io.ipoli.android.quest.events.subquests.CompleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.DeleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.NewSubQuestEvent;
import io.ipoli.android.quest.events.subquests.UndoCompleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.UpdateSubQuestNameEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.ui.dialogs.TextPickerFragment;
import io.ipoli.android.quest.ui.formatters.TimerFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/26/16.
 */

public class QuestActivity extends BaseActivity implements Chronometer.OnChronometerTickListener, TextPickerFragment.OnTextPickedListener {
    public static final String ACTION_QUEST_CANCELED = "io.ipoli.android.intent.action.QUEST_CANCELED";
    public static final String ACTION_START_QUEST = "io.ipoli.android.intent.action.START_QUEST";

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

    private boolean isAddSubQuestInEditMode = false;
    private boolean afterOnCreate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!IntentUtils.hasExtra(getIntent(), Constants.QUEST_ID_EXTRA_KEY)) {
            finish();
            return;
        }

        questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);

        setContentView(R.layout.activity_quest);

        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
        afterOnCreate = true;

        collapsingToolbarLayout.setTitleEnabled(false);

        details.setLayoutManager(new LinearLayoutManager(this));
        details.setHasFixedSize(true);

        eventBus.post(new ScreenShownEvent(EventSource.QUEST));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quest_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent i = new Intent(this, EditQuestActivity.class);
                i.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
                startActivity(i);
                return true;
            case R.id.action_done:
                eventBus.post(new DoneQuestTapEvent(quest));
                stopTimer();
                eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.QUEST));
                finish();
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_quest, R.string.help_dialog_quest_title, "quest").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        questPersistenceService.listenById(questId, quest -> {
            if (afterOnCreate) {
                afterOnCreate = false;
                String action = getIntent().getAction();
                if (ACTION_QUEST_CANCELED.equals(action)) {
                    new StopQuestCommand(this, quest, questPersistenceService).execute();
                } else if (ACTION_START_QUEST.equals(action)) {
                    NotificationManagerCompat.from(this).cancel(getIntent().getIntExtra(Constants.REMINDER_NOTIFICATION_ID_EXTRA_KEY, 0));
                    new StartQuestCommand(this, quest, questPersistenceService).execute();
                }
            }
            onQuestFound(quest);
        });
    }

    private void onQuestFound(Quest quest) {
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
        adapter = new QuestDetailsAdapter(this, quest, isAddSubQuestInEditMode, eventBus);
        details.setAdapter(adapter);
        isAddSubQuestInEditMode = false;
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
        TextPickerFragment.newInstance(e.text, R.string.pick_note_title, this).show(getSupportFragmentManager());
    }

    @Override
    public void onTextPicked(String text) {

        if (StringUtils.isEmpty(text)) {
            quest.removeTextNote();
        } else {
            List<Note> notes = quest.getTextNotes();
            if (!notes.isEmpty()) {
                notes.get(0).setText(text);
            } else {
                quest.addNote(new Note(text));
            }
        }
        questPersistenceService.save(quest);
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

    @Subscribe
    public void onNewSubQuestEvent(NewSubQuestEvent e) {
        quest.addSubQuest(e.subQuest);
        isAddSubQuestInEditMode = true;
        questPersistenceService.save(quest);
    }

    @Subscribe
    public void onUpdateSubQuestEvent(UpdateSubQuestNameEvent e) {
        questPersistenceService.save(quest);
    }

    @Subscribe
    public void onDeleteSubQuestEvent(DeleteSubQuestEvent e) {
        quest.removeSubQuest(e.subQuest);
        questPersistenceService.save(quest);
    }

    @Subscribe
    public void onCompleteSubQuest(CompleteSubQuestEvent e) {
        questPersistenceService.save(quest);
    }

    @Subscribe
    public void onUndoCompleteSubQuest(UndoCompleteSubQuestEvent e) {
        questPersistenceService.save(quest);
    }

    @Subscribe
    public void onOpenNote(OpenNoteEvent e) {
        Note note = e.note;
        if (note.getNoteType() == Note.Type.URL) {
            Intent noteLink = new Intent(Intent.ACTION_VIEW, Uri.parse(note.getData()));
            startActivity(noteLink);
        } else if (note.getNoteType() == Note.Type.INTENT) {
            String packageName = note.getData();
            try {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                startActivity(LaunchIntent);
            } catch (Exception ex) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
                }
            }
        }
    }
}
