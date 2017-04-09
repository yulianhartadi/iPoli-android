package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.parsers.DateTimeParser;
import io.ipoli.android.app.ui.CategoryView;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.adapters.SuggestionsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;
import io.ipoli.android.quest.events.SuggestionItemTapEvent;
import io.ipoli.android.quest.suggestions.OnSuggestionsUpdatedListener;
import io.ipoli.android.quest.suggestions.ParsedPart;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.SuggestionsManager;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/4/16.
 */
public class QuickAddActivity extends BaseActivity implements TextWatcher, OnSuggestionsUpdatedListener {
    private static final int SUGGESTION_ITEM_HEIGHT_DP = 40;
    private static final int MAX_VISIBLE_SUGGESTION_ITEMS = 4;

    @Inject
    LocalStorage localStorage;

    @BindView(R.id.quick_add_text)
    AddQuestAutocompleteTextView questText;

    @BindView(R.id.quest_category)
    CategoryView categoryView;

    private QuestParser questParser;

    private SuggestionsManager suggestionsManager;
    private int selectionStartIdx = 0;
    private SuggestionsAdapter adapter;

    @Inject
    DateTimeParser prettyTimeParser;

    private enum TextWatcherState {GUI_CHANGE, FROM_DELETE, AFTER_DELETE, FROM_DROP_DOWN}

    private TextWatcherState textWatcherState = TextWatcherState.GUI_CHANGE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_quick_add);
        ButterKnife.bind(this);
        App.getAppComponent(this).inject(this);
        questParser = new QuestParser(prettyTimeParser);
        String additionalText = getIntent().getStringExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT);

        suggestionsManager = SuggestionsManager.createForQuest(prettyTimeParser, shouldUse24HourFormat());
        suggestionsManager.setSuggestionsUpdatedListener(this);

        initSuggestions();
        questText.addTextChangedListener(this);
        questText.setShowSoftInputOnFocus(true);
        questText.requestFocus();
        questText.setText(additionalText);
        showKeyboard();
    }

    private void onQuestTextClicked() {
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
    }

    private void initSuggestions() {
        adapter = new SuggestionsAdapter(this, eventBus, suggestionsManager.getSuggestions());
        questText.setAdapter(adapter);
        questText.setThreshold(2);
    }

    private void colorParsedParts(List<ParsedPart> parsedParts) {
        Editable editable = questText.getText();
        clearSpans(editable);
        for (ParsedPart p : parsedParts) {
            int backgroundColor = p.isPartial ? R.color.md_red_A200 : R.color.md_white;
            int foregroundColor = p.isPartial ? R.color.md_white : R.color.md_blue_700;
            markText(editable, p.startIdx, p.endIdx, backgroundColor, foregroundColor);
        }
    }

    private void markText(Editable text, int startIdx, int endIdx, @ColorRes int backgroundColorRes, @ColorRes int foregroundColorRes) {
        text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, backgroundColorRes)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, foregroundColorRes)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    @OnClick(R.id.quick_add_text)
    public void onQuestTextClicked(View v) {
        onQuestTextClicked();
    }

    @OnClick(R.id.add)
    public void onAddQuest(View v) {
        String text = questText.getText().toString();
        if (StringUtils.isEmpty(text)) {
            Toast.makeText(this, R.string.quest_name_validation, Toast.LENGTH_LONG).show();
            return;
        }

        Reminder reminder = new Reminder(0);
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(reminder);

        Quest quest = questParser.parseQuest(text);
        if (quest == null) {
            Toast.makeText(this, R.string.quest_name_validation, Toast.LENGTH_LONG).show();
            return;
        }
        quest.setCategory(categoryView.getSelectedCategory().name());
        quest.setReminders(reminders);
        eventBus.post(new NewQuestEvent(quest, EventSource.QUICK_ADD));
        if (quest.getScheduled() != null) {
            Toast.makeText(this, R.string.quest_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.quest_moved_to_inbox, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @OnClick(R.id.cancel)
    public void onCancel(View v) {
        finish();
    }

    @OnEditorAction(R.id.quick_add_text)
    public boolean onAddEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            onAddQuest(v);
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

    private void setTransformedText(SuggestionsManager.TextTransformResult result, TextWatcherState state) {
        textWatcherState = state;
        selectionStartIdx = result.selectionIndex;
        questText.setText(result.text);
        questText.setSelection(result.selectionIndex);
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
    public void afterTextChanged(Editable s) {

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

    @Override
    public void onSuggestionsUpdated() {
        if (adapter != null) {
            if (suggestionsManager.getSuggestions().size() > MAX_VISIBLE_SUGGESTION_ITEMS) {
                questText.setDropDownHeight((int) ViewUtils.dpToPx(MAX_VISIBLE_SUGGESTION_ITEMS * SUGGESTION_ITEM_HEIGHT_DP, getResources()));
            } else {
                questText.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            adapter.setSuggestions(suggestionsManager.getSuggestions());
        }
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
}
