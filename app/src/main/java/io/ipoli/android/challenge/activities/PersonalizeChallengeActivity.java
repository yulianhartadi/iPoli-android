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
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.adapters.PredefinedChallengeQuestAdapter;
import io.ipoli.android.challenge.data.PredefinedChallenge;
import io.ipoli.android.challenge.events.AcceptChallengeEvent;
import io.ipoli.android.challenge.events.ShowPersonalizeChallengeEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.viewmodels.PredefinedChallengeQuestViewModel;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/13/16.
 */
public class PersonalizeChallengeActivity extends BaseActivity {

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
        predefinedChallenge = App.getPredefinedChallenges().get(index);

        collapsingToolbar.setTitle(predefinedChallenge.challenge.getName());
        challengePicture.setImageResource(predefinedChallenge.backgroundPicture);

        questList.setLayoutManager(new LinearLayoutManager(this));
        questList.setHasFixedSize(true);

        category = predefinedChallenge.challenge.getCategoryType();

        initViewModels(index);

        predefinedChallengeQuestAdapter = new PredefinedChallengeQuestAdapter(this, eventBus, viewModels);
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
        Quest quest1 = makeQuest("Plan a vacation with friends", Category.PERSONAL, LocalDate.now().plusDays(5));
        quest1.setDuration(90);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        RepeatingQuest rq1 = makeRepeatingQuest("Call parents once every week", "Call parents", 30, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(1);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest("Visit parents once every month", "Visit parents", 180, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        recurrence.setFlexibleCount(1);
        rq2.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        RepeatingQuest rq3 = makeRepeatingQuest("Have family dinner 3 times a week", "Have family dinner", 90, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq3.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));

        RepeatingQuest rq4 = makeRepeatingQuest("Go out with friends 2 times a week", "Go out with friends", 90, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(2);
        rq4.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq4.getRawText(), rq4));
    }

    private void createFamousWriter() {

        Quest q1 = makeQuest("Create a blog at Medium", category);
        q1.setStartTime(Time.afterMinutes(30));
        q1.setDuration(15);
        q1.addNote(new Note(Note.NoteType.URL, "Create my blog at Medium", "https://medium.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(q1));

        Quest q2 = makeQuest("Choose what I am going to write about", category);
        q2.setStartTime(Time.afterMinutes(90));
        q2.setDuration(45);
        viewModels.add(new PredefinedChallengeQuestViewModel(q2));

        Quest q3 = makeQuest("Pick 5 bloggers who inspire you and read most of their posts", category, LocalDate.now().plusDays(1));
        q3.setStartTime(Time.at(20, 30));
        q3.setDuration(60);
        viewModels.add(new PredefinedChallengeQuestViewModel(q3));

        Quest q4 = makeQuest("Research & write my first blog post", category, LocalDate.now().plusDays(2));
        q4.setDuration(90);
        viewModels.add(new PredefinedChallengeQuestViewModel(q4));

        RepeatingQuest rq1 = makeRepeatingQuest("Write a blog post once every week", "Write a blog post", 120, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(1);
        recurrence.setDtstartDate(LocalDate.now().plusDays(8));
        rq1.setRecurrence(recurrence);
        rq1.addNote(new Note(Note.NoteType.URL, "Visit Medium", "https://medium.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));
    }

    private void createProgrammingNinja() {
        Quest quest1 = makeQuest("Sign up at freeCodeCamp", category);
        quest1.setStartTime(Time.afterMinutes(15));
        quest1.setDuration(30);
        quest1.addNote(new Note(Note.NoteType.URL, "Sign up at freeCodeCamp", "https://www.freecodecamp.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        RepeatingQuest rq1 = makeRepeatingQuest("Read JavaScript For Cats 3 times a week", "Read JavaScript For Cats", 30, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        recurrence.setDtstartDate(LocalDate.now().plusDays(1));
        rq1.setRecurrence(recurrence);
        rq1.addNote(new Note(Note.NoteType.URL, "JavaScript For Cats", "https://noblemule.gitbooks.io/javascript-for-cats/content/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest("Conquer freeCodeCamp challenges 5 times a week", "Conquer freeCodeCamp challenges", 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(5);
        recurrence.setDtstartDate(LocalDate.now().plusDays(2));
        rq2.setRecurrence(recurrence);
        rq2.addNote(new Note(Note.NoteType.URL, "freeCodeCamp challenges", "https://www.freecodecamp.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        RepeatingQuest rq3 = makeRepeatingQuest("Watch CS50x Programming Course 2 times a week", "Watch CS50x Programming Course", 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(2);
        recurrence.setDtstartDate(LocalDate.now().plusDays(1));
        rq3.setRecurrence(recurrence);
        rq3.addNote(new Note(Note.NoteType.URL, "Watch CS50x on edX", "https://www.edx.org/course/introduction-computer-science-harvardx-cs50x"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));
    }

    private void createEnglishJedi() {
        Quest quest1 = makeQuest("Sign up at Duolingo", category);
        quest1.setStartTime(Time.afterMinutes(15));
        quest1.setDuration(15);
        quest1.addNote(new Note(Note.NoteType.INTENT, "Sign up at Duolingo", "com.duolingo"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        Quest quest2 = makeQuest("Sign up for a local English course", category, LocalDate.now().plusDays(1));
        quest2.setDuration(60);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest2));

        Quest quest3 = makeQuest("Subscribe to the Misterduncan YouTube channel", category);
        quest3.setDuration(15);
        quest3.addNote(new Note(Note.NoteType.URL, "Subscribe to Misterduncan", "https://www.youtube.com/channel/UC8pPDhxSn1nee70LRKJ0p3g"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest3));

        RepeatingQuest rq1 = makeRepeatingQuest("Learn using Duolingo for 15 min every day", "Learn using Duolingo", 15, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.DAILY);
        recurrence.setDtstartDate(LocalDate.now().plusDays(1));
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        rq1.addNote(new Note(Note.NoteType.INTENT, "Learn using Duolingo", "com.duolingo"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest("Watch a movie with English subtitles 5 times a week", "Watch a movie with English subtitles", 120, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(5);
        recurrence.setDtstartDate(LocalDate.now().plusDays(7));
        rq2.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        RepeatingQuest rq3 = makeRepeatingQuest("Read Alice in Wonderland 4 times a week", "Read Alice in Wonderland", 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(4);
        recurrence.setDtstartDate(LocalDate.now().plusDays(14));
        rq3.setRecurrence(recurrence);
        rq3.addNote(new Note(Note.NoteType.URL, "Read Alice in Wonderland", "https://www.gutenberg.org/files/11/11-h/11-h.htm"));
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));
    }

    private void createHealthyAndFit() {
        RepeatingQuest rq1 = makeRepeatingQuest("Drink 6 glasses of water every day", "Drink glass of water", 10, category);
        rq1.setTimesADay(6);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest("Eat healthy breakfast every day", "Eat healthy breakfast", 30, category);
        rq2.setStartTime(Time.at(9, 30));
        recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq2.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        RepeatingQuest rq3 = makeRepeatingQuest("Workout 3 times a week", "Workout", 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq3.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));

        RepeatingQuest rq4 = makeRepeatingQuest("Go for a walk 3 times a week", "Go for a walk", 30, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq4.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq4.getRawText(), rq4));

        RepeatingQuest rq5 = makeRepeatingQuest("Go for a run 2 times a week", "Go for a run", 30, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq5.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq5.getRawText(), rq5, false));

        RepeatingQuest rq6 = makeRepeatingQuest("Cook healthy dinner 5 times a week", "Cook healthy dinner", 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(5);
        rq6.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq6.getRawText(), rq6, false));

        RepeatingQuest rq7 = makeRepeatingQuest("Meditate 3 times a week", "Meditate", 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        rq7.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq7.getRawText(), rq7, false));

        RepeatingQuest rq8 = makeRepeatingQuest("Say 3 things I am grateful for every day", "Say 3 things I am grateful for", 10, category);
        recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq8.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq8.getRawText(), rq8));

        RepeatingQuest rq9 = makeRepeatingQuest("Eat a fruit every day", "Eat a fruit", 15, category);
        recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq9.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq9.getRawText(), rq9));
    }

    private void createStressFreeMind() {
        RepeatingQuest rq1 = makeRepeatingQuest("Meditate every day for 10 min", "Meditate", 10, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        RepeatingQuest rq2 = makeRepeatingQuest("Read a book for 30 min 3 times a week", "Read a book", 30, Category.LEARNING);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        rq2.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq2.getRawText(), rq2));

        Quest quest1 = makeQuest("Share your troubles with a friend", Category.PERSONAL, LocalDate.now().plusDays(5));
        quest1.setStartTime(Time.at(21, 0));
        quest1.setDuration(60);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        RepeatingQuest rq3 = makeRepeatingQuest("Take a walk for 30 min 5 times a week", "Take a walk", 30, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(5);
        recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        rq3.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq3.getRawText(), rq3));

        RepeatingQuest rq4 = makeRepeatingQuest("Say 3 things that I am grateful for every morning", "Say 3 things that I am grateful", 15, category);
        rq4.setStartMinute(Time.at(9, 30).toMinuteOfDay());
        recurrence = Recurrence.create();
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq4.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq4.getRawText(), rq4));
    }

    private void createWeightCutter() {

        Quest quest1 = makeQuest("Sign up for a gym club card", category);
        quest1.setStartTime(Time.afterMinutes(60));
        quest1.setDuration(30);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        RepeatingQuest rq1 = makeRepeatingQuest("Run 2 times a week for 30 min", "Run", 30, category);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(2);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());

        recurrence.setDtendDate(predefinedChallenge.challenge.getEndDate());
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        rq1 = makeRepeatingQuest("Workout at the gym 3 times a week for 1h", "Workout", 60, category);
        recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(3);
        recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());

        recurrence.setDtendDate(predefinedChallenge.challenge.getEndDate());
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        rq1 = makeRepeatingQuest("Measure my weight every morning", "Measure my weight", Constants.QUEST_MIN_DURATION, category);
        recurrence = Recurrence.create();
        recurrence.setDtstartDate(LocalDate.now().plusDays(2));
        recurrence.setDtendDate(LocalDate.now().plusDays(9));
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        rq1 = makeRepeatingQuest("Prepare healthy dinner 6 times a week", "Prepare healthy dinner", 60, category);
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
        Quest quest1 = makeQuest("Learn how to give great presentation", category);
        quest1.setStartTime(Time.afterMinutes(15));
        quest1.setDuration(30);
        quest1.addNote(new Note(Note.NoteType.URL, "Presentation Tips by Princeton University", "https://www.princeton.edu/~archss/webpdfs08/BaharMartonosi.pdf"));
        quest1.addNote(new Note(Note.NoteType.URL, "Presentation Tips by University of Kent", "https://www.kent.ac.uk/careers/presentationskills.htm"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1));

        Quest quest2 = makeQuest("Sign up at Canva", category);
        quest2.setStartTime(Time.afterHours(1));
        quest2.setDuration(15);
        quest2.addNote(new Note(Note.NoteType.URL, "Sign up at Canva", "https://www.canva.com/"));

        viewModels.add(new PredefinedChallengeQuestViewModel(quest2));

        Quest quest3 = makeQuest("Create my presentation at Canva", category, LocalDate.now().plusDays(1));
        quest3.setStartTime(Time.atHours(11));
        quest3.setDuration(120);
        quest3.addNote(new Note(Note.NoteType.URL, "Open Canva", "https://www.canva.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest3));

        RepeatingQuest rq1 = makeRepeatingQuest("Practice presenting alone twice a day for a week", "Practice presenting alone", 20, category);
        rq1.setTimesADay(2);
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(LocalDate.now().plusDays(2));
        recurrence.setDtendDate(LocalDate.now().plusDays(9));
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);

        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1));

        Quest quest4 = makeQuest("Practice presenting to a friend", category, LocalDate.now().plusDays(7));
        quest4.setDuration(30);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest4));

        Quest quest5 = makeQuest("Upload my presentation to SlideShare", category, LocalDate.now().plusDays(10));
        quest5.setDuration(30);
        quest5.addNote(new Note(Note.NoteType.URL, "Sign up at SlideShare.net", "https://www.slideshare.net/upload"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest5));
    }

    @NonNull
    private Quest makeQuest(String name, Category category) {
        return makeQuest(name, category, LocalDate.now());
    }

    @NonNull
    private Quest makeQuest(String name, Category category, LocalDate endDate) {
        Quest q = new Quest(name, endDate);
        q.setCategory(category.name());
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(new Reminder(0));
        q.setReminders(reminders);
        return q;
    }

    private RepeatingQuest makeRepeatingQuest(String rawText, String name, int duration, Category category) {
        RepeatingQuest rq = new RepeatingQuest(rawText);
        rq.setName(name);
        rq.setDuration(duration);
        rq.setCategory(category.name());
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(new Reminder(0));
        rq.setReminders(reminders);
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
        finish();
    }
}