package io.ipoli.android.quest.activities;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.adapters.BaseSuggestionsAdapter;
import io.ipoli.android.quest.adapters.SuggestionsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.NewQuestContextChangedEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewQuestSavedEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;
import io.ipoli.android.quest.events.SuggestionItemTapEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.suggestions.OnSuggestionsUpdatedListener;
import io.ipoli.android.quest.suggestions.ParsedPart;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.SuggestionsManager;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class AddQuestActivity extends BaseActivity implements TextWatcher, OnSuggestionsUpdatedListener {

    @Inject
    Bus eventBus;

    @BindView(R.id.quest_text)
    AddQuestAutocompleteTextView questText;

    @BindView(R.id.quest_context_name)
    TextView contextName;

    @BindView(R.id.quest_context_container)
    LinearLayout contextContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private BaseSuggestionsAdapter adapter;

    private final PrettyTimeParser parser = new PrettyTimeParser();

    private QuestContext questContext;

    private SuggestionsManager suggestionsManager;
    private int selectionStartIdx = 0;

    enum TextWatcherState {GUI_CHANGE, FROM_DELETE, AFTER_DELETE, FROM_DROP_DOWN;}

    TextWatcherState textWatcherState = TextWatcherState.GUI_CHANGE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        App.getAppComponent(this).inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        suggestionsManager = new SuggestionsManager(parser);
        suggestionsManager.setSuggestionsUpdatedListener(this);

        questText.addTextChangedListener(this);

        questText.setShowSoftInputOnFocus(true);
        questText.requestFocus();

        initUI();
        initContextUI();

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
            iv.setOnClickListener(v -> {
                removeSelectedContextCheck();
                changeContext(ctx);
                eventBus.post(new NewQuestContextChangedEvent(ctx));
            });
        }
    }

    private void changeContext(QuestContext ctx) {
        colorLayout(ctx);
        questContext = ctx;
        setSelectedContext();
    }

    private void setSelectedContext() {
        getCurrentContextImageView().setImageResource(questContext.whiteImage);
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

    private void setContextName() {
        String name = questContext.name();
        contextName.setText(Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_quest_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                eventBus.post(new NewQuestSavedEvent(questText.getText().toString().trim(), EventSource.TOOLBAR));
                saveQuest();
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_add_quest, R.string.help_dialog_add_quest_title, "add_quest").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveQuest() {
        String text = questText.getText().toString().trim();

        QuestParser qParser = new QuestParser(parser);
        if (qParser.isRepeatingQuest(text)) {
            RepeatingQuest repeatingQuest = qParser.parseRepeatingQuest(text);
            if (repeatingQuest == null) {
                resetQuestText();
                Toast.makeText(this, "Please, add quest name", Toast.LENGTH_LONG).show();
                return;
            }
            repeatingQuest.setContext(questContext.name());
            eventBus.post(new NewRepeatingQuestEvent(repeatingQuest));
            if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
                Toast.makeText(this, R.string.no_internet_repeating_quest_added, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.repeating_quest_added, Toast.LENGTH_LONG).show();
            }
        } else {
            Quest q = qParser.parse(text);
            if (q == null) {
                resetQuestText();
                Toast.makeText(this, "Please, add quest name", Toast.LENGTH_LONG).show();
                return;
            }
            if (isQuestForThePast(q)) {
                Date completedAt = new LocalDate(q.getEndDate(), DateTimeZone.UTC).toDate();
                Calendar c = Calendar.getInstance();
                c.setTime(completedAt);

                int completedAtMinute = Time.now().toMinutesAfterMidnight();
                if (hasStartTime(q)) {
                    completedAtMinute = q.getStartMinute();
                }
                c.add(Calendar.MINUTE, completedAtMinute);
                q.setCompletedAt(c.getTime());
                q.setCompletedAtMinute(completedAtMinute);
            }
            Quest.setContext(q, questContext);
            eventBus.post(new NewQuestEvent(q));
        }
        resetQuestText();
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        Toast.makeText(this, R.string.quest_added, Toast.LENGTH_SHORT).show();
    }

    private boolean hasStartTime(Quest q) {
        return q.getStartMinute() >= 0;
    }

    private boolean isQuestForThePast(Quest q) {
        return q.getEndDate() != null && new LocalDate(q.getEndDate(), DateTimeZone.UTC).isBefore(new LocalDate());
    }

    private void resetQuestText() {
        suggestionsManager.setSuggestionsUpdatedListener(null);
        suggestionsManager = new SuggestionsManager(parser);
        suggestionsManager.setSuggestionsUpdatedListener(this);
        questText.removeTextChangedListener(this);
        questText.setText("");
        questText.addTextChangedListener(this);
    }

    @OnEditorAction(R.id.quest_text)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            eventBus.post(new NewQuestSavedEvent(questText.getText().toString().trim(), EventSource.KEYBOARD));
            saveQuest();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (isInsert(count, after) || textWatcherState == TextWatcherState.FROM_DELETE) {
            return;
        }

        SuggestionsManager.TextTransformResult result = suggestionsManager.deleteText(s.toString(), start);
        setTransformedText(result, TextWatcherState.FROM_DELETE);
        textWatcherState = TextWatcherState.AFTER_DELETE;

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        switch (textWatcherState) {
            case FROM_DELETE:
            case FROM_DROP_DOWN:
                List<ParsedPart> parsedParts = suggestionsManager.onTextChange(s.toString(), selectionStartIdx);
                colorParsedParts(parsedParts);
                break;

            case GUI_CHANGE:
                parsedParts = suggestionsManager.onTextChange(s.toString(), questText.getSelectionStart());
                colorParsedParts(parsedParts);
                break;
            case AFTER_DELETE:
                break;
        }

        textWatcherState = TextWatcherState.GUI_CHANGE;
    }

    private boolean isInsert(int replacedLen, int newLen) {
        return newLen >= replacedLen;
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void colorParsedParts(List<ParsedPart> parsedParts) {
        Editable editable = questText.getText();
        clearSpans(editable);
        for (ParsedPart p : parsedParts) {
            int color = p.isPartial ? R.color.md_red_A200 : R.color.md_blue_500;
            markText(editable, p.startIdx, p.endIdx, color);
        }
    }

    private void clearSpans(Editable editable) {
        BackgroundColorSpan[] backgroundSpansToRemove = editable.getSpans(0, editable.toString().length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : backgroundSpansToRemove) {
            editable.removeSpan(span);
        }
        ForegroundColorSpan[] foregroundSpansToRemove = editable.getSpans(0, editable.toString().length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : foregroundSpansToRemove) {
            editable.removeSpan(span);
        }
    }

    private void markText(Editable text, int startIdx, int endIdx, int colorRes) {
        text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, colorRes)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.md_white)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Subscribe
    public void onAdapterItemClick(SuggestionAdapterItemClickEvent e) {
        SuggestionDropDownItem suggestion = e.suggestionItem;
        String text = questText.getText().toString();
        eventBus.post(new SuggestionItemTapEvent(suggestion.visibleText, text));
        int selectionIndex = questText.getSelectionStart();
        SuggestionsManager.TextTransformResult result = suggestionsManager.onSuggestionItemClick(text, suggestion, selectionIndex);
        setTransformedText(result, TextWatcherState.FROM_DROP_DOWN);
        if (suggestion.nextTextEntityType != null) {
            List<ParsedPart> parsedParts = suggestionsManager.parse(result.text, result.selectionIndex);
            ParsedPart partialPart = suggestionsManager.findPartialPart(parsedParts);
            String parsedText = partialPart == null ? "" : StringUtils.substring(result.text, partialPart.startIdx, partialPart.endIdx);
            suggestionsManager.changeCurrentSuggestionsProvider(suggestion.nextTextEntityType, parsedText);
        }
    }

    private void setTransformedText(SuggestionsManager.TextTransformResult result, TextWatcherState state) {
        textWatcherState = state;
        selectionStartIdx = result.selectionIndex;
        questText.setText(result.text);
        questText.setSelection(result.selectionIndex);
    }

    @Override
    public void onSuggestionsUpdated() {
        if (adapter != null) {
            adapter.setSuggestions(suggestionsManager.getSuggestions());
        }
    }

    private void colorLayout(QuestContext context) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, context.resDarkColor));
    }
}
