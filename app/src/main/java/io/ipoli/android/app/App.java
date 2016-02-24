package io.ipoli.android.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.jobs.RemindPlanDayJob;
import io.ipoli.android.app.jobs.RemindReviewDayJob;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.assistant.AssistantService;
import io.ipoli.android.player.LevelUpActivity;
import io.ipoli.android.player.PlayerService;
import io.ipoli.android.player.events.PlayerLevelUpEvent;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.QuestsSavedEvent;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class App extends Application {

    private static AppComponent appComponent;

    @Inject
    Bus eventBus;

    @Inject
    AnalyticsService analyticsService;

    @Inject
    AssistantService assistantService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    PlayerService playerService;

    @Override
    public void onCreate() {
        super.onCreate();
        getAppComponent(this).inject(this);
        resetDueDateForIncompleteQuests();
        registerServices();
        initPlanDayReminder();
        initReviewDayReminder();
        sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int runCount = prefs.getInt(Constants.KEY_APP_RUN_COUNT, 0);
        if (runCount == 0) {
            saveInitialQuests();
        }
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(Constants.KEY_APP_RUN_COUNT, runCount + 1);
        e.apply();
    }

    private void saveInitialQuests() {
        List<Quest> quests = new ArrayList<>();

        addTomorrowQuests(quests);
        addTodayUnscheduledQuests(quests);
        addTodayScheduledQuests(quests);
        addInboxQuests(quests);

        questPersistenceService.saveAll(quests);
    }

    private void addTodayUnscheduledQuests(List<Quest> initialQuests) {
        Quest trashQuest = new Quest("Throw away the trash", new Date());
        Quest.setContext(trashQuest, QuestContext.CHORES);
        initialQuests.add(trashQuest);
    }

    private void addTomorrowQuests(List<Quest> initialQuests) {
        Date tomorrow = DateUtils.getTomorrow();

        Quest gymQuest = new Quest("Go to the gym", tomorrow);
        Quest.setContext(gymQuest, QuestContext.WELLNESS);
        Quest.setStartTime(gymQuest, Time.now());
        gymQuest.setDuration(60);
        initialQuests.add(gymQuest);

        Quest brushQuest = new Quest("Brush your teeth", tomorrow);
        Quest.setContext(brushQuest, QuestContext.WELLNESS);
        Quest.setStartTime(brushQuest, Time.atHours(10));

        initialQuests.add(brushQuest);
    }

    private void addInboxQuests(List<Quest> initialQuests) {
        Quest defrostQuest = new Quest("Defrost the freezer");
        Quest.setContext(defrostQuest, QuestContext.CHORES);
        initialQuests.add(defrostQuest);

        Quest dentistQuest = new Quest("Go to the dentist");
        Quest.setContext(dentistQuest, QuestContext.PERSONAL);
        initialQuests.add(dentistQuest);
    }

    private void addTodayScheduledQuests(List<Quest> initialQuests) {
        Quest welcomeQuest = new Quest("Get to know iPoli", DateUtils.getNow());
        Quest.setContext(welcomeQuest, QuestContext.FUN);
        Quest.setStartTime(welcomeQuest, Time.afterMinutes(10));
        initialQuests.add(welcomeQuest);

        Quest readQuest = new Quest("Read a book", DateUtils.getNow());
        Quest.setContext(readQuest, QuestContext.LEARNING);
        readQuest.setDuration(60);
        Quest.setStartTime(readQuest, Time.afterHours(2));
        initialQuests.add(readQuest);

        Quest callQuest = new Quest("Call mom & dad", new Date());
        Quest.setContext(callQuest, QuestContext.PERSONAL);
        Quest.setStartTime(callQuest, Time.at(19, 30));
        callQuest.setDuration(15);
        initialQuests.add(callQuest);
    }

    private void resetDueDateForIncompleteQuests() {
        List<Quest> quests = questPersistenceService.findAllUncompleted();
        for (Quest q : quests) {
            if (q.getDue() != null && DateUtils.isBeforeToday(q.getDue())) {
                q.setDue(null);
                questPersistenceService.save(q);
            }
        }
    }

    private void registerServices() {
        eventBus.register(analyticsService);
        eventBus.register(assistantService);
        eventBus.register(playerService);
        eventBus.register(this);
    }

    @Subscribe
    public void onPlayerLevelUp(PlayerLevelUpEvent e) {
        Intent i = new Intent(this, LevelUpActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(LevelUpActivity.LEVEL_EXTRA_KEY, e.newLevel);
        startActivity(i);
    }

    private void initPlanDayReminder() {
        Time time = Time.at(Constants.DEFAULT_PLAN_DAY_TIME);
        new RemindPlanDayJob(this, time).schedule();
    }

    private void initReviewDayReminder() {
        Time time = Time.at(Constants.DEFAULT_REVIEW_DAY_TIME);
        new RemindReviewDayJob(this, time).schedule();
    }

    public static AppComponent getAppComponent(Context context) {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(context))
                    .build();
        }

        return appComponent;
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        scheduleNextReminder();
    }

    @Subscribe
    public void onQuestsSaved(QuestsSavedEvent e) {
        scheduleNextReminder();
    }

    @Subscribe
    public void onQuestDeleted(QuestDeletedEvent e) {
        scheduleNextReminder();
    }

    private void scheduleNextReminder() {
        sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}
