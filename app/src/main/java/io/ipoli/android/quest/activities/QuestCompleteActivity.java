package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.Difficulty;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.data.Log;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.QuestDifficultyChangedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/22/16.
 */
public class QuestCompleteActivity extends BaseActivity {

    @Bind(R.id.quest_complete_log)
    EditText log;

    @Bind(R.id.quest_complete_difficulty_group)
    RadioGroup difficultyGroup;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    Bus eventBus;

    private Quest quest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_complete);
        setFinishOnTouchOutside(false);
        ButterKnife.bind(this);
        appComponent().inject(this);

        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId).subscribe(q -> {
            quest = q;
            addDifficultyChangeListener();
        });


        eventBus.post(new ScreenShownEvent("quest_complete"));
    }

    private void addDifficultyChangeListener() {
        difficultyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String d = Difficulty.values()[getDifficulty()].name();
            eventBus.post(new QuestDifficultyChangedEvent(quest, d));
        });
    }

    @OnClick(R.id.quest_complete_done)
    public void onDoneTap(View v) {
        saveQuest().subscribe(q -> {
            QuestNotificationScheduler.stopAll(q.getId(), this);
            eventBus.post(new QuestCompletedEvent(q));
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        saveQuest();
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private int getDifficulty() {
        int radioButtonID = difficultyGroup.getCheckedRadioButtonId();
        View radioButton = difficultyGroup.findViewById(radioButtonID);
        return difficultyGroup.indexOfChild(radioButton);
    }

    private Observable<Quest> saveQuest() {
        String logText = log.getText().toString();
        if (!TextUtils.isEmpty(logText)) {
            quest.getLogs().add(new Log(logText));
        }
        quest.setDifficulty(getDifficulty() + 1);

        if (quest.getActualStartDateTime() != null) {
            long nowMillis = System.currentTimeMillis();
            long startMillis = quest.getActualStartDateTime().getTime();
            quest.setActualDuration((int) TimeUnit.MILLISECONDS.toMinutes(nowMillis - startMillis));
        }
        quest.setCompletedAt(DateUtils.nowUTC());

        return questPersistenceService.save(quest);

    }
}
