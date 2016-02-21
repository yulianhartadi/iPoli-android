package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.squareup.otto.Bus;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.Difficulty;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.events.CompleteQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

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
        quest = questPersistenceService.findById(questId);
    }

    @OnClick(R.id.quest_complete_done)
    public void onDoneTap(View v) {
        saveQuest();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        saveQuest();
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private Difficulty getDifficulty() {
        int radioButtonID = difficultyGroup.getCheckedRadioButtonId();
        View radioButton = difficultyGroup.findViewById(radioButtonID);
        int idx = difficultyGroup.indexOfChild(radioButton);
        RadioButton r = (RadioButton) difficultyGroup.getChildAt(idx);
        try {
            return Difficulty.valueOf(r.getText().toString().toUpperCase());
        } catch (Exception ignored) {
            return Difficulty.UNKNOWN;
        }
    }

    private void saveQuest() {
        quest.setLog(log.getText().toString());
        quest.setDifficulty(getDifficulty().name());

        if (quest.getActualStartDateTime() != null) {
            long nowMillis = System.currentTimeMillis();
            long startMillis = quest.getActualStartDateTime().getTime();
            quest.setMeasuredDuration((int) TimeUnit.MILLISECONDS.toMinutes(nowMillis - startMillis));
        }
        quest.setCompletedAtDateTime(new Date());

        quest = questPersistenceService.save(quest);
        QuestNotificationScheduler.stopAll(quest.getId(), this);
        eventBus.post(new CompleteQuestEvent(quest));
    }
}
