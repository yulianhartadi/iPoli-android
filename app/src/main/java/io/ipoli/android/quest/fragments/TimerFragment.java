package io.ipoli.android.quest.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.commands.StopQuestCommand;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DoneQuestTapEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.StartQuestTapEvent;
import io.ipoli.android.quest.events.StopQuestTapEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.ui.formatters.TimerFormatter;
import rx.Observable;

public class TimerFragment extends BaseFragment implements Chronometer.OnChronometerTickListener{


    @Inject
    Bus eventBus;

    @BindView(R.id.root_container)
    ViewGroup rootLayout;

    @BindView(R.id.quest_details_progress)
    ProgressBar timerProgress;

    @BindView(R.id.quest_details_timer)
    FloatingActionButton timerButton;

    @BindView(R.id.quest_details_time)
    Chronometer timer;

    @BindView(R.id.quest_details_name)
    TextView name;

    @BindView(R.id.quest_details_edit)
    ImageButton edit;

    QuestPersistenceService questPersistenceService;

    private Unbinder unbinder;

    private boolean questHasDuration;
    private Quest quest;

    private boolean isTimerRunning;
    private int elapsedSeconds;
    private String questId;
    private boolean afterOnCreate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_quest_timer, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());

        questId = getActivity().getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        afterOnCreate = true;
        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    private void initUI() {
        setBackgroundColors(Quest.getCategory(quest));
        questHasDuration = quest.getDuration() > 0;
        resetTimerUI();
        elapsedSeconds = 0;
        name.setText(quest.getName());
    }

    private void setBackgroundColors(Category category) {
        rootLayout.setBackgroundColor(ContextCompat.getColor(getContext(), category.resDarkerColor));
        getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), category.resDarkerColor));
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), category.resDarkerColor));
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

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        Quest q = questPersistenceService.findById(questId);
        Observable<Quest> questObservable = null;
        if (afterOnCreate) {
            afterOnCreate = false;
            String action = getActivity().getIntent().getAction();
            if (QuestActivity.ACTION_QUEST_CANCELED.equals(action)) {
                questObservable = new StopQuestCommand(getContext(), q, questPersistenceService).execute();
            } else if (QuestActivity.ACTION_START_QUEST.equals(action)) {
                NotificationManagerCompat.from(getContext()).cancel(getActivity().getIntent().getIntExtra(Constants.REMINDER_NOTIFICATION_ID_EXTRA_KEY, 0));
                questObservable = new StartQuestCommand(getContext(), q, questPersistenceService).execute();
            }
        }
        if (questObservable == null) {
            questObservable = Observable.just(q);
        }
        onQuestFound(questObservable);
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        Quest q = questPersistenceService.findById(questId);
        onQuestFound(Observable.just(q));
    }

    private void onQuestFound(Observable<Quest> questObservable) {
        QuestNotificationScheduler.stopTimer(questId, getContext());
        questObservable.subscribe(q -> {
            this.quest = q;
            initUI();
            if (Quest.isStarted(quest)) {
                elapsedSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - quest.getActualStart().getTime());
                resumeTimer();
                timerButton.setImageResource(R.drawable.ic_stop_white_32dp);
                edit.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        if (isTimerRunning) {
            stopTimer();
            long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - quest.getActualStart().getTime());
            boolean isOverdue = questHasDuration && quest.getDuration() - elapsedMinutes < 0;
            if (!isOverdue) {
                QuestNotificationScheduler.scheduleUpdateTimer(questId, getContext());
            }
        }
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.RESULT_REMOVED) {
            getActivity().finish();
        }
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

    @OnClick(R.id.quest_details_timer)
    public void onTimerTap(View v) {
        if (isTimerRunning) {
            eventBus.post(new StopQuestTapEvent(quest));
            stopTimer();
            new StopQuestCommand(getContext(), quest, questPersistenceService).execute().subscribe(q -> {
                quest = q;
                resetTimerUI();
                timerButton.setImageResource(R.drawable.ic_play_arrow_white_32dp);
                edit.setVisibility(View.VISIBLE);
            });
        } else {
            eventBus.post(new StartQuestTapEvent(quest));
            new StartQuestCommand(getContext(), quest, questPersistenceService).execute().subscribe(q -> {
                quest = q;
                startTimer();
                timerButton.setImageResource(R.drawable.ic_stop_white_32dp);
                edit.setVisibility(View.GONE);
            });
        }
    }

    @OnClick(R.id.quest_details_done)
    public void onDoneTap(View v) {
        eventBus.post(new DoneQuestTapEvent(quest));
        stopTimer();
        eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.QUEST));
        long experience = quest.getExperience();
        long coins = quest.getCoins();
        Toast.makeText(getContext(), getString(R.string.quest_complete, experience, coins), Toast.LENGTH_LONG).show();
        getActivity().finish();
    }

    @OnClick(R.id.quest_details_edit)
    public void onEditTap(View v) {
        eventBus.post(new EditQuestRequestEvent(quest, EventSource.QUEST));
        Intent i = new Intent(getContext(), EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        startActivityForResult(i, Constants.EDIT_QUEST_RESULT_REQUEST_CODE);
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        long nowMillis = quest.getActualStart().getTime() + TimeUnit.SECONDS.toMillis(elapsedSeconds);
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
        long endTimeMillis = quest.getActualStart().getTime() + TimeUnit.MINUTES.toMillis(quest.getDuration());
        timer.setText(TimerFormatter.format(endTimeMillis - nowMillis));
    }

    private void showCountUpTime(long nowMillis) {
        long timerMillis = nowMillis - quest.getActualStart().getTime();
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

    @Override
    public void onDestroyView() {
        questPersistenceService.removeAllListeners();
        unbinder.unbind();
        super.onDestroyView();
    }
}
