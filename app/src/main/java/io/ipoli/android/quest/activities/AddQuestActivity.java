package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import co.mobiwise.materialintro.shape.Focus;
import io.ipoli.android.R;
import io.ipoli.android.Tutorial;
import io.ipoli.android.TutorialItem;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestActivity extends BaseActivity implements TextWatcher, AdapterView.OnItemClickListener {

    @Inject
    Bus eventBus;

    @Bind(R.id.add_quest_container)
    LinearLayout rootContainer;

    @Bind(R.id.quest_text)
    AddQuestAutocompleteTextView questText;

    @Bind(R.id.due_date)
    ImageButton dueDate;

    @Bind(R.id.start_time)
    ImageButton startTime;

    @Bind(R.id.duration)
    ImageButton duration;

    private ArrayAdapter<String> adapter;
    private String[] durationAutoCompletes;

    private String[] dueDateAutoCompletes;

    private String[] startTimeAutoCompletes;

    private final PrettyTimeParser parser = new PrettyTimeParser();

    @Inject
    QuestPersistenceService questPersistenceService;

    private List<String> questNameAutoCompletes;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        appComponent().inject(this);
        setFinishOnTouchOutside(false);

        dueDateAutoCompletes = getResources().getStringArray(R.array.due_date_auto_completes);
        startTimeAutoCompletes = getResources().getStringArray(R.array.start_time_auto_completes);
        durationAutoCompletes = getResources().getStringArray(R.array.duration_auto_completes);

        questNameAutoCompletes = createQuestNameAutoCompletes();

        questText.setOnItemClickListener(this);
        questText.addTextChangedListener(this);
        questText.requestFocus();
        resetUI();



        Tutorial.getInstance(this).addItem(
                new TutorialItem.Builder(this)
                        .setState(Tutorial.State.TUTORIAL_ADD_QUEST)
                        .setTarget(getWindow().getDecorView())
                        .setFocusType(Focus.MINIMUM)
                        .enableDotAnimation(false)
                        .dismissOnTouch(true)
                        .build());

    }

    private List<String> createQuestNameAutoCompletes() {
        List<Quest> completedQuests = questPersistenceService.findAllCompleted();
        Set<String> names = new HashSet<>();
        for (Quest q : completedQuests) {
            names.add(q.getName());
        }
        return new ArrayList<>(names);
    }

    private void resetUI() {
        resetButtons();
        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, questNameAutoCompletes);
        questText.setAdapter(adapter);
        questText.setThreshold(1);
    }

    private void resetButtons() {
        duration.setBackgroundResource(R.drawable.circle_disable);
        startTime.setBackgroundResource(R.drawable.circle_disable);
        dueDate.setBackgroundResource(R.drawable.circle_disable);
        duration.setEnabled(false);
        startTime.setEnabled(false);
        dueDate.setEnabled(false);
    }

    @OnClick(R.id.due_date)
    public void onDueDateClick(View v) {
        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, dueDateAutoCompletes);
        questText.setAdapter(adapter);
        questText.setThreshold(1);
        questText.showDropDown();
    }

    @OnClick(R.id.start_time)
    public void onStartTimeClick(View v) {
        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, startTimeAutoCompletes);
        questText.setAdapter(adapter);
        questText.setThreshold(1);
        questText.showDropDown();
    }

    @OnClick(R.id.duration)
    public void onDurationClick(View v) {
        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, durationAutoCompletes);
        questText.setAdapter(adapter);
        questText.setThreshold(1);
        questText.showDropDown();
    }

    @OnClick(R.id.save_quest)
    public void onSaveQuestClick(View v) {
        String text = questText.getText().toString().trim();

        Quest q = new QuestParser(parser).parse(text);
        if (TextUtils.isEmpty(q.getName())) {
            Toast.makeText(this, "Please, add quest name", Toast.LENGTH_LONG).show();
            return;
        }
        eventBus.post(new NewQuestEvent(q.getName(), q.getStartTime(), q.getDuration(), q.getDue()));
        Toast.makeText(this, R.string.quest_added, Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(0, R.anim.slide_down_interpolate);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String selectedText = adapter.getItem(position);
        if (questNameAutoCompletes.contains(selectedText)) {
            questText.setText(selectedText);
        } else {
            String text = this.questText.getText().toString();
            if (!text.endsWith(" ")) {
                selectedText = " " + selectedText;
            }
            questText.append(selectedText);
            questText.setThreshold(1000);
        }
        questText.setSelection(this.questText.getText().length());
    }

    @OnEditorAction(R.id.quest_text)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            onSaveQuestClick(v);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_down_interpolate);
    }

    @OnClick(R.id.cancel_save)
    public void onCancelClick(View v) {
        finish();
        overridePendingTransition(0, R.anim.slide_down_interpolate);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String text = editable.toString();

        if (text.replaceAll(" ", "").length() < 3) {
            resetUI();
            return;
        }

        String matchedDuration = new DurationMatcher().match(text);
        if (!TextUtils.isEmpty(matchedDuration)) {
            text = text.replace(matchedDuration, " ");
            duration.setBackgroundResource(R.drawable.circle_disable);
            duration.setEnabled(false);
        } else {
            duration.setBackgroundResource(R.drawable.circle_normal);
            duration.setEnabled(true);
        }

        String matchedStartTime = new StartTimeMatcher(parser).match(text);
        if (!TextUtils.isEmpty(matchedStartTime)) {
            text = text.replace(matchedStartTime, " ");
            startTime.setBackgroundResource(R.drawable.circle_disable);
            startTime.setEnabled(false);
        } else {
            startTime.setBackgroundResource(R.drawable.circle_normal);
            startTime.setEnabled(true);
        }

        String matchedDueDate = new DueDateMatcher(parser).match(text);
        if (!TextUtils.isEmpty(matchedDueDate)) {
            dueDate.setBackgroundResource(R.drawable.circle_disable);
            dueDate.setEnabled(false);
        } else {
            dueDate.setBackgroundResource(R.drawable.circle_normal);
            dueDate.setEnabled(true);
        }

        if (TextUtils.isEmpty(matchedDueDate)) {
            dueDate.setBackgroundResource(R.drawable.circle_accent);
        } else if (TextUtils.isEmpty(matchedStartTime)) {
            startTime.setBackgroundResource(R.drawable.circle_accent);
        } else if (TextUtils.isEmpty(matchedDuration)) {
            duration.setBackgroundResource(R.drawable.circle_accent);
        }
    }
}
