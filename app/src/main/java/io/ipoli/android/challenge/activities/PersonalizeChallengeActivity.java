package io.ipoli.android.challenge.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import net.fortuna.ical4j.model.Recur;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.adapters.PredefinedChallengeQuestAdapter;
import io.ipoli.android.challenge.data.PredefinedChallenge;
import io.ipoli.android.challenge.events.AcceptChallengeEvent;
import io.ipoli.android.challenge.events.NewChallengeEvent;
import io.ipoli.android.challenge.events.ShowPersonalizeChallengeEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.viewmodels.PredefinedChallengeQuestViewModel;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.player.PowerUpManager;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.reminder.data.Reminder;
import io.ipoli.android.store.PowerUp;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/13/16.
 */
public class PersonalizeChallengeActivity extends BaseActivity {
    @Inject
    PowerUpManager powerUpManager;

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.predefined_challenge_quests)
    RecyclerView questList;

    @BindView(R.id.challenge_picture)
    ImageView challengePicture;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;


    @Inject
    RepeatingQuestScheduler repeatingQuestScheduler;

    private ArrayList<PredefinedChallengeQuestViewModel> viewModels;

    private PredefinedChallenge predefinedChallenge;

    private PredefinedChallengeQuestAdapter predefinedChallengeQuestAdapter;

    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || !getIntent().hasExtra(Constants.PREDEFINED_CHALLENGE_INDEX)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_personalize_challenge);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        int index = getIntent().getIntExtra(Constants.PREDEFINED_CHALLENGE_INDEX, 0);
        predefinedChallenge = App.getPredefinedChallenges(this).get(index);

        collapsingToolbar.setTitle(predefinedChallenge.challenge.getName());
        challengePicture.setImageResource(predefinedChallenge.backgroundPicture);

        questList.setLayoutManager(new LinearLayoutManager(this));
        questList.setHasFixedSize(true);

        category = predefinedChallenge.challenge.getCategoryType();

        initViewModels(index);

        predefinedChallengeQuestAdapter = new PredefinedChallengeQuestAdapter(viewModels);
        questList.setAdapter(predefinedChallengeQuestAdapter);

        setBackgroundColors();

        eventBus.post(new ShowPersonalizeChallengeEvent(predefinedChallenge.challenge.getName()));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_help).setVisible(false);
        menu.findItem(R.id.action_pick_daily_challenge_quests).setVisible(false);
        return true;
    }

    protected void initViewModels(int index) {
        viewModels = new ArrayList<>();
        switch (index) {
            case 0:
                createStressFreeMind();
                break;
            case 1:
                createWeightCutter();
                break;
            case 2:
                createHealthyAndFit();
                break;
            case 3:
                createEnglishJedi();
                break;
            case 4:
                createProgrammingNinja();
                break;
            case 5:
                createMasterPresenter();
                break;
            case 6:
                createFamousWriter();
                break;
            case 7:
                createFamilyAndFriendsTime();
                break;
        }
    }

    private void createFamilyAndFriendsTime() {
        Quest quest1 = makeQuest(getString(R.string.challenge8_quest1_name), Category.PERSONAL, LocalDate.now().plusDays(5));
        quest1.setDuration(90);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        RepeatingQuest rq1 = makeRepeatingQuest(getString(R.string.challenge8_rq1_raw_text), getString(R.string.challenge8_rq1_name), 30, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(1);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest(getString(R.string.challenge8_rq2_raw_text), getString(R.string.challenge8_rq2_name), 180, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        recurrence.setFlexibleCount(1);
        rq2.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        RepeatingQuest rq3 = makeRepeatingQuest(getString(R.string.challenge8_rq3_raw_text), getString(R.string.challenge8_rq3_name), 90, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq3.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));

        RepeatingQuest rq4 = makeRepeatingQuest(getString(R.string.challenge8_rq4_raw_text), getString(R.string.challenge8_rq4_name), 90, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(2);
        rq4.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq4.getRawText(), rq4));
    }

    private void createFamousWriter() {
        Quest q1 = makeQuest(getString(R.string.challenge7_quest1_name), category);
        q1.setStartTime(Time.afterMinutes(30));
        q1.setDuration(15);
        q1.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge7_quest1_note1), "https://medium.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(q1));

        Quest q2 = makeQuest(getString(R.string.challenge7_quest2_name), category);
        q2.setStartTime(Time.afterMinutes(90));
        q2.setDuration(45);
        viewModels.add(new PredefinedChallengeQuestViewModel(q2));

        Quest q3 = makeQuest(getString(R.string.challenge7_quest3_name), category, LocalDate.now().plusDays(1));
        q3.setStartTime(Time.at(20, 30));
        q3.setDuration(60);
        viewModels.add(new PredefinedChallengeQuestViewModel(q3));

        Quest q4 = makeQuest(getString(R.string.challenge7_quest4_name), category, LocalDate.now().plusDays(2));
        q4.setDuration(90);
        viewModels.add(new PredefinedChallengeQuestViewModel(q4));

        RepeatingQuest rq1 = makeRepeatingQuest(getString(R.string.challenge7_rq1_raw_text), getString(R.string.challenge7_rq1_name), 120, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(1);
        recurrence.setDtstartDate(LocalDate.now().plusDays(8));
        rq1.setRecurrence(recurrence);
        rq1.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge7_rq1_note1), "https://medium.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));
    }

    private void createProgrammingNinja() {
        Quest quest1 = makeQuest(getString(R.string.challenge5_quest1_name), category);
        quest1.setStartTime(Time.afterMinutes(15));
        quest1.setDuration(30);
        quest1.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge5_quest1_note1), "https://www.freecodecamp.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        RepeatingQuest rq1 = makeRepeatingQuest(getString(R.string.challenge5_rq1_raw_text), getString(R.string.challenge5_rq1_name), 30, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        recurrence.setDtstartDate(LocalDate.now().plusDays(1));
        rq1.setRecurrence(recurrence);
        rq1.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge5_rq1_note1), "https://noblemule.gitbooks.io/javascript-for-cats/content/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest(getString(R.string.challenge5_rq2_raw_text), getString(R.string.challenge5_rq2_name), 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(5);
        recurrence.setDtstartDate(LocalDate.now().plusDays(2));
        rq2.setRecurrence(recurrence);
        rq2.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge5_rq2_note1), "https://www.freecodecamp.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        RepeatingQuest rq3 = makeRepeatingQuest(getString(R.string.challenge5_rq3_raw_text), getString(R.string.challenge5_rq3_name), 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(2);
        recurrence.setDtstartDate(LocalDate.now().plusDays(1));
        rq3.setRecurrence(recurrence);
        rq3.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge5_rq3_note1), "https://www.edx.org/course/introduction-computer-science-harvardx-cs50x"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));
    }

    private void createEnglishJedi() {
        Quest quest1 = makeQuest(getString(R.string.challenge4_quest1_name), category);
        quest1.setStartTime(Time.afterMinutes(15));
        quest1.setDuration(15);
        quest1.addNote(new Note(Note.NoteType.INTENT, getString(R.string.challenge4_quest1_note1), "com.duolingo"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        Quest quest2 = makeQuest(getString(R.string.challenge4_quest2_name), category, LocalDate.now().plusDays(1));
        quest2.setDuration(60);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest2));

        Quest quest3 = makeQuest(getString(R.string.challenge4_quest3_name), category);
        quest3.setDuration(15);
        quest3.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge4_quest3_note1), "https://www.youtube.com/channel/UC8pPDhxSn1nee70LRKJ0p3g"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest3));

        RepeatingQuest rq1 = makeRepeatingQuest(getString(R.string.challenge4_rq1_raw_text), getString(R.string.challenge4_rq1_name), 15, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.DAILY);
        recurrence.setDtstartDate(LocalDate.now().plusDays(1));
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        rq1.addNote(new Note(Note.NoteType.INTENT, getString(R.string.challenge4_rq1_note1), "com.duolingo"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest(getString(R.string.challenge4_rq2_raw_text), getString(R.string.challenge4_rq2_name), 120, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(5);
        recurrence.setDtstartDate(LocalDate.now().plusDays(7));
        rq2.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        RepeatingQuest rq3 = makeRepeatingQuest(getString(R.string.challenge4_rq3_raw_text), getString(R.string.challenge4_rq3_name), 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(4);
        recurrence.setDtstartDate(LocalDate.now().plusDays(14));
        rq3.setRecurrence(recurrence);
        rq3.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge4_rq3_note1), "https://www.gutenberg.org/files/11/11-h/11-h.htm"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));
    }

    private void createHealthyAndFit() {
        RepeatingQuest rq1 = makeRepeatingQuest(getString(R.string.challenge3_rq1_raw_text), getString(R.string.challenge3_rq1_name), 10, category);
        rq1.setTimesADay(6);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest(getString(R.string.challenge3_rq2_raw_text), getString(R.string.challenge3_rq2_name), 30, category);
        rq2.setStartTime(Time.at(9, 30));
        recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq2.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        RepeatingQuest rq3 = makeRepeatingQuest(getString(R.string.challenge3_rq3_raw_text), getString(R.string.challenge3_rq3_name), 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq3.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));

        RepeatingQuest rq4 = makeRepeatingQuest(getString(R.string.challenge3_rq4_raw_text), getString(R.string.challenge3_rq4_name), 30, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq4.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq4.getRawText(), rq4));

        RepeatingQuest rq5 = makeRepeatingQuest(getString(R.string.challenge3_rq5_raw_text), getString(R.string.challenge3_rq5_name), 30, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq5.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq5.getRawText(), rq5, false));

        RepeatingQuest rq6 = makeRepeatingQuest(getString(R.string.challenge3_rq6_raw_text), getString(R.string.challenge3_rq6_name), 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(5);
        rq6.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq6.getRawText(), rq6, false));

        RepeatingQuest rq7 = makeRepeatingQuest(getString(R.string.challenge3_rq7_raw_text), getString(R.string.challenge3_rq7_name), 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq7.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq7.getRawText(), rq7, false));

        RepeatingQuest rq8 = makeRepeatingQuest(getString(R.string.challenge3_rq8_raw_text), getString(R.string.challenge3_rq8_name), 10, category);
        recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq8.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq8.getRawText(), rq8));

        RepeatingQuest rq9 = makeRepeatingQuest(getString(R.string.challenge3_rq9_raw_text), getString(R.string.challenge3_rq9_name), 15, category);
        recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq9.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq9.getRawText(), rq9));
    }

    private void createStressFreeMind() {
        RepeatingQuest rq1 = makeRepeatingQuest(getString(R.string.challenge2_rq1_raw_text), getString(R.string.challenge2_rq1_name), 10, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest(getString(R.string.challenge2_rq2_raw_text), getString(R.string.challenge2_rq2_name), 30, Category.LEARNING);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        rq2.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        Quest quest1 = makeQuest(getString(R.string.challenge2_quest1_name), Category.PERSONAL, LocalDate.now().plusDays(5));
        quest1.setStartTime(Time.at(21, 0));
        quest1.setDuration(60);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        RepeatingQuest rq3 = makeRepeatingQuest(getString(R.string.challenge2_rq3_raw_text), getString(R.string.challenge2_rq3_name), 30, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(5);
        recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        rq3.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));

        RepeatingQuest rq4 = makeRepeatingQuest(getString(R.string.challenge2_rq4_raw_text), getString(R.string.challenge2_rq4_name), 15, category);
        rq4.setStartMinute(Time.at(9, 30).toMinuteOfDay());
        recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq4.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq4.getRawText(), rq4));
    }

    private void createWeightCutter() {

        Quest quest1 = makeQuest(getString(R.string.challenge1_quest1_name), category);
        quest1.setStartTime(Time.afterMinutes(60));
        quest1.setDuration(30);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        RepeatingQuest rq1 = makeRepeatingQuest(getString(R.string.challenge1_rq1_raw_text), getString(R.string.challenge1_rq1_name), 30, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(2);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());

        recurrence.setDtendDate(predefinedChallenge.challenge.getEndDate());
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        rq1 = makeRepeatingQuest(getString(R.string.challenge1_rq2_raw_text), getString(R.string.challenge1_rq2_name), 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());

        recurrence.setDtendDate(predefinedChallenge.challenge.getEndDate());
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        rq1 = makeRepeatingQuest(getString(R.string.challenge1_rq3_raw_text), getString(R.string.challenge1_rq3_name), Constants.QUEST_MIN_DURATION, category);
        recurrence = Recurrence.create();
        recurrence.setDtstartDate(LocalDate.now().plusDays(2));
        recurrence.setDtendDate(LocalDate.now().plusDays(9));
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        rq1 = makeRepeatingQuest(getString(R.string.challenge1_rq4_raw_text), getString(R.string.challenge1_rq4_name), 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(6);
        recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtendDate(predefinedChallenge.challenge.getEndDate());
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));
    }

    private void createMasterPresenter() {
        Quest quest1 = makeQuest(getString(R.string.challenge6_quest1_name), category);
        quest1.setStartTime(Time.afterMinutes(15));
        quest1.setDuration(30);
        quest1.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge6_quest1_note1), "https://www.princeton.edu/~archss/webpdfs08/BaharMartonosi.pdf"));
        quest1.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge6_quest1_note2), "https://www.kent.ac.uk/careers/presentationskills.htm"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        Quest quest2 = makeQuest(getString(R.string.challenge6_quest2_name), category);
        quest2.setStartTime(Time.afterHours(1));
        quest2.setDuration(15);
        quest2.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge6_quest2_note1), "https://www.canva.com/"));

        viewModels.add(new PredefinedChallengeQuestViewModel(quest2));

        Quest quest3 = makeQuest(getString(R.string.challenge6_quest3_name), category, LocalDate.now().plusDays(1));
        quest3.setStartTime(Time.atHours(11));
        quest3.setDuration(120);
        quest3.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge6_quest3_note1), "https://www.canva.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest3));

        RepeatingQuest rq1 = makeRepeatingQuest(getString(R.string.challenge6_rq1_raw_text), getString(R.string.challenge6_rq1_name), 20, category);
        rq1.setTimesADay(2);
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(LocalDate.now().plusDays(2));
        recurrence.setDtendDate(LocalDate.now().plusDays(9));
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);

        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        Quest quest4 = makeQuest(getString(R.string.challenge6_quest4_name), category, LocalDate.now().plusDays(7));
        quest4.setDuration(30);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest4));

        Quest quest5 = makeQuest(getString(R.string.challenge6_quest5_name), category, LocalDate.now().plusDays(10));
        quest5.setDuration(30);
        quest5.addNote(new Note(Note.NoteType.URL, getString(R.string.challenge6_quest5_note1), "https://www.slideshare.net/upload"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest5));
    }

    @NonNull
    private Quest makeQuest(String name, Category category) {
        return makeQuest(name, category, LocalDate.now());
    }

    @NonNull
    private Quest makeQuest(String name, Category category, LocalDate endDate) {
        Quest q = new Quest(name, endDate);
        q.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        q.setCategory(category.name());
        if (powerUpManager.isEnabled(PowerUp.REMINDERS)) {
            q.addReminder(new Reminder(0));
        }
        return q;
    }

    private RepeatingQuest makeRepeatingQuest(String rawText, String name, int duration, Category category) {
        RepeatingQuest rq = new RepeatingQuest(rawText);
        rq.setName(name);
        rq.setDuration(duration);
        rq.setCategory(category.name());
        rq.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        if (powerUpManager.isEnabled(PowerUp.REMINDERS)) {
            rq.addReminder(new Reminder(0));
        }
        return rq;
    }

    private void setBackgroundColors() {
        collapsingToolbar.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbar.setStatusBarScrimColor(ContextCompat.getColor(this, category.color700));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
    }

    @OnClick(R.id.accept_challenge)
    public void onAcceptChallenge(View view) {
        view.setVisibility(View.GONE);
        eventBus.post(new AcceptChallengeEvent(predefinedChallenge.challenge.getName()));
        Toast.makeText(this, R.string.challenge_accepted, Toast.LENGTH_SHORT).show();
        List<Quest> quests = new ArrayList<>();
        List<RepeatingQuest> repeatingQuests = new ArrayList<>();
        List<BaseQuest> selectedQuests = predefinedChallengeQuestAdapter.getSelectedQuests();
        for (BaseQuest bq : selectedQuests) {
            if (bq instanceof Quest) {
                Quest q = (Quest) bq;
                quests.add(q);
            } else {
                RepeatingQuest rq = (RepeatingQuest) bq;
                repeatingQuests.add(rq);
            }
        }
        Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests = new HashMap<>();
        for (RepeatingQuest repeatingQuest : repeatingQuests) {
            List<Quest> scheduledQuests = repeatingQuestScheduler.schedule(repeatingQuest, LocalDate.now());
            repeatingQuestToScheduledQuests.put(repeatingQuest, scheduledQuests);
        }

        challengePersistenceService.acceptChallenge(predefinedChallenge.challenge, quests, repeatingQuestToScheduledQuests);
        eventBus.post(new NewChallengeEvent(predefinedChallenge.challenge, EventSource.PERSONALIZE_CHALLENGE));
        finish();
    }
}