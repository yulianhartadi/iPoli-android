package io.ipoli.android.quest.activities;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.adapters.BaseSuggestionsAdapter;
import io.ipoli.android.quest.adapters.SuggestionsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.NewQuestContextChangedEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewQuestSavedEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.RepeatingQuestSavedEvent;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;
import io.ipoli.android.quest.events.SuggestionItemTapEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.suggestions.OnSuggestionsUpdatedListener;
import io.ipoli.android.quest.suggestions.ParsedPart;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.SuggestionsManager;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.DurationPickerFragment;
import io.ipoli.android.quest.ui.dialogs.RecurrencePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimesPerDayPickerFragment;
import io.ipoli.android.quest.ui.formatters.DateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.FrequencyTextFormatter;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;
import io.ipoli.android.quest.ui.formatters.TimesPerDayFormatter;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDay;
import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class AddQuestActivity extends BaseActivity implements TextWatcher, OnSuggestionsUpdatedListener,
        DatePickerFragment.OnDatePickedListener,
        RecurrencePickerFragment.OnRecurrencePickedListener,
        DurationPickerFragment.OnDurationPickedListener,
        TimesPerDayPickerFragment.OnTimesPerDayPickedListener,
        TimePickerFragment.OnTimePickedListener {

    @BindView(R.id.appbar)
    AppBarLayout appBar;

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

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.quest_info_container)
    ViewGroup infoContainer;

    @BindView(R.id.quest_end_date_value)
    TextView endDateText;

    @BindView(R.id.quest_start_time_value)
    TextView startTimeText;

    @BindView(R.id.quest_duration_value)
    TextView durationText;

    @BindView(R.id.quest_times_per_day_value)
    TextView timesPerDayText;

    @BindView(R.id.quest_repeat_pattern_value)
    TextView frequencyText;

    @BindView(R.id.quest_text_layout)
    TextInputLayout questTextLayout;

    private BaseSuggestionsAdapter adapter;

    private final PrettyTimeParser prettyTimeParser = new PrettyTimeParser();

    private QuestContext questContext;

    private SuggestionsManager suggestionsManager;
    private int selectionStartIdx = 0;
    private String rawText;

    enum TextWatcherState {GUI_CHANGE, FROM_DELETE, AFTER_DELETE, FROM_DROP_DOWN}

    enum InsertMode {SMART_ADD, EDIT}

    private TextWatcherState textWatcherState = TextWatcherState.GUI_CHANGE;

    private InsertMode insertMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        App.getAppComponent(this).inject(this);

        setSupportActionBar(toolbar);
        toolbarTitle.setText(R.string.title_activity_add_quest);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        changeState(InsertMode.SMART_ADD);

        suggestionsManager = new SuggestionsManager(prettyTimeParser);
        suggestionsManager.setSuggestionsUpdatedListener(this);

        questText.addTextChangedListener(this);

        questText.setShowSoftInputOnFocus(true);
        questText.requestFocus();

        initUI();
        initContextUI();
        populateTimesPerDay(1);
        populateDuration(Constants.QUEST_CALENDAR_EVENT_MIN_DURATION);
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

    private void changeState(InsertMode insertMode) {
        this.insertMode = insertMode;
        switch (insertMode) {
            case SMART_ADD:
                questTextLayout.setHint(getString(R.string.smart_add_hint));
                infoContainer.setVisibility(View.GONE);
                break;
            case EDIT:
                questText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                questTextLayout.setHint(getString(R.string.add_quest_name_hint));
                infoContainer.setVisibility(View.VISIBLE);
                questText.removeTextChangedListener(this);
                questText.setAdapter(null);
                break;
        }
        supportInvalidateOptionsMenu();
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
        changeContext(QuestContext.LEARNING);

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
        contextName.setText(StringUtils.capitalize(questContext.name()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_quest_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_save).setTitle(insertMode == InsertMode.SMART_ADD ? R.string.done : R.string.save);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                onSaveTap(EventSource.TOOLBAR);
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_add_quest, R.string.help_dialog_add_quest_title, "add_quest").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveTap(EventSource source) {
        if (insertMode == InsertMode.SMART_ADD) {
            changeState(InsertMode.EDIT);
            populateFormFromParser();
        } else {
            eventBus.post(new NewQuestSavedEvent(questText.getText().toString().trim(), source));
            saveQuest();
        }
    }

    private void populateFormFromParser() {
        QuestParser questParser = new QuestParser(prettyTimeParser);
        QuestParser.QuestParserResult result = questParser.parseText(questText.getText().toString());
        this.rawText = result.rawText;
        if (result.endDate == null) {
            populateEndDate(null);
        } else {
            populateEndDate(toStartOfDay(new LocalDate(result.endDate, DateTimeZone.UTC)));
        }
        populateStartTime(result.startMinute);
        populateDuration(Math.max(result.duration, Constants.QUEST_CALENDAR_EVENT_MIN_DURATION));
        populateTimesPerDay(result.timesPerDay);
        populateFrequency(result);

        questText.setText(result.name);
        questText.setSelection(result.name.length());
        questText.clearFocus();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void populateFrequency(QuestParser.QuestParserResult result) {
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstart(toStartOfDayUTC(LocalDate.now()));
        if (result.everyDayRecurrence != null) {
            recurrence.setRrule(result.everyDayRecurrence.toString());
            recurrence.setType(Recurrence.RecurrenceType.DAILY);
        } else if (result.dayOfWeekRecurrence != null) {
            recurrence.setRrule(result.dayOfWeekRecurrence.toString());
            recurrence.setType(Recurrence.RecurrenceType.WEEKLY);
        } else if (result.dayOfMonthRecurrence != null) {
            recurrence.setRrule(result.dayOfMonthRecurrence.toString());
            recurrence.setType(Recurrence.RecurrenceType.MONTHLY);
        } else {
            recurrence = null;
        }
        setFrequencyText(recurrence);
    }

    @OnClick(R.id.quest_end_date_container)
    public void onEndDateClick(View view) {
        Calendar c = Calendar.getInstance();
        DatePickerFragment f = DatePickerFragment.newInstance(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), this);
        f.show(this.getSupportFragmentManager());
    }

    @OnClick(R.id.quest_start_time_container)
    public void onStartTimeClick(View view) {
        TimePickerFragment f = TimePickerFragment.newInstance(this);
        f.show(this.getSupportFragmentManager());
    }

    @OnClick(R.id.quest_duration_container)
    public void onDurationClick(View view) {
        DurationPickerFragment durationPickerFragment;
        if (durationText.getTag() != null && (int) durationText.getTag() > 0) {
            durationPickerFragment = DurationPickerFragment.newInstance((int) durationText.getTag(), this);
        } else {
            durationPickerFragment = DurationPickerFragment.newInstance(this);
        }
        durationPickerFragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_times_per_day_container)
    public void onTimesPerDayClick(View view) {
        int timesPerDay = 1;
        if (timesPerDayText.getTag() != null && (int) timesPerDayText.getTag() > 0) {
            timesPerDay = (int) timesPerDayText.getTag();
        }
        TimesPerDayPickerFragment timesPerDayPickerFragment = TimesPerDayPickerFragment.newInstance(timesPerDay, this);
        timesPerDayPickerFragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_frequency_container)
    public void onFrequencyClick(View view) {
        RecurrencePickerFragment recurrencePickerFragment = RecurrencePickerFragment.newInstance(this, (Recurrence) frequencyText.getTag());
        recurrencePickerFragment.show(getSupportFragmentManager());
    }

    @Override
    public void onDatePicked(Date date) {
        if (date != null) {
            setFrequencyText(null);
        }
        populateEndDate(date);
    }

    @Override
    public void onTimePicked(Time time) {
        populateStartTime(time == null ? -1 : time.toMinutesAfterMidnight());
    }

    @Override
    public void onDurationPicked(int duration) {
        populateDuration(duration);
    }

    @Override
    public void onTimesPerDayPicked(int timesPerDay) {
        populateTimesPerDay(timesPerDay);
    }

    private void populateEndDate(Date date) {
        if (date != null) {
            setFrequencyText(null);
        }
        endDateText.setText(DateFormatter.format(date));
        endDateText.setTag(date);
    }

    private void populateStartTime(int startMinute) {
        Date time = startMinute >= 0 ? Time.of(startMinute).toDate() : null;
        if (time != null) {
            populateTimesPerDay(1);
        }
        startTimeText.setText(StartTimeFormatter.format(time));
        if (time != null) {
            startTimeText.setTag(startMinute);
        } else {
            startTimeText.setTag(null);
        }
    }

    private void populateDuration(int duration) {
        durationText.setText(DurationFormatter.formatReadable(duration));
        durationText.setTag(duration);
    }

    private void populateTimesPerDay(int timesPerDay) {
        if (timesPerDay > 1) {
            populateStartTime(-1);
        }
        timesPerDayText.setText(TimesPerDayFormatter.formatReadable(timesPerDay));
        timesPerDayText.setTag(timesPerDay);
    }

    @Override
    public void onRecurrencePicked(Recurrence recurrence) {
        setFrequencyText(recurrence);
    }

    private void setFrequencyText(Recurrence recurrence) {
        if (recurrence != null) {
            populateEndDate(null);
        }
        frequencyText.setText(FrequencyTextFormatter.formatReadable(recurrence));
        frequencyText.setTag(recurrence);
    }

    public void saveQuest() {
        String name = questText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please, add quest name", Toast.LENGTH_LONG).show();
            return;
        }
        if (isRepeatingQuest()) {
            RepeatingQuest rq = new RepeatingQuest(rawText);
            rq.setName(name);
            rq.setDuration((int) durationText.getTag());
            rq.setStartMinute(startTimeText.getTag() != null ? (int) startTimeText.getTag() : null);
            Recurrence recurrence = frequencyText.getTag() != null ? (Recurrence) frequencyText.getTag() : Recurrence.create();
            recurrence.setTimesPerDay((int) timesPerDayText.getTag());
            if (recurrence.getRrule() == null) {
                if (endDateText.getTag() != null) {
                    recurrence.setDtstart(toStartOfDayUTC(new LocalDate((Date) endDateText.getTag())));
                    recurrence.setDtend(toStartOfDayUTC(new LocalDate((Date) endDateText.getTag())));
                } else {
                    recurrence.setDtstart(null);
                    recurrence.setDtend(null);
                }
            }
            rq.setRecurrence(recurrence);
            rq.setContext(questContext.name());
            eventBus.post(new NewRepeatingQuestEvent(rq));
        } else {
            Quest q = new Quest(name);
            q.setRawText(rawText);
            q.setEndDate((Date) endDateText.getTag());
            q.setDuration((int) durationText.getTag());
            q.setStartMinute(startTimeText.getTag() != null ? (int) startTimeText.getTag() : null);
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
            q.setContext(questContext.name());
            eventBus.post(new NewQuestEvent(q));
        }
        finish();
    }

    private boolean isRepeatingQuest() {
        return frequencyText.getTag() != null || (int) timesPerDayText.getTag() > 1;
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        Toast.makeText(this, R.string.quest_added, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onRepeatingQuestSaved(RepeatingQuestSavedEvent e) {
        Toast.makeText(this, R.string.repeating_quest_added, Toast.LENGTH_SHORT).show();
    }

    private boolean hasStartTime(Quest q) {
        return q.getStartMinute() >= 0;
    }

    private boolean isQuestForThePast(Quest q) {
        return q.getEndDate() != null && new LocalDate(q.getEndDate(), DateTimeZone.UTC).isBefore(new LocalDate());
    }

    @OnEditorAction(R.id.quest_text)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            onSaveTap(EventSource.KEYBOARD);
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
            int backgroundColor = p.isPartial ? R.color.md_red_A200 : R.color.md_white;
            int foregroundColor = p.isPartial ? R.color.md_white : R.color.md_blue_700;
            markText(editable, p.startIdx, p.endIdx, backgroundColor, foregroundColor);
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

    private void markText(Editable text, int startIdx, int endIdx, @ColorRes int backgroundColorRes, @ColorRes int foregroundColorRes) {
        text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, backgroundColorRes)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, foregroundColorRes)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        appBar.setBackgroundColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, context.resDarkColor));
    }
}