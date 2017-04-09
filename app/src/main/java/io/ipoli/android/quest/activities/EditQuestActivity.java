package io.ipoli.android.quest.activities;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

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
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.ui.CategoryView;
import io.ipoli.android.app.ui.dialogs.DatePickerFragment;
import io.ipoli.android.app.ui.dialogs.TextPickerFragment;
import io.ipoli.android.app.ui.dialogs.TimePickerFragment;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.ui.formatters.FrequencyTextFormatter;
import io.ipoli.android.app.ui.formatters.PriorityFormatter;
import io.ipoli.android.app.ui.formatters.ReminderTimeFormatter;
import io.ipoli.android.app.ui.formatters.TimesADayFormatter;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.adapters.EditQuestSubQuestListAdapter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.CancelDeleteQuestEvent;
import io.ipoli.android.quest.events.ChallengePickedEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.events.QuestDatePickedEvent;
import io.ipoli.android.quest.events.QuestDurationPickedEvent;
import io.ipoli.android.quest.events.QuestNodePickedEvent;
import io.ipoli.android.quest.events.QuestPriorityPickedEvent;
import io.ipoli.android.quest.events.QuestRecurrencePickedEvent;
import io.ipoli.android.quest.events.QuestStartTimePickedEvent;
import io.ipoli.android.quest.events.UndoDeleteRepeatingQuestEvent;
import io.ipoli.android.quest.events.UpdateQuestEvent;
import io.ipoli.android.quest.events.subquests.AddSubQuestTappedEvent;
import io.ipoli.android.quest.events.subquests.NewSubQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.ui.dialogs.ChallengePickerFragment;
import io.ipoli.android.quest.ui.dialogs.DurationPickerFragment;
import io.ipoli.android.quest.ui.dialogs.EditReminderFragment;
import io.ipoli.android.quest.ui.dialogs.PriorityPickerFragment;
import io.ipoli.android.quest.ui.dialogs.RecurrencePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimesADayPickerFragment;
import io.ipoli.android.quest.ui.events.QuestReminderPickedEvent;
import io.ipoli.android.quest.ui.events.UpdateRepeatingQuestEvent;
import io.ipoli.android.reminder.ReminderMinutesParser;
import io.ipoli.android.reminder.TimeOffsetType;
import io.ipoli.android.reminder.data.Reminder;

import static io.ipoli.android.app.events.EventSource.EDIT_QUEST;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class EditQuestActivity extends BaseActivity implements
        DatePickerFragment.OnDatePickedListener,
        RecurrencePickerFragment.OnRecurrencePickedListener,
        DurationPickerFragment.OnDurationPickedListener,
        TimePickerFragment.OnTimePickedListener,
        TextPickerFragment.OnTextPickedListener,
        ChallengePickerFragment.OnChallengePickedListener,
        CategoryView.OnCategoryChangedListener, TimesADayPickerFragment.OnTimesADayPickedListener, PriorityPickerFragment.OnPriorityPickedListener {

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.quest_text)
    TextInputEditText questText;

    @BindView(R.id.quest_category)
    CategoryView categoryView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.quest_info_container)
    ViewGroup infoContainer;

    @BindView(R.id.quest_end_date_container)
    ViewGroup endDateContainer;

    @BindView(R.id.quest_frequency_container)
    ViewGroup frequencyContainer;

    @BindView(R.id.quest_challenge_container)
    ViewGroup challengeContainer;

    @BindView(R.id.quest_priority_container)
    ViewGroup priorityContainer;

    @BindView(R.id.quest_end_date_value)
    TextView endDateText;

    @BindView(R.id.quest_start_time_value)
    TextView startTimeText;

    @BindView(R.id.quest_duration_value)
    TextView durationText;

    @BindView(R.id.quest_repeat_pattern_value)
    TextView frequencyText;

    @BindView(R.id.quest_challenge_value)
    TextView challengeText;

    @BindView(R.id.quest_priority_value)
    TextView priorityText;

    @BindView(R.id.quest_note_value)
    TextView noteText;

    @BindView(R.id.quest_text_layout)
    TextInputLayout questTextLayout;

    @BindView(R.id.quest_reminders_container)
    ViewGroup remindersContainer;

    @BindView(R.id.quest_times_a_day_value)
    TextView timesADayText;

    @BindView(R.id.add_sub_quest)
    TextInputEditText addSubQuest;

    @BindView(R.id.sub_quests_container)
    RecyclerView subQuestsContainer;

    @BindView(R.id.add_sub_quest_clear)
    ImageButton clearAddSubQuest;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    private EditQuestSubQuestListAdapter subQuestListAdapter;

    enum EditMode {EDIT_QUEST, EDIT_REPEATING_QUEST}

    private EditMode editMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quest);
        ButterKnife.bind(this);
        App.getAppComponent(this).inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        categoryView.addCategoryChangedListener(this);
        initSubQuestsUI();

        if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY))) {
            onEditQuest();
        } else if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY))) {
            onEditRepeatingQuest();
        }
    }

    private void initSubQuestsUI() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        subQuestsContainer.setLayoutManager(layoutManager);

        subQuestListAdapter = new EditQuestSubQuestListAdapter(this, eventBus, new ArrayList<>());
        subQuestsContainer.setAdapter(subQuestListAdapter);

        hideUnderline(addSubQuest);

        addSubQuest.setOnFocusChangeListener((view, isFocused) -> {
            String text = addSubQuest.getText().toString();
            if (isFocused) {
                showUnderline(addSubQuest);
                if (text.equals(getString(R.string.edit_quest_add_sub_quest))) {
                    setAddSubQuestInEditMode();
                }
                addSubQuest.requestFocus();
                eventBus.post(new AddSubQuestTappedEvent(EDIT_QUEST));
            } else {
                hideUnderline(addSubQuest);
                if (StringUtils.isEmpty(text)) {
                    setAddSubQuestInViewMode();
                }
            }
        });
    }

    private void onEditQuest() {
        changeEditMode(EditMode.EDIT_QUEST);
        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);

        questPersistenceService.findById(questId, quest -> {
            questText.setText(quest.getName());
            questText.setSelection(quest.getName().length());
            populateDuration(quest.getDuration());
            populateStartTime(quest.getStartMinute());
            populateTimesADay(quest.getTimesADay());
            populatePriority(quest.getPriority());
            if (quest.getScheduled() != null) {
                populateEndDate(quest.getScheduledDate());
            } else {
                populateEndDate(null);
            }
            if (quest.isFromRepeatingQuest()) {
                challengeContainer.setClickable(false);
            }
            categoryView.changeCategory(quest.getCategoryType());
            List<Note> notes = quest.getTextNotes();
            populateNoteText(notes.isEmpty() ? null : notes.get(0).getText());
            subQuestListAdapter.setSubQuests(quest.getSubQuests());
            challengePersistenceService.findById(quest.getChallengeId(), this::populateChallenge);

            for (Reminder reminder : quest.getReminders()) {
                addReminder(reminder);
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
            populatePriority(rq.getPriority());
            populateTimesADay(rq.getTimesADay());
            populateStartTime(rq.getStartMinute());
            setFrequencyText(rq.getRecurrence());
            categoryView.changeCategory(rq.getCategoryType());
            List<Note> notes = rq.getTextNotes();
            populateNoteText(notes.isEmpty() ? null : notes.get(0).getText());
            subQuestListAdapter.setSubQuests(rq.getSubQuests());
            challengePersistenceService.findById(rq.getChallengeId(), this::populateChallenge);

            for (Reminder reminder : rq.getReminders()) {
                addReminder(reminder);
            }
        });
    }

    private void changeEditMode(EditMode editMode) {
        this.editMode = editMode;
        switch (editMode) {
            case EDIT_QUEST:
            case EDIT_REPEATING_QUEST:
                questTextLayout.setHint(getString(R.string.add_quest_name_hint));
                infoContainer.setVisibility(View.VISIBLE);
                break;
        }

        if (editMode == EditMode.EDIT_QUEST) {
            toolbarTitle.setText(R.string.title_edit_quest);
            frequencyContainer.setVisibility(View.GONE);
        }
        if (editMode == EditMode.EDIT_REPEATING_QUEST) {
            toolbarTitle.setText(R.string.title_edit_repeating_quest);
            endDateContainer.setVisibility(View.GONE);
        }
    }

    private void setAddSubQuestInViewMode() {
        addSubQuest.setText(getString(R.string.edit_quest_add_sub_quest));
        addSubQuest.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        clearAddSubQuest.setVisibility(View.INVISIBLE);
    }

    private void setAddSubQuestInEditMode() {
        addSubQuest.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_87));
        addSubQuest.setText("");
        clearAddSubQuest.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.add_sub_quest_clear)
    public void onClearAddSubQuestClick(View v) {
        hideUnderline(addSubQuest);
        addSubQuest.clearFocus();
        setAddSubQuestInViewMode();
    }

    private void showUnderline(View view) {
        view.getBackground().clearColorFilter();
    }

    private void hideUnderline(View view) {
        view.getBackground().setColorFilter(ContextCompat.getColor(this, android.R.color.transparent), PorterDuff.Mode.SRC_IN);
    }

    @OnEditorAction(R.id.add_sub_quest)
    public boolean onSubQuestEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            addSubQuest();
            return true;
        } else {
            return false;
        }
    }

    private void addSubQuest() {
        String name = addSubQuest.getText().toString();
        if (StringUtils.isEmpty(name)) {
            return;
        }

        SubQuest sq = new SubQuest(name);
        subQuestListAdapter.addSubQuest(sq);
        eventBus.post(new NewSubQuestEvent(sq, EDIT_QUEST));
        setAddSubQuestInEditMode();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_quest_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                onSaveTap(EventSource.TOOLBAR);
                return true;
            case R.id.action_delete:
                AlertDialog d = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_delete_quest_title)).setMessage(getString(R.string.dialog_delete_quest_message)).create();
                if (editMode == EditMode.EDIT_QUEST) {
                    String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
                    questPersistenceService.findById(questId, quest -> {
                        d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_it), (dialogInterface, i) -> {
                            eventBus.post(new DeleteQuestRequestEvent(quest, EDIT_QUEST));
                            setResult(Constants.RESULT_REMOVED);
                            finish();
                        });
                        d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> {
                            eventBus.post(new CancelDeleteQuestEvent(quest, EDIT_QUEST));
                        });
                        d.show();
                    });
                } else if (editMode == EditMode.EDIT_REPEATING_QUEST) {
                    String questId = getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY);
                    repeatingQuestPersistenceService.findById(questId, repeatingQuest -> {
                        d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_it), (dialogInterface, i) -> {
                            eventBus.post(new DeleteRepeatingQuestRequestEvent(repeatingQuest, EDIT_QUEST));
                            Toast.makeText(this, R.string.repeating_quest_deleted, Toast.LENGTH_SHORT).show();
                            setResult(Constants.RESULT_REMOVED);
                            finish();
                        });
                        d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> {
                            eventBus.post(new UndoDeleteRepeatingQuestEvent(repeatingQuest, EDIT_QUEST));
                        });
                        d.show();
                    });
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveTap(EventSource source) {
        if (editMode == EditMode.EDIT_QUEST) {
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
            q.setScheduledDate((LocalDate) endDateText.getTag());
            q.setDuration((int) durationText.getTag());
            q.setPriority((int) priorityText.getTag());
            q.setStartMinute(startTimeText.getTag() != null ? (int) startTimeText.getTag() : null);
            q.setCategory(categoryView.getSelectedCategory().name());
            q.setChallengeId((String) challengeText.getTag());
            q.setTimesADay((int) timesADayText.getTag());
            List<Note> textNotes = q.getTextNotes();
            String txt = (String) noteText.getTag();

            if (!StringUtils.isEmpty(txt)) {
                if (textNotes.isEmpty()) {
                    q.getNotes().add(new Note(txt));
                } else {
                    textNotes.get(0).setText(txt);
                }
            } else {
                q.removeTextNote();
            }

            q.setSubQuests(subQuestListAdapter.getSubQuests());
            q.setReminders(getReminders());
            eventBus.post(new UpdateQuestEvent(q, source));
            if (q.getScheduled() != null) {
                Toast.makeText(this, R.string.quest_saved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.quest_moved_to_inbox, Toast.LENGTH_SHORT).show();
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
            rq.setPriority((int) priorityText.getTag());
            rq.setStartMinute(startTimeText.getTag() != null ? (int) startTimeText.getTag() : null);
            rq.setRecurrence((Recurrence) frequencyText.getTag());
            rq.setCategory(categoryView.getSelectedCategory().name());
            rq.setChallengeId((String) challengeText.getTag());
            rq.setTimesADay((int) timesADayText.getTag());
            List<Note> textNotes = rq.getTextNotes();
            String txt = (String) noteText.getTag();

            if (!StringUtils.isEmpty(txt)) {
                if (textNotes.isEmpty()) {
                    rq.getNotes().add(new Note(txt));
                } else {
                    textNotes.get(0).setText(txt);
                }
            } else {
                rq.removeTextNote();
            }

            rq.setSubQuests(subQuestListAdapter.getSubQuests());
            rq.setReminders(getReminders());
            eventBus.post(new UpdateRepeatingQuestEvent(rq, source));
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

    @OnClick(R.id.quest_end_date_container)
    public void onEndDateClick(View view) {
        DatePickerFragment f = DatePickerFragment.newInstance((LocalDate) endDateText.getTag(), this);
        f.show(this.getSupportFragmentManager());
    }

    @OnClick(R.id.quest_start_time_container)
    public void onStartTimeClick(View view) {
        Time time = Time.now();
        if (startTimeText.getTag() != null) {
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
        RecurrencePickerFragment recurrencePickerFragment = RecurrencePickerFragment.newInstance((Recurrence) frequencyText.getTag(), this);
        recurrencePickerFragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_priority_container)
    public void onPriorityClick(View view) {
        PriorityPickerFragment priorityPickerFragment = PriorityPickerFragment.newInstance((int) priorityText.getTag(), this);
        priorityPickerFragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_add_reminder_container)
    public void onRemindersClicked(View view) {
        EditReminderFragment f = EditReminderFragment.newInstance((reminder, editMode) -> {
            if (reminder != null) {
                addReminder(reminder);
            }
            eventBus.post(new QuestReminderPickedEvent(reminder, editMode.name(), this.editMode.name()));
        });
        f.show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_times_a_day_container)
    public void onTimesADayClick(View view) {
        TimesADayPickerFragment fragment;
        if (timesADayText.getTag() != null && (int) timesADayText.getTag() > 0) {
            fragment = TimesADayPickerFragment.newInstance((int) timesADayText.getTag(), this);
        } else {
            fragment = TimesADayPickerFragment.newInstance(this);
        }
        fragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_challenge_container)
    public void onChallengeClick(View view) {
        ChallengePickerFragment.newInstance((String) challengeText.getTag(), this).show(getSupportFragmentManager());
    }

    @OnClick(R.id.quest_note_container)
    public void onNoteClick(View view) {
        TextPickerFragment.newInstance((String) noteText.getTag(), R.string.pick_note_title, this).show(getSupportFragmentManager());
    }

    @Override
    public void onDatePicked(LocalDate date) {
        if (date != null) {
            setFrequencyText(null);
        }
        populateEndDate(date);
        eventBus.post(new QuestDatePickedEvent(editMode.name().toLowerCase()));
    }

    @Override
    public void onTimesADayPicked(int timesADay) {
        populateTimesADay(timesADay);
        eventBus.post(new QuestDurationPickedEvent(editMode.name().toLowerCase()));
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
        populateStartTime(time == null ? null : time.toMinuteOfDay());
        eventBus.post(new QuestStartTimePickedEvent(editMode.name().toLowerCase()));
    }

    @Override
    public void onDurationPicked(int duration) {
        populateDuration(duration);
        eventBus.post(new QuestDurationPickedEvent(editMode.name().toLowerCase()));
    }

    @Override
    public void onPriorityPicked(int priority) {
        populatePriority(priority);
        eventBus.post(new QuestPriorityPickedEvent(priority));
    }

    @Override
    public void onChallengePicked(Challenge challenge) {
        populateChallenge(challenge);
        String name = challenge != null ? challenge.getName() : getString(R.string.none);
        eventBus.post(new ChallengePickedEvent(editMode.name().toLowerCase(), name));
    }

    private void populateChallenge(Challenge challenge) {
        if (challenge != null) {
            challengeText.setText(challenge.getName());
            challengeText.setTag(challenge.getId());
        } else {
            challengeText.setText(R.string.none);
            challengeText.setTag(null);
        }
    }

    private void populateEndDate(LocalDate date) {
        if (date != null) {
            setFrequencyText(null);
        }
        endDateText.setText(DateFormatter.format(date));
        endDateText.setTag(date);
    }

    private void populateTimesADay(int timesADay) {
        timesADayText.setText(TimesADayFormatter.formatReadable(timesADay));
        timesADayText.setTag(timesADay);
        if (timesADay > 1) {
            populateStartTime(null);
        }
    }

    private void populateStartTime(Integer startMinute) {
        if (startMinute != null) {
            populateTimesADay(1);
            startTimeText.setText(Time.of(startMinute).toString(shouldUse24HourFormat()));
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

    private void populatePriority(int priority) {
        priorityText.setText(PriorityFormatter.format(this, priority));
        priorityText.setTag(priority);
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
            if (!reminder.getNotificationId().equals(r.getNotificationId())
                    && reminder.getMinutesFromStart() == r.getMinutesFromStart()) {
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
        }
        frequencyText.setText(FrequencyTextFormatter.formatReadable(recurrence));
        frequencyText.setTag(recurrence);
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
    public void onCategoryChanged(Category category) {
        colorLayout(category);
    }

    private void colorLayout(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
    }

    @Override
    protected void onDestroy() {
        categoryView.removeCategoryChangedListener(this);
        super.onDestroy();
    }
}