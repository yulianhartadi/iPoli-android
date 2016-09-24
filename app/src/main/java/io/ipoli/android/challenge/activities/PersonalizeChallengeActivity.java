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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.adapters.PredefinedChallengeQuestAdapter;
import io.ipoli.android.challenge.data.PredefinedChallenge;
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

    private ArrayList<PredefinedChallengeQuestViewModel> viewModels;

    private PredefinedChallenge predefinedChallenge;

    private PredefinedChallengeQuestAdapter predefinedChallengeQuestAdapter;

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
        initViewModels(index);

        predefinedChallengeQuestAdapter = new PredefinedChallengeQuestAdapter(this, eventBus, viewModels);
        questList.setAdapter(predefinedChallengeQuestAdapter);

        setBackgroundColors(predefinedChallenge.challenge.getCategoryType());
    }

    protected void initViewModels(int index) {
        viewModels = new ArrayList<>();
        switch (index) {
            case 0:
                createWeightCutter();
                break;
            case 1:
                createStressFreeMind();
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
    }

    private void createFamousWriter() {
        
    }

    private void createProgrammingNinja() {
    }

    private void createEnglishJedi() {
    }

    private void createHealthyAndFit() {
    }

    private void createStressFreeMind() {
    }

    private void createWeightCutter() {
        Category category = predefinedChallenge.challenge.getCategoryType();
        RepeatingQuest rq1 = makeRepeatingQuest("Run 2 times a week for 30 min", "Run", 30, category);
        Recurrence recurrence = new Recurrence(1);
        recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
        recurrence.setFlexibleCount(2);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(DateUtils.toStartOfDayUTC(LocalDate.now()));
        recurrence.setDtendDate(predefinedChallenge.challenge.getEndDate());
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1, true));

        rq1 = makeRepeatingQuest("Workout at the gym 3 times a week for 1h", "Workout", 60, category);
        recurrence = new Recurrence(1);
        recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
        recurrence.setFlexibleCount(3);
        recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(DateUtils.toStartOfDayUTC(LocalDate.now()));
        recurrence.setDtendDate(predefinedChallenge.challenge.getEndDate());
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1, true));

        rq1 = makeRepeatingQuest("Measure my weight every morning", "Measure my weight", Constants.QUEST_MIN_DURATION, category);
        recurrence = new Recurrence(1);
        recurrence.setDtstartDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusDays(2)));
        recurrence.setDtendDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusDays(9)));
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1, true));

        rq1 = makeRepeatingQuest("Prepare healthy dinner 6 times a week", "Prepare healthy dinner", 60, category);
        recurrence = new Recurrence(1);
        recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
        recurrence.setFlexibleCount(6);
        recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(DateUtils.toStartOfDayUTC(LocalDate.now()));
        recurrence.setDtendDate(predefinedChallenge.challenge.getEndDate());
        rq1.setRecurrence(recurrence);
        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1, true));

        Quest quest1 = makeQuest("Sign up for a gym club card", category);
        Quest.setStartTime(quest1, Time.afterMinutes(60));
        quest1.setDuration(30);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1, true));
    }

    private void createMasterPresenter() {
        Category category = predefinedChallenge.challenge.getCategoryType();

        Quest quest1 = makeQuest("Learn how to give great presentation", category);
        Quest.setStartTime(quest1, Time.afterMinutes(15));
        quest1.setDuration(30);
        quest1.addNote(new Note(Note.Type.URL, "Presentation Tips by Princeton University", "https://www.princeton.edu/~archss/webpdfs08/BaharMartonosi.pdf"));
        quest1.addNote(new Note(Note.Type.URL, "Presentation Tips by University of Kent", "https://www.kent.ac.uk/careers/presentationskills.htm"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest1, true));

        Quest quest2 = makeQuest("Sign up at Canva", category);
        Quest.setStartTime(quest2, Time.afterHours(1));
        quest2.setDuration(15);
        quest2.addNote(new Note(Note.Type.URL, "Sign up at Canva", "https://www.canva.com/"));

        viewModels.add(new PredefinedChallengeQuestViewModel(quest2, true));

        Quest quest3 = makeQuest("Create my presentation at Canva", category, DateUtils.getTomorrow());
        Quest.setStartTime(quest3, Time.atHours(11));
        quest3.setDuration(120);
        quest3.addNote(new Note(Note.Type.URL, "Open Canva", "https://www.canva.com/"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest3, true));

        RepeatingQuest rq1 = makeRepeatingQuest("Practice presenting alone twice a day for a week", "Practice presenting alone", 20, category);
        Recurrence recurrence = new Recurrence(2);
        recurrence.setDtstartDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusDays(2)));
        recurrence.setDtendDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusDays(9)));
        recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        rq1.setRecurrence(recurrence);

        viewModels.add(new PredefinedChallengeQuestViewModel(rq1.getRawText(), rq1, true));

        Quest quest4 = makeQuest("Practice presenting to a friend", category, LocalDate.now().plusDays(7).toDate());
        quest4.setDuration(30);
        viewModels.add(new PredefinedChallengeQuestViewModel(quest4, true));

        Quest quest5 = makeQuest("Upload my presentation to SlideShare", category, LocalDate.now().plusDays(10).toDate());
        quest5.setDuration(30);
        quest5.addNote(new Note(Note.Type.URL, "Sign up at SlideShare.net", "https://www.slideshare.net/upload"));
        viewModels.add(new PredefinedChallengeQuestViewModel(quest5, true));
    }

    @NonNull
    private Quest makeQuest(String name, Category category) {
        return makeQuest(name, category, DateUtils.now());
    }

    @NonNull
    private Quest makeQuest(String name, Category category, Date endDate) {
        Quest q = new Quest(name, endDate);
        q.setCategory(category.name());
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(new Reminder(0, new Random().nextInt()));
        q.setReminders(reminders);
        return q;
    }

    private RepeatingQuest makeRepeatingQuest(String rawText, String name, int duration, Category category) {
        RepeatingQuest rq = new RepeatingQuest(rawText);
        rq.setName(name);
        rq.setDuration(duration);
        rq.setCategory(category.name());
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(new Reminder(0, new Random().nextInt()));
        rq.setReminders(reminders);
        return rq;
    }

    private void setBackgroundColors(Category category) {
        collapsingToolbar.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbar.setStatusBarScrimColor(ContextCompat.getColor(this, category.color700));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
    }

    @OnClick(R.id.accept_challenge)
    public void onAcceptChallenge(View view) {
        challengePersistenceService.save(predefinedChallenge.challenge, () -> {
            List<Quest> quests = new ArrayList<>();
            List<RepeatingQuest> repeatingQuests = new ArrayList<>();
            List<BaseQuest> selectedQuests = predefinedChallengeQuestAdapter.getSelectedQuests();
            for (BaseQuest bq : selectedQuests) {
                if (bq instanceof Quest) {
                    quests.add((Quest) bq);
                } else {
                    repeatingQuests.add((RepeatingQuest) bq);
                }
            }
            questPersistenceService.save(quests, () -> {
                repeatingQuestPersistenceService.save(repeatingQuests, () -> {
                    finish();
                });
            });
        });
    }
}
