package io.ipoli.android.quest.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.NetworkConnectionChangedEvent;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.CategoryView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.adapters.BaseSuggestionsAdapter;
import io.ipoli.android.quest.adapters.SuggestionsAdapter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CancelDeleteQuestEvent;
import io.ipoli.android.quest.events.ChallengePickedEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestCategoryChangedEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewQuestSavedEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.QuestDatePickedEvent;
import io.ipoli.android.quest.events.QuestDurationPickedEvent;
import io.ipoli.android.quest.events.QuestNodePickedEvent;
import io.ipoli.android.quest.events.QuestRecurrencePickedEvent;
import io.ipoli.android.quest.events.QuestStartTimePickedEvent;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;
import io.ipoli.android.quest.events.SuggestionItemTapEvent;
import io.ipoli.android.quest.events.UndoDeleteRepeatingQuestEvent;
import io.ipoli.android.quest.events.UpdateQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.suggestions.OnSuggestionsUpdatedListener;
import io.ipoli.android.quest.suggestions.ParsedPart;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.SuggestionsManager;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;
import io.ipoli.android.quest.ui.dialogs.ChallengePickerFragment;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.DurationPickerFragment;
import io.ipoli.android.quest.ui.dialogs.EditReminderFragment;
import io.ipoli.android.quest.ui.dialogs.RecurrencePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TextPickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;
import io.ipoli.android.quest.ui.events.QuestReminderPickedEvent;
import io.ipoli.android.quest.ui.events.UpdateRepeatingQuestEvent;
import io.ipoli.android.quest.ui.formatters.DateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.FrequencyTextFormatter;
import io.ipoli.android.quest.ui.formatters.ReminderTimeFormatter;
import io.ipoli.android.reminders.ReminderMinutesParser;
import io.ipoli.android.reminders.TimeOffsetType;
import io.ipoli.android.reminders.data.Reminder;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDay;
import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class EditQuestActivity extends BaseActivity implements TextWatcher, OnSuggestionsUpdatedListener,
        DatePickerFragment.OnDatePickedListener,
        RecurrencePickerFragment.OnRecurrencePickedListener,
        DurationPickerFragment.OnDurationPickedListener,
        TimePickerFragment.OnTimePickedListener,
        TextPickerFragment.OnTextPickedListener,
        ChallengePickerFragment.OnChallengePickedListener,
        CategoryView.OnCategoryChangedListener {

    @Inject
    Bus eventBus;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.quest_text)
    AddQuestAutocompleteTextView questText;

    @BindView(R.id.quest_category)
    CategoryView categoryView;

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

    @BindView(R.id.quest_repeat_pattern_value)
    TextView frequencyText;

    @BindView(R.id.quest_challenge_value)
    TextView challengeValue;

    @BindView(R.id.quest_note_value)
    TextView noteText;

    @BindView(R.id.quest_text_layout)
    TextInputLayout questTextLayout;

    @BindView(R.id.quest_reminders_container)
    ViewGroup remindersContainer;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    private BaseSuggestionsAdapter adapter;

    private final PrettyTimeParser prettyTimeParser = new PrettyTimeParser();

    private SuggestionsManager suggestionsManager;
    private int selectionStartIdx = 0;
    private String rawText;

    enum TextWatcherState {GUI_CHANGE, FROM_DELETE, AFTER_DELETE, FROM_DROP_DOWN}

    enum EditMode {ADD, EDIT_NEW_QUEST, EDIT_QUEST, EDIT_REPEATING_QUEST}

    private TextWatcherState textWatcherState = TextWatcherState.GUI_CHANGE;

    private EditMode editMode;

    private int notificationId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quest);
        ButterKnife.bind(this);
        App.getAppComponent(this).inject(this);

        setSupportActionBar(toolbar);
        toolbarTitle.setText(R.string.title_activity_add_quest);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        categoryView.addCategoryChangedListener(this);

        if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY))) {
            onEditQuest();
        } else if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY))) {
            onEditRepeatingQuest();
        } else {
            onAddNewQuest();
        }
    }

    private void onEditQuest() {
        changeEditMode(EditMode.EDIT_QUEST);
        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);

        questPersistenceService.findById(questId, quest -> {
            questText.setText(quest.getName());
            questText.setSelection(quest.getName().length());
            populateDuration(quest.getDuration());
            populateStartTime(quest.getStartMinute());
            if (quest.getEndDate() != null) {
                populateEndDate(toStartOfDay(new LocalDate(quest.getEndDate(), DateTimeZone.UTC)));
            } else {
                populateEndDate(null);
            }
            categoryView.changeCategory(Quest.getCategory(quest));
            populateNoteText(quest.getNote());
            challengePersistenceService.findById(quest.getChallengeId(), this::populateChallenge);

            if (quest.getReminders() == null || quest.getReminders().isEmpty()) {
                notificationId = new Random().nextInt();
            } else {
                notificationId = quest.getReminders().get(0).getNotificationId();
                for (Reminder reminder : quest.getReminders()) {
                    addReminder(reminder);
                }
            }
        });
    }

    private void onEditRepeatingQuest() {
        changeEditMode(EditMode.EDIT_REPEATING_QUEST);
        String questId = getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY);

        repeatingQuestPersistenceService.findById(questId, rq -> {
            questText.setText(rq.getName());
            questText.setSelection(rq.getName().length());
            populateDuration(rq.getDuration());
            if (rq.getStartMinute() >= 0) {
                populateStartTime(rq.getStartMinute());
            }
            setFrequencyText(rq.getRecurrence());
            categoryView.changeCategory(RepeatingQuest.getCategory(rq));
            populateNoteText(rq.getNote());
            challengePersistenceService.findById(rq.getChallengeId(), this::populateChallenge);

            if (rq.getReminders().isEmpty()) {
                notificationId = new Random().nextInt();
            } else {
                notificationId = rq.getReminders().get(0).getNotificationId();
                for (Reminder reminder : rq.getReminders()) {
                    addReminder(reminder);
                }
            }
        });
    }

    private void onAddNewQuest() {
        changeEditMode(EditMode.ADD);
        populateDuration(Constants.QUEST_MIN_DURATION);
        populateNoteText(null);
        populateChallenge(null);
        notificationId = new Random().nextInt();
        addReminder(new Reminder(0, notificationId));
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

    private void changeEditMode(EditMode editMode) {
        this.editMode = editMode;
        switch (editMode) {
            case ADD:
                suggestionsManager = new SuggestionsManager(prettyTimeParser);
                suggestionsManager.setSuggestionsUpdatedListener(this);
                initSuggestions();
                questText.addTextChangedListener(this);
                questText.setShowSoftInputOnFocus(true);
                questText.requestFocus();
                questTextLayout.setHint(getString(R.string.smart_add_hint));
                infoContainer.setVisibility(View.GONE);
                showKeyboard();
                break;
            case EDIT_NEW_QUEST:
            case EDIT_QUEST:
            case EDIT_REPEATING_QUEST:
                questText.setOnClickListener(null);
                questText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                questTextLayout.setHint(getString(R.string.add_quest_name_hint));
                infoContainer.setVisibility(View.VISIBLE);
                questText.removeTextChangedListener(this);
                questText.setAdapter(null);
                break;
        }
        if (editMode == EditMode.EDIT_QUEST) {
            toolbarTitle.setText(R.string.title_edit_quest);
            findViewById(R.id.quest_frequency_container).setVisibility(View.GONE);
        }
        if (editMode == EditMode.EDIT_REPEATING_QUEST) {
            toolbarTitle.setText(R.string.title_edit_quest);
            findViewById(R.id.quest_end_date_container).setVisibility(View.GONE);
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

    private void initSuggestions() {
        adapter = new SuggestionsAdapter(this, eventBus, suggestionsManager.getSuggestions());
        questText.setAdapter(adapter);
        questText.setThreshold(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_quest_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_save).setTitle(editMode == EditMode.ADD ? R.string.done : R.string.save);
        menu.findItem(R.id.action_delete).setVisible(!(editMode == EditMode.ADD || editMode == EditMode.EDIT_NEW_QUEST));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                onSaveTap(EventSource.TOOLBAR);
                return true;
            case R.id.action_delete:
                AlertDialog d = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_delete_quest_title)).setMessage(getString(R.string.dialog_delete_quest_message)).create();
                if (editMode == EditMode.EDIT_QUEST) {
                    String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
                    questPersistenceService.findById(questId, quest -> {
                        d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_it), (dialogInterface, i) -> {
                            eventBus.post(new DeleteQuestRequestEvent(quest, EventSource.EDIT_QUEST));
                            Toast.makeText(this, R.string.quest_deleted, Toast.LENGTH_SHORT).show();
                            setResult(Constants.RESULT_REMOVED);
                            finish();
                        });
                        d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> {
                            eventBus.post(new CancelDeleteQuestEvent(quest, EventSource.EDIT_QUEST));
                        });
                        d.show();
                    });
                } else if (editMode == EditMode.EDIT_REPEATING_QUEST) {
                    String questId = getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY);
                    repeatingQuestPersistenceService.findById(questId, repeatingQuest -> {
                        d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_it), (dialogInterface, i) -> {
                            eventBus.post(new DeleteRepeatingQuestRequestEvent(repeatingQuest, EventSource.EDIT_QUEST));
                            Toast.makeText(this, R.string.repeating_quest_deleted, Toast.LENGTH_SHORT).show();
                            setResult(Constants.RESULT_REMOVED);
                            finish();
                        });
                        d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> {
                            eventBus.post(new UndoDeleteRepeatingQuestEvent(repeatingQuest, EventSource.EDIT_QUEST));
                        });
                        d.show();
                    });
                }
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_add_quest, R.string.help_dialog_add_quest_title, "add_quest").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveTap(EventSource source) {
        if (editMode == EditMode.ADD) {
            changeEditMode(EditMode.EDIT_NEW_QUEST);
            populateFormFromParser();
        } else if (editMode == EditMode.EDIT_NEW_QUEST) {
            eventBus.post(new NewQuestSavedEvent(questText.getText().toString().trim(), source));
            saveQuest();
        } else if (editMode == EditMode.EDIT_QUEST) {
            updateQuest(source);
        } else if (editMode == EditMode.EDIT_REPEATING_QUEST) {
            updateRepeatingQuest(source);
        }
    }

    private void updateQuest(EventSource source) {
        String name = questText.getText().toString().trim();
        if (isQuestNameInvalid(name)) {
            return;
        }
        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId, q -> {
            q.setName(name);
            q.setEndDateFromLocal((Date) endDateText.getTag());
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
                q.setCompletedAtDate(c.getTime());
                q.setCompletedAtMinute(completedAtMinute);
            }
            q.setCategory(categoryView.getSelectedCategory().name());
            q.setChallengeId((String) challengeValue.getTag());
            q.setNote((String) noteText.getTag());
            eventBus.post(new UpdateQuestEvent(q, getReminders(), source));
            if (q.getEndDate() != null) {
                Toast.makeText(this, R.string.quest_saved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.quest_saved_to_inbox, Toast.LENGTH_SHORT).show();
            }
            setResult(RESULT_OK);
            finish();
        });
    }

    private void updateRepeatingQuest(EventSource source) {
        String name = questText.getText().toString().trim();
        if (isQuestNameInvalid(name)) {
            return;
        }
        String questId = getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY);
        repeatingQuestPersistenceService.findById(questId, rq -> {
            rq.setName(name);
            rq.setDuration((int) durationText.getTag());
            rq.setStartMinute(startTimeText.getTag() != null ? (int) startTimeText.getTag() : null);
            rq.setRecurrence((Recurrence) frequencyText.getTag());
            rq.setCategory(categoryView.getSelectedCategory().name());
            rq.setChallengeId((String) challengeValue.getTag());
            rq.setNote((String) noteText.getTag());
            eventBus.post(new UpdateRepeatingQuestEvent(rq, getReminders(), source));
            Toast.makeText(this, R.string.repeating_quest_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private boolean isQuestNameInvalid(String name) {
        if (StringUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.quest_name_validation, Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private void populateFormFromParser() {
        QuestParser questParser = new QuestParser(prettyTimeParser);
        QuestParser.QuestParserResult result = questParser.parse(questText.getText().toString());
        this.rawText = result.rawText;
        if (result.endDate == null) {
            populateEndDate(null);
        } else {
            populateEndDate(toStartOfDay(new LocalDate(result.endDate, DateTimeZone.UTC)));
        }
        populateStartTime(result.startMinute);
        populateDuration(Math.max(result.duration, Constants.QUEST_MIN_DURATION));
        populateFrequency(result);

        questText.setText(result.name);
        questText.setSelection(result.name.length());
        questText.clearFocus();
        hideKeyboard();
    }

    private void populateFrequency(QuestParser.QuestParserResult result) {
        Recurrence recurrence = Recurrence.create();
        if (result.everyDayRecurrence != null) {
            recurrence.setRrule(result.everyDayRecurrence.toString());
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.DAILY);
        } else if (result.dayOfWeekRecurrence != null) {
            recurrence.setRrule(result.dayOfWeekRecurrence.toString());
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
        } else if (result.dayOfMonthRecurrence != null) {
            recurrence.setRrule(result.dayOfMonthRecurrence.toString());
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.MONTHLY);
        } else if (result.timesAWeek > 0) {
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
            recurrence.setFlexibleCount(result.timesAWeek);
            Recur recur = new Recur(Recur.WEEKLY, null);
            recurrence.setRrule(recur.toString());
        } else if (result.timesAMonth > 0) {
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.MONTHLY);
            recurrence.setFlexibleCount(result.timesAMonth);
            Recur recur = new Recur(Recur.MONTHLY, null);
            recurrence.setRrule(recur.toString());
        } else {
            recurrence = null;
        }
        setFrequencyText(recurrence);
    }

    @OnClick(R.id.quest_end_date_container)
    public void onEndDateClick(View view) {
        DatePickerFragment f = DatePickerFragment.newInstance((Date) endDateText.getTag(), this);
        f.show(this.getSupportFragmentManager());
    }

    @OnClick(R.id.quest_start_time_container)
    public void onStartTimeClick(View view) {
        Time time = Time.now();
        if (startTimeText.getTag() != null && (int) startTimeText.getTag() > -1) {
            time = Time.of((int) startTimeText.getTag());
        }
        TimePickerFragment f = TimePickerFragment.newInstance(time, this);
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

    @OnClick(R.id.quest_frequency_container)
    public void onFrequencyClick(View view) {
        boolean disableNoRepeat = editMode == EditMode.EDIT_REPEATING_QUEST;
        RecurrencePickerFragment recurrencePickerFragment = RecurrencePickerFragment.newInstance(disableNoRepeat, this, (Recurrence) frequencyText.getTag());
        recurrencePickerFragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_add_reminder_container)
    public void onRemindersClicked(View view) {
        EditReminderFragment f = EditReminderFragment.newInstance(notificationId, (reminder, editMode) -> {
            if (reminder != null) {
                addReminder(reminder);
            }
            eventBus.post(new QuestReminderPickedEvent(reminder, editMode.name(), this.editMode.name()));
        });
        f.show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_challenge_container)
    public void onChallengeClick(View view) {
        ChallengePickerFragment.newInstance((String) challengeValue.getTag(), this).show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_note_container)
    public void onNoteClick(View view) {
        TextPickerFragment.newInstance((String) noteText.getTag(), R.string.pick_note_title, this).show(getSupportFragmentManager());
    }

    @Override
    public void onDatePicked(Date date) {
        if (date != null) {
            setFrequencyText(null);
        }
        populateEndDate(date);
        eventBus.post(new QuestDatePickedEvent(editMode.name().toLowerCase()));
    }


    @Override
    public void onTextPicked(String text) {
        populateNoteText(text);
        eventBus.post(new QuestNodePickedEvent(editMode.name().toLowerCase()));
    }

    private void populateNoteText(String text) {
        noteText.setTag(text);
        if (StringUtils.isEmpty(text)) {
            noteText.setText(R.string.none);
        } else {
            noteText.setText(text);
        }
    }

    @Override
    public void onTimePicked(Time time) {
        populateStartTime(time == null ? -1 : time.toMinutesAfterMidnight());
        eventBus.post(new QuestStartTimePickedEvent(editMode.name().toLowerCase()));
    }

    @Override
    public void onDurationPicked(int duration) {
        populateDuration(duration);
        eventBus.post(new QuestDurationPickedEvent(editMode.name().toLowerCase()));
    }

    @Override
    public void onChallengePicked(String challengeId) {
        challengePersistenceService.findById(challengeId, challenge -> {
            populateChallenge(challenge);
            String name = challenge != null ? challenge.getName() : getString(R.string.none);
            eventBus.post(new ChallengePickedEvent(editMode.name().toLowerCase(), name));
        });
    }

    private void populateChallenge(Challenge challenge) {
        if (challenge != null) {
            challengeValue.setText(challenge.getName());
            challengeValue.setTag(challenge.getId());
        } else {
            challengeValue.setText(R.string.none);
            challengeValue.setTag(null);
        }
    }

    private void populateEndDate(Date date) {
        if (date != null) {
            setFrequencyText(null);
        }
        endDateText.setText(DateFormatter.format(date));
        endDateText.setTag(date);
    }

    private void populateStartTime(int startMinute) {
        if (startMinute >= 0) {
            if (frequencyText.getTag() != null) {
                ((Recurrence) frequencyText.getTag()).setTimesADay(1);
            }
            startTimeText.setText(Time.of(startMinute).toString());
            startTimeText.setTag(startMinute);
        } else {
            startTimeText.setText(R.string.do_not_know);
            startTimeText.setTag(null);
        }
    }

    private void populateDuration(int duration) {
        durationText.setText(DurationFormatter.formatReadable(duration));
        durationText.setTag(duration);
    }

    private void addReminder(Reminder reminder) {
        if (reminderWithSameTimeExists(reminder)) {
            return;
        }
        View v = getLayoutInflater().inflate(R.layout.quest_reminder_item, remindersContainer, false);
        populateReminder(reminder, v);
        remindersContainer.addView(v);

        v.setOnClickListener(view -> {
            EditReminderFragment f = EditReminderFragment.newInstance((Reminder) v.getTag(), (editedReminder, mode) -> {
                if (editedReminder == null || reminderWithSameTimeExists(editedReminder)) {
                    remindersContainer.removeView(v);
                    return;
                }
                populateReminder(editedReminder, v);
                eventBus.post(new QuestReminderPickedEvent(editedReminder, editMode.name(), this.editMode.name()));
            });
            f.show(getSupportFragmentManager());
        });
    }

    private boolean reminderWithSameTimeExists(Reminder reminder) {
        for (Reminder r : getReminders()) {
            if (reminder.getMinutesFromStart() == r.getMinutesFromStart()) {
                return true;
            }
        }
        return false;
    }

    private void populateReminder(Reminder reminder, View reminderView) {
        String text = "";
        Pair<Long, TimeOffsetType> parsedResult = ReminderMinutesParser.parseCustomMinutes(Math.abs(reminder.getMinutesFromStart()));
        if (parsedResult != null) {
            text = ReminderTimeFormatter.formatTimeOffset(parsedResult.first, parsedResult.second);
        }
        ((TextView) reminderView.findViewById(R.id.reminder_text)).setText(text);
        reminderView.setTag(reminder);
    }

    private List<Reminder> getReminders() {
        List<Reminder> reminders = new ArrayList<>();
        for (int i = 0; i < remindersContainer.getChildCount(); i++) {
            reminders.add((Reminder) remindersContainer.getChildAt(i).getTag());
        }
        return reminders;
    }

    @Override
    public void onRecurrencePicked(Recurrence recurrence) {
        setFrequencyText(recurrence);
        eventBus.post(new QuestRecurrencePickedEvent(editMode.name().toLowerCase()));
    }

    private void setFrequencyText(Recurrence recurrence) {
        if (recurrence != null) {
            populateEndDate(null);
            if (recurrence.getTimesADay() > 1) {
                populateStartTime(-1);
            }
        }
        frequencyText.setText(FrequencyTextFormatter.formatReadable(recurrence));
        frequencyText.setTag(recurrence);
    }

    public void saveQuest() {
        String name = questText.getText().toString().trim();
        if (isQuestNameInvalid(name)) {
            return;
        }
        if (isRepeatingQuest()) {
            createRepeatingQuest(name);
        } else {
            createQuest(name);
        }
        setResult(RESULT_OK);
        finish();
    }

    private void createQuest(String name) {
        Quest q = new Quest(name);
        q.setRawText(rawText);
        q.setEndDateFromLocal((Date) endDateText.getTag());
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
            q.setCompletedAtDate(c.getTime());
            q.setCompletedAtMinute(completedAtMinute);
        }
        q.setCategory(categoryView.getSelectedCategory().name());
        q.setNote((String) noteText.getTag());
        q.setChallengeId((String) challengeValue.getTag());

        eventBus.post(new NewQuestEvent(q, getReminders(), EventSource.EDIT_QUEST));
        if (q.getEndDate() != null) {
            Toast.makeText(this, R.string.quest_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.quest_saved_to_inbox, Toast.LENGTH_SHORT).show();
        }
    }

    private void createRepeatingQuest(String name) {
        RepeatingQuest rq = new RepeatingQuest(rawText);
        rq.setName(name);
        rq.setDuration((int) durationText.getTag());
        rq.setStartMinute(startTimeText.getTag() != null ? (int) startTimeText.getTag() : null);
        Recurrence recurrence = frequencyText.getTag() != null ? (Recurrence) frequencyText.getTag() : Recurrence.create();
        recurrence.setDtstartDate(toStartOfDayUTC(LocalDate.now()));
        if (recurrence.getRrule() == null) {
            if (endDateText.getTag() != null) {
                recurrence.setDtstartDate(toStartOfDayUTC(new LocalDate((Date) endDateText.getTag())));
                recurrence.setDtendDate(toStartOfDayUTC(new LocalDate((Date) endDateText.getTag())));
            } else {
                recurrence.setDtstartDate(null);
                recurrence.setDtend(null);
            }
        }
        rq.setRecurrence(recurrence);
        rq.setCategory(categoryView.getSelectedCategory().name());
        rq.setChallengeId((String) challengeValue.getTag());
        rq.setNote((String) noteText.getTag());
        eventBus.post(new NewRepeatingQuestEvent(rq, getReminders()));
        Toast.makeText(this, R.string.repeating_quest_saved, Toast.LENGTH_SHORT).show();
    }

    private boolean isRepeatingQuest() {
        return frequencyText.getTag() != null;
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

    @Override
    public void onCategoryChanged(Category category) {
        colorLayout(category);
        if (editMode == EditMode.EDIT_NEW_QUEST) {
            eventBus.post(new NewQuestCategoryChangedEvent(category));
        }
    }

    private void colorLayout(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
    }

    @Subscribe
    public void onNetworkChanged(NetworkConnectionChangedEvent e) {
        if(!e.hasInternet) {
            showNoInternetActivity();
        }
    }

}