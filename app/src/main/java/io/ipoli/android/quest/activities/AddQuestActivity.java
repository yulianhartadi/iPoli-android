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
public class AddQuestActivity extends BaseActivity implements TextWatcher, OnSuggestionsUpdatedListener, AddQuestAutocompleteTextView.OnSelectionChangedListener {

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
    private List<ParsedPart> parsedParts;

    boolean changeTextFromDropDown = false;
    boolean afterDelete = false;
    int selectionStartIdx = 0;

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
        questText.addOnSelectionChangedListener(this);
        questText.setTextIsSelectable(false);
        questText.requestFocus();
        selectionStartIdx = questText.getSelectionStart();

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
//        if (isDelete(count, after) && !isDelete) {
//            SuggestionsManager.TextViewProps props = suggestionsManager.onTextDeleted(s.toString(), start, count);
//            isDelete = true;
//            questText.setText(props.text);
//            questText.setSelection(props.selectionStartIdx);
//            isDelete = false;
//            afterDelete = true;
//
//        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        if (afterDelete) {
//            afterDelete = false;
//            return;
//        }
//
//        if (before < count && !changeTextFromDropDown) {
//            String insertedText = s.toString().substring(start, start + count);
//            if (!insertedText.equals(" ")) {
//                suggestionsManager.onTextInserted(start, count);
//            }
//        }
//
//        String text = s.toString();
//        String originalText = s.toString();
//
//        for (SuggestionType t : suggestionsManager.getUnusedTypes()) {
//            text = match(t, text, originalText);
//        }
//
//        parsedParts = suggestionsManager.onTextChange(originalText, start, before, count, changeTextFromDropDown);

        parsedParts = suggestionsManager.onTextChange(s.toString(), questText.getSelectionStart());
    }

    private boolean isDelete(int replacedLen, int newLen) {
        return newLen < replacedLen;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        colorParsedParts(editable);

    }

    private void colorParsedParts(Editable editable) {
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

    private String match(SuggestionType type, String text, String originalText) {
//        if (!typeToMatcher.containsKey(type)) {
//            return text;
//        }
//        String matchedText = typeToMatcher.get(type).match(text);
//        if (!TextUtils.isEmpty(matchedText)) {
//            int i = originalText.indexOf(matchedText);
////            suggestionsManager.changeCurrentSuggester(type, i, matchedText.length());
//            text = text.replace(matchedText, " ");
//        }
        return text;
    }

    @Subscribe
    public void onAdapterItemClick(SuggestionAdapterItemClickEvent e) {
        SuggestionDropDownItem suggestion = e.suggestionItem;
        String s = suggestion.text;
        String text = questText.getText().toString();
        int selectionStart = questText.getSelectionStart();
        int[] idxs = suggestionsManager.onSuggestionItemClick(selectionStart);


        String begin = text.substring(0, idxs[0]);
        String end = idxs[1] + 1 < text.length() ? text.substring(idxs[1] + 1).trim() : "";
        s = !begin.endsWith(" ") ? " " + s : s;
        s = s + " ";

        changeTextFromDropDown = true;
        questText.setText(begin + s + end);
        questText.setSelection(begin.length() + s.length());
        changeTextFromDropDown = false;
    }

    @Override
    public void onSuggestionsUpdated() {
        if (adapter != null) {
            adapter.setSuggestions(suggestionsManager.getSuggestions());
        }
    }


    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
//        if (Math.abs(selectionStartIdx - selStart) <= 1 || changeTextFromDropDown) {
//            selectionStartIdx = selStart;
//            return;
//        }
//
//        ParsedPart p = suggestionsManager.findNotPartialParsedPartContainingIdx(selStart);
//        if (p != null) {
//            questText.setSelection(p.startIdx);
//            selectionStartIdx = p.startIdx;
//        } else {
//            selectionStartIdx = selStart;
//        }
//        parsedParts = suggestionsManager.onCursorSelectionChanged(questText.getText().toString(), selectionStartIdx);
//        colorParsedParts(questText.getText());
    }
}
