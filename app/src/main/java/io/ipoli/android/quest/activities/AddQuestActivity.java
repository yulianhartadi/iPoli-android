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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.HashMap;
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
import io.ipoli.android.quest.AddQuestSuggestion;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.QuestPartType;
import io.ipoli.android.quest.SuggestionAdapterManager;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;
import io.ipoli.android.quest.events.SuggestionsAdapterChangedEvent;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.MainMatcher;
import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.parsers.RecurrenceMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;
import io.ipoli.android.tutorial.Tutorial;
import io.ipoli.android.tutorial.TutorialItem;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestActivity extends BaseActivity implements TextWatcher {

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

    private ArrayAdapter<AddQuestSuggestion> adapter;

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
    private Map<QuestPartType, QuestTextMatcher> typeToMatcher;

    private Map<QuestPartType, QuestPart> typeToQuestPart = new HashMap<>();


    private SuggestionAdapterManager suggestionAdapterManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        appComponent().inject(this);
        suggestionAdapterManager = new SuggestionAdapterManager(this);

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

    }

    private void initMatchers() {
        durationMatcher = new DurationMatcher();
        startTimeMatcher = new StartTimeMatcher(parser);
        dueDateMatcher = new DueDateMatcher(parser);
        timesPerDayMatcher = new TimesPerDayMatcher();
        recurrenceMatcher = new RecurrenceMatcher();
        mainMatcher = new MainMatcher();
        typeToMatcher = new HashMap<QuestPartType, QuestTextMatcher>() {{
            put(QuestPartType.DURATION, durationMatcher);
            put(QuestPartType.START_TIME, startTimeMatcher);
            put(QuestPartType.DUE_DATE, dueDateMatcher);
            put(QuestPartType.TIMES_PER_DAY, timesPerDayMatcher);
            put(QuestPartType.RECURRENT, recurrenceMatcher);
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
        adapter = suggestionAdapterManager.getAdapter();
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        String text = s.toString();
        String originalText = s.toString();

        if (count == 0 && before == 1) {//delete
            QuestPartType typeToDelete = findQuestPartToDelete(start);

            if (typeToDelete != null) {
                deleteQuestPartAndUpdateText(originalText, typeToDelete);
                return;
            }
        }

        Map<QuestPartType, QuestPart> currParts = new HashMap<>();

        for (QuestPartType t : QuestPartType.getOrdered()) {
            text = match(t, text, originalText, currParts);
        }

        String matchedPreposition = mainMatcher.match(text);
        if (!TextUtils.isEmpty(matchedPreposition)) {
            text = text.replace(matchedPreposition, " ");
            suggestionAdapterManager.changeAdapterSuggestions(matchedPreposition);
        }

        updateParsedQuestParts(start, before, count, originalText, currParts);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        clearSpans(editable);

        for (QuestPart p : typeToQuestPart.values()) {
            if (p.isParsed) {
                markText(editable, p.text, R.color.md_purple_300);
            }
        }
    }

    private void clearSpans(Editable editable) {
        BackgroundColorSpan[] spansToRemove = editable.getSpans(0, editable.toString().length(), BackgroundColorSpan.class);
        for (int i = 0; i < spansToRemove.length; i++)
            editable.removeSpan(spansToRemove[i]);
    }

    private void markText(Editable text, String spanText, int colorRes) {
        spanText = spanText.trim();
        int startIdx = text.toString().indexOf(spanText);
        int endIdx = startIdx + spanText.length();
        text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, colorRes)), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private String match(QuestPartType type, String text, String originalText, Map<QuestPartType, QuestPart> currParts) {
        String matchedText = typeToMatcher.get(type).match(text);
        if (!TextUtils.isEmpty(matchedText)) {
            suggestionAdapterManager.markAdapterAsUsed(type);
            int i = originalText.indexOf(matchedText);
            currParts.put(type, new QuestPart(matchedText, i, typeToMatcher.get(type), type));
            text = text.replace(matchedText, " ");
        } else {
            suggestionAdapterManager.markAdapterAsNotUsed(type);
        }
        return text;
    }

    @Nullable
    private QuestPartType findQuestPartToDelete(int start) {
        QuestPartType typeToRemove = null;
        for (QuestPartType t : typeToQuestPart.keySet()) {
            QuestPart p = typeToQuestPart.get(t);
            if (p.containsIndex(start)) {
                typeToRemove = t;
                break;
            }
        }
        return typeToRemove;
    }

    private void deleteQuestPartAndUpdateText(String originalText, QuestPartType typeToDelete) {
        QuestPart p = typeToQuestPart.get(typeToDelete);
        typeToQuestPart.remove(typeToDelete);
        if (!TextUtils.isEmpty(p.text)) {
            String begin = originalText.substring(0, p.start).trim() + " ";
            String end = originalText.substring(p.end()).trim();
            if (!TextUtils.isEmpty(end)) {
                end = " " + end + " ";
            }
            questText.setText(begin + end);
            questText.setSelection(begin.length());
        }
    }

    private void updateParsedQuestParts(int start, int before, int count, String originalText, Map<QuestPartType, QuestPart> currParts) {
        if (currParts.size() < typeToQuestPart.size()) {
            for (QuestPartType t : typeToQuestPart.keySet()) {
                if (!currParts.containsKey(t)) {
                    QuestPart p = typeToQuestPart.get(t);
                    if (p.containsOrIsNextToIdx(start)) {
                        p.isParsed = false;
                        if (count == 1 && before == 0) { // add
                            if (p.start + p.length() >= originalText.length()) {
                                p.text = originalText.substring(p.start);
                            } else {
                                p.text = originalText.substring(p.start, p.start + p.length() + 1);
                            }
                        }

                        typeToQuestPart.put(t, p);
                    }
                } else {
                    typeToQuestPart.put(t, currParts.get(t));
                }
            }
        } else {
            typeToQuestPart = currParts;
        }
    }

    @Subscribe
    public void onAdapterItemClick(SuggestionAdapterItemClickEvent e) {
        QuestPartType at = suggestionAdapterManager.getAdapter().getType();
        int i = questText.getSelectionStart();
        String s = e.suggestionItem.text;
        String text = questText.getText().toString();
        String begin = text.substring(0, i);
        String end = text.substring(i);

        if (at != QuestPartType.MAIN) {
            begin = begin.trim();
            if (begin.endsWith(at.text)) {
                begin = begin.substring(0, begin.length() - at.text.length());
            }
        }

        if (typeToQuestPart.containsKey(at)) {
            QuestPart p = typeToQuestPart.get(at);
            if (p.containsOrIsNextToIdx(i)) {
                if (begin.endsWith(p.text)) {
                    begin = begin.substring(0, begin.length() - p.length());
                } else if (end.startsWith(p.text)) {
                    end = end.substring(p.length());
                }
            }
        }

        if (!begin.endsWith(" ")) {
            s = " " + s;
        }
        if (!end.startsWith(" ")) {
            s = s + " ";
        }
        questText.setText(begin + s + end);
        questText.setSelection(begin.length() + s.length());
    }

    @Subscribe
    public void onAdapterChanged(SuggestionsAdapterChangedEvent e) {
        questText.showDropDown();
    }

    public class QuestPart {
        String text;
        int start;
        boolean isParsed;
        QuestTextMatcher mather;
        QuestPartType type;

        public QuestPart(String text, int start, QuestTextMatcher mather, QuestPartType type) {
            this.text = text;
            this.start = start;
            this.mather = mather;
            this.type = type;
            isParsed = true;
        }

        public int length() {
            return text.length();
        }

        public int end() {
            return start + text.length() - 1;
        }

        public boolean containsIndex(int index) {
            return index >= start && index <= end();
        }

        public boolean containsOrIsNextToIdx(int index) {
            return containsIndex(index) || start - 1 == index || end() + 1 == index;
        }
    }
}
