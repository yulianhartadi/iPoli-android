package io.ipoli.android.quest.activities;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import co.mobiwise.materialintro.shape.Focus;
import io.ipoli.android.BottomBarUtil;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.adapters.BaseSuggestionsAdapter;
import io.ipoli.android.quest.adapters.SuggestionsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.MainMatcher;
import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.parsers.RecurrenceMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.suggestions.OnSuggestionsUpdatedListener;
import io.ipoli.android.quest.suggestions.ParsedPart;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.SuggestionType;
import io.ipoli.android.quest.suggestions.SuggestionsManager;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;
import io.ipoli.android.tutorial.Tutorial;
import io.ipoli.android.tutorial.TutorialItem;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestActivity extends BaseActivity implements TextWatcher, OnSuggestionsUpdatedListener {

    @Inject
    Bus eventBus;

    @Bind(R.id.appbar)
    AppBarLayout appBar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quest_text)
    AddQuestAutocompleteTextView questText;

    @Bind(R.id.quest_context_name)
    TextView contextName;

    @Bind(R.id.quest_context_container)
    LinearLayout contextContainer;

    private BaseSuggestionsAdapter adapter;

    private final PrettyTimeParser parser = new PrettyTimeParser();

    @Inject
    QuestPersistenceService questPersistenceService;

    private QuestContext questContext;
    private BottomBar bottomBar;

    private DurationMatcher durationMatcher;
    private StartTimeMatcher startTimeMatcher;
    private DueDateMatcher dueDateMatcher;
    private TimesPerDayMatcher timesPerDayMatcher;
    private RecurrenceMatcher recurrenceMatcher;
    private MainMatcher mainMatcher;
    private Map<SuggestionType, QuestTextMatcher> typeToMatcher;
    private SuggestionsManager suggestionsManager;
//    private List<ParsedPart> parsedParts;

    boolean changeTextFromDropDown = false;
    boolean afterDelete = false;
    private int selectionStartIdx = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        appComponent().inject(this);
        suggestionsManager = new SuggestionsManager(parser);
        suggestionsManager.setSuggestionsUpdatedListener(this);

        initMatchers();

        questText.addTextChangedListener(this);
        questText.requestFocus();

        if (getIntent() != null && getIntent().getBooleanExtra(Constants.IS_TODAY_QUEST_EXTRA_KEY, false)) {
            questText.setText(" " + getString(R.string.add_quest_today));
        }
        initUI();

        bottomBar = BottomBarUtil.getBottomBar(this, R.id.root_container, R.id.quest_container, savedInstanceState, BottomBarUtil.ADD_QUEST_TAB_INDEX);
        initContextUI();
        Tutorial.getInstance(this).addItem(
                new TutorialItem.Builder(this)
                        .setState(Tutorial.State.TUTORIAL_ADD_QUEST)
                        .setTarget(questText)
                        .setFocusType(Focus.NORMAL)
                        .enableDotAnimation(false)
                        .dismissOnTouch(true)
                        .build());


        questText.setOnClickListener(v -> {
            int selStart = questText.getSelectionStart();
            String text = questText.getText().toString();
            int newSel = suggestionsManager.getSelectionIndex(text, selStart);
            if (newSel != selStart) {
                selectionStartIdx = newSel;
                questText.setSelection(selectionStartIdx);
                colorParsedParts(suggestionsManager.parse(text, 0));
            } else if (Math.abs(selStart - selectionStartIdx) > 1) {
                selectionStartIdx = selStart;
                questText.setSelection(selectionStartIdx);
                colorParsedParts(suggestionsManager.parse(text, selectionStartIdx));
            }
        });
    }

    private void initMatchers() {
        durationMatcher = new DurationMatcher();
        startTimeMatcher = new StartTimeMatcher(parser);
        dueDateMatcher = new DueDateMatcher(parser);
        timesPerDayMatcher = new TimesPerDayMatcher();
        recurrenceMatcher = new RecurrenceMatcher();
        mainMatcher = new MainMatcher();
        typeToMatcher = new HashMap<SuggestionType, QuestTextMatcher>() {{
            put(SuggestionType.DURATION, durationMatcher);
            put(SuggestionType.START_TIME, startTimeMatcher);
            put(SuggestionType.DUE_DATE, dueDateMatcher);
            put(SuggestionType.TIMES_PER_DAY, timesPerDayMatcher);
            put(SuggestionType.RECURRENT, recurrenceMatcher);
            put(SuggestionType.MAIN, mainMatcher);
        }};
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        bottomBar.onSaveInstanceState(outState);
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

    private void initUI() {
        adapter = new SuggestionsAdapter(this, eventBus, suggestionsManager.getSuggestions());
        questText.setAdapter(adapter);
        questText.setThreshold(1);
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
        bottomBar.mapColorForTab(BottomBarUtil.ADD_QUEST_TAB_INDEX, ContextCompat.getColor(this, ctx.resLightColor));
        bottomBar.selectTabAtPosition(BottomBarUtil.ADD_QUEST_TAB_INDEX, false);
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

    public void saveQuest() {
        String text = questText.getText().toString().trim();

        Quest q = new QuestParser(parser).parse(text);
        if (TextUtils.isEmpty(q.getName())) {
            Toast.makeText(this, "Please, add quest name", Toast.LENGTH_LONG).show();
            return;
        }
        eventBus.post(new NewQuestEvent(q.getName(), q.getStartMinute(), q.getDuration(), q.getEndDate(), questContext));
        Toast.makeText(this, R.string.quest_added, Toast.LENGTH_SHORT).show();
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

    boolean isDelete = false;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.d("beforeTextChanged", start + " " + count);
        if (isDelete(count, after) && !isDelete) {
//            Log.d("iPoli Typing", continiusTyping + "");
            SuggestionsManager.TextTransformResult result = suggestionsManager.deleteText(s.toString(), start);
            isDelete = true;

            questText.setText(result.text);
            questText.setSelection(result.selectionIndex);

            List<ParsedPart> parsedParts = suggestionsManager.onTextChange(result.text, result.selectionIndex);
            colorParsedParts(parsedParts);
            afterDelete = true;
            isDelete = false;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d("onTextChanged", start + " " + count);
        if (afterDelete || isDelete) {
            afterDelete = false;
            return;
        }

        List<ParsedPart> parsedParts = suggestionsManager.onTextChange(s.toString(), questText.getSelectionStart());
        colorParsedParts(parsedParts);
    }

    private boolean isDelete(int replacedLen, int newLen) {
        return newLen < replacedLen;
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void colorParsedParts(List<ParsedPart> parsedParts) {
        Editable editable = questText.getText();
        clearSpans(editable);
        for (ParsedPart p : parsedParts) {
            int color = p.isPartial ? R.color.md_yellow_200 : R.color.md_blue_200;
            markText(editable, p.startIdx, p.endIdx, color);
        }
    }

    private void clearSpans(Editable editable) {
        BackgroundColorSpan[] spansToRemove = editable.getSpans(0, editable.toString().length(), BackgroundColorSpan.class);
        for (int i = 0; i < spansToRemove.length; i++)
            editable.removeSpan(spansToRemove[i]);
    }

    private void markText(Editable text, int startIdx, int endIdx, int colorRes) {
        text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, colorRes)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Subscribe
    public void onAdapterItemClick(SuggestionAdapterItemClickEvent e) {
        SuggestionDropDownItem suggestion = e.suggestionItem;
        String s = suggestion.text;
        String text = questText.getText().toString();
        int selectionStart = questText.getSelectionStart();
        SuggestionsManager.TextTransformResult result = suggestionsManager.replace(text, s, selectionStart);
        questText.setText(result.text);
        questText.setSelection(result.selectionIndex);
    }

    @Override
    public void onSuggestionsUpdated() {
        if (adapter != null) {
            adapter.setSuggestions(suggestionsManager.getSuggestions());
        }
    }
}
