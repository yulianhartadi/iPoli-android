package io.ipoli.android.quest.activities;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
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
import io.ipoli.android.BottomBarUtil;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;
import io.ipoli.android.tutorial.Tutorial;
import io.ipoli.android.tutorial.TutorialItem;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestActivity extends BaseActivity implements TextWatcher, AdapterView.OnItemClickListener {

    @Inject
    Bus eventBus;

    @Bind(R.id.appbar)
    AppBarLayout appBar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quest_text)
    AddQuestAutocompleteTextView questText;

    @Bind(R.id.due_date)
    ImageButton dueDate;

    @Bind(R.id.start_time)
    ImageButton startTime;

    @Bind(R.id.duration)
    ImageButton duration;

    @Bind(R.id.quest_context_name)
    TextView contextName;

    @Bind(R.id.quest_context_container)
    LinearLayout contextContainer;

    private ArrayAdapter<String> adapter;
    private String[] durationAutoCompletes;

    private String[] dueDateAutoCompletes;

    private String[] startTimeAutoCompletes;

    private final PrettyTimeParser parser = new PrettyTimeParser();

    @Inject
    QuestPersistenceService questPersistenceService;

    private List<String> questNameAutoCompletes;

    private QuestContext questContext;
    private BottomBar bottomBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);
        bottomBar = BottomBarUtil.getBottomBar(this, savedInstanceState, BottomBarUtil.ADD_QUEST_TAB_INDEX);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        appComponent().inject(this);

        dueDateAutoCompletes = getResources().getStringArray(R.array.due_date_auto_completes);
        startTimeAutoCompletes = getResources().getStringArray(R.array.start_time_auto_completes);
        durationAutoCompletes = getResources().getStringArray(R.array.duration_auto_completes);

        questNameAutoCompletes = createQuestNameAutoCompletes();

        questText.setOnItemClickListener(this);
        questText.addTextChangedListener(this);
        questText.requestFocus();

        if(getIntent() != null && getIntent().getBooleanExtra(Constants.IS_TODAY_QUEST_EXTRA_KEY, false)) {
            questText.setText(" " + getString(R.string.add_quest_today));
        }
        resetUI();

        initContextUI();

        Tutorial.getInstance(this).addItem(
                new TutorialItem.Builder(this)
                        .setState(Tutorial.State.TUTORIAL_ADD_QUEST)
                        .setTarget(questText)
                        .setFocusType(Focus.NORMAL)
                        .enableDotAnimation(false)
                        .dismissOnTouch(true)
                        .build());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        bottomBar.onSaveInstanceState(outState);
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

    private void initContextUI() {
        changeContext(QuestContext.PERSONAL);

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
        questContext = ctx;
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
        String ctxId = "quest_context_" + questContext.name().toLowerCase();
        int ctxResId = getResources().getIdentifier(ctxId, "id", getPackageName());
        return (ImageView) findViewById(ctxResId);
    }

    private void setBackgroundColors(QuestContext ctx) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, ctx.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, ctx.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, ctx.resDarkColor));
    }

    private void setContextName() {
        contextName.setText(questContext.name().substring(0, 1) + questContext.name().substring(1).toLowerCase());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_quest_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                saveQuest();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void saveQuest() {
        String text = questText.getText().toString().trim();

        Quest q = new QuestParser(parser).parse(text);
        if (TextUtils.isEmpty(q.getName())) {
            Toast.makeText(this, "Please, add quest name", Toast.LENGTH_LONG).show();
            return;
        }
        eventBus.post(new NewQuestEvent(q.getName(), q.getStartTime(), q.getDuration(), q.getDue(), questContext));
        Toast.makeText(this, R.string.quest_added, Toast.LENGTH_SHORT).show();
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
            saveQuest();
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
