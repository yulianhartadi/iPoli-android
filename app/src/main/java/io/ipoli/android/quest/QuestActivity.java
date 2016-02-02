package io.ipoli.android.quest;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.commands.StopQuestCommand;
import io.ipoli.android.quest.events.CompleteQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class QuestActivity extends BaseActivity {

    public static final String ACTION_QUEST_DONE = "io.ipoli.android.action.QUEST_DONE";
    public static final String ACTION_QUEST_CANCELED = "io.ipoli.android.action.QUEST_CANCELED";

    @Bind(R.id.quest_details_progress)
    ProgressBar timerProgress;

    @Bind(R.id.quest_details_timer)
    FloatingActionButton timerButton;

    @Bind(R.id.quest_details_time)
    TextView timer;

    @Bind(R.id.quest_details_name)
    TextView name;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    Bus eventBus;

    private CountDownTimer questTimer;
    private boolean questHasDuration;
    private Quest quest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || TextUtils.isEmpty(getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY))) {
            finish();
            return;
        }
        setContentView(R.layout.activity_quest);
        ButterKnife.bind(this);
        appComponent().inject(this);

        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        quest = questPersistenceService.findById(questId);
        questHasDuration = quest.getDuration() > 0;

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.md_blue_800));
        resetTimer();
        name.setText(quest.getName());

        Intent intent = getIntent();
        String action = intent.getAction();

        if (QuestActivity.ACTION_QUEST_CANCELED.equals(action)) {
            new StopQuestCommand(quest, questPersistenceService, this).execute();
            stopTimer();
            resetTimer();
        } else if (QuestActivity.ACTION_QUEST_DONE.equals(action)) {
            QuestNotificationScheduler.stopTimer(questId, this);
            QuestNotificationScheduler.stopDone(questId, this);
            Intent i = new Intent(this, QuestCompleteActivity.class);
            i.putExtra(Constants.QUEST_ID_EXTRA_KEY, quest.getId());
            startActivityForResult(i, Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE);
        }
    }

    private void resetTimer() {
        int minuteDuration = questHasDuration ? quest.getDuration() : 0;
        Date d = new Date(TimeUnit.MINUTES.toMillis(minuteDuration));
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
        timer.setText(sdf.format(d));
        timerProgress.setMax(100);
        timerProgress.setProgress(0);
        timerProgress.setSecondaryProgress(100);
    }

    @Override
    protected void onResume() {
        QuestNotificationScheduler.stopTimer(quest.getId(), this);
        if (Quest.getStatus(quest) == Status.STARTED) {
            long elapsedMillis = System.currentTimeMillis() - quest.getActualStartDateTime().getTime();
            startTimer(elapsedMillis);
            timerButton.setImageResource(R.drawable.ic_stop_white_32dp);
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK || resultCode == RESULT_CANCELED) && requestCode == Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE) {
            final Difficulty d = (Difficulty) data.getSerializableExtra(QuestCompleteActivity.DIFFICULTY_EXTRA_KEY);
            final String log = data.getStringExtra(QuestCompleteActivity.LOG_EXTRA_KEY);
            quest.setStatus(Status.COMPLETED.name());
            quest.setLog(log);
            quest.setDifficulty(d.name());
            questPersistenceService.save(quest);
            eventBus.post(new CompleteQuestEvent(quest));
            stopTimer();
            finish();
        }
    }

    private void startTimer(long elapsedMillis) {
        long totalTime = questHasDuration ? TimeUnit.MINUTES.toMillis(quest.getDuration()) : TimeUnit.MINUTES.toMillis(Constants.QUEST_WITH_NO_DURATION_TIMER_MINUTES);

        timerProgress.setMax((int) TimeUnit.MILLISECONDS.toSeconds(totalTime));
        timerProgress.setSecondaryProgress((int) TimeUnit.MILLISECONDS.toSeconds(totalTime));

        questTimer = createQuestTimer(totalTime, elapsedMillis);
        questTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTimerRunning() && !ACTION_QUEST_DONE.equals(getIntent().getAction())) {
            stopTimer();
            long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - quest.getActualStartDateTime().getTime());
            int qDuration = quest.getDuration();
            boolean hasRemainingTime = qDuration - elapsedMinutes > 0;
            if (hasRemainingTime) {
                QuestNotificationScheduler.scheduleUpdateTimer(quest.getId(), this);
            }
        }
    }

    private void stopTimer() {
        if (questTimer != null) {
            questTimer.cancel();
            questTimer = null;
        }
    }

    private boolean isTimerRunning() {
        return questTimer != null;
    }

    @OnClick(R.id.quest_details_timer)
    public void onTimerTap(View v) {
        if (isTimerRunning()) {
            new StopQuestCommand(quest, questPersistenceService, this).execute();
            stopTimer();
            resetTimer();
            timerButton.setImageResource(R.drawable.ic_play_arrow_white_32dp);
        } else {
            new StartQuestCommand(this, questPersistenceService, quest).execute();
            startTimer(0);
            timerButton.setImageResource(R.drawable.ic_stop_white_32dp);
        }
    }

    CountDownTimer createQuestTimer(final long totalTime, final long elapsedMillis) {
        return new CountDownTimer(totalTime + 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                long elapsedSeconds = (totalTime - millisUntilFinished + elapsedMillis) / 1000;
                timerProgress.setProgress((int) elapsedSeconds);
                long timerMillis = questHasDuration ?
                        millisUntilFinished - elapsedMillis :
                        System.currentTimeMillis() - quest.getActualStartDateTime().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
                timer.setText(sdf.format(new Date(timerMillis)));
            }

            @Override
            public void onFinish() {
                // count up
            }
        };
    }

    @OnClick(R.id.quest_details_done)
    public void onDoneTap(View v) {
        stopTimer();
        QuestNotificationScheduler.stopTimer(quest.getId(), this);
        QuestNotificationScheduler.stopDone(quest.getId(), this);
        Intent i = new Intent(this, QuestCompleteActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, quest.getId());
        startActivityForResult(i, Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE);
    }
}
