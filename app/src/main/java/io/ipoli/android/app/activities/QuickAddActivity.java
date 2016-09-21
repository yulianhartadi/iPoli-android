package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.ui.CategoryView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/4/16.
 */
public class QuickAddActivity extends BaseActivity {

    @BindView(R.id.quick_add_text)
    TextInputEditText questText;

    @BindView(R.id.quest_category)
    CategoryView categoryView;

    private QuestParser questParser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_quick_add);
        ButterKnife.bind(this);
        questParser = new QuestParser(new PrettyTimeParser());
        String additionalText = getIntent().getStringExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT);
        questText.setText(additionalText);
        questText.setSelection(0);
        showKeyboard();
    }

    @OnClick(R.id.add)
    public void onAddQuest(View v) {
        String text = questText.getText().toString();
        if (StringUtils.isEmpty(text)) {
            Toast.makeText(this, R.string.quest_name_validation, Toast.LENGTH_LONG).show();
            return;
        }

        Reminder reminder = new Reminder(0, new Random().nextInt());
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(reminder);

        if (questParser.isRepeatingQuest(text)) {
            RepeatingQuest repeatingQuest = questParser.parseRepeatingQuest(text);
            if (repeatingQuest == null) {
                Toast.makeText(this, R.string.quest_name_validation, Toast.LENGTH_LONG).show();
                return;
            }
            repeatingQuest.setCategory(categoryView.getSelectedCategory().name());
            eventBus.post(new NewRepeatingQuestEvent(repeatingQuest, reminders));
            Toast.makeText(this, R.string.repeating_quest_saved, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Quest quest = questParser.parseQuest(text);
        if (quest == null) {
            Toast.makeText(this, R.string.quest_name_validation, Toast.LENGTH_LONG).show();
            return;
        }
        quest.setCategory(categoryView.getSelectedCategory().name());
        eventBus.post(new NewQuestEvent(quest, reminders, EventSource.QUICK_ADD));
        if (quest.getEndDate() != null) {
            Toast.makeText(this, R.string.quest_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.quest_saved_to_inbox, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @OnClick(R.id.cancel)
    public void onCancel(View v) {
        finish();
    }
}
