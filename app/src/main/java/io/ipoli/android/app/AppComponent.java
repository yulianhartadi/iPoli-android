package io.ipoli.android.app;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.android.MainActivity;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.modules.AnalyticsModule;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.BusModule;
import io.ipoli.android.app.modules.PersistenceModule;
import io.ipoli.android.app.modules.RestAPIModule;
import io.ipoli.android.app.net.JsonRequestBodyBuilder;
import io.ipoli.android.app.rate.RateDialog;
import io.ipoli.android.app.receivers.AndroidCalendarEventChangedReceiver;
import io.ipoli.android.app.services.AppJobService;
import io.ipoli.android.challenge.fragments.ChallengeListFragment;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.fragments.CalendarFragment;
import io.ipoli.android.quest.fragments.DayViewFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.fragments.RepeatingQuestListFragment;
import io.ipoli.android.quest.receivers.RemindStartQuestReceiver;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;
import io.ipoli.android.quest.receivers.ShowQuestCompleteNotificationReceiver;
import io.ipoli.android.quest.receivers.SnoozeQuestReceiver;
import io.ipoli.android.quest.receivers.StartQuestTimerReceiver;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;
import io.ipoli.android.quest.widgets.QuestRemoteViewsFactory;
import io.ipoli.android.reward.activities.RewardActivity;
import io.ipoli.android.reward.fragments.RewardListFragment;
import io.ipoli.android.tutorial.TutorialActivity;
import io.ipoli.android.tutorial.fragments.PickQuestsFragment;
import io.ipoli.android.tutorial.fragments.PickRepeatingQuestsFragment;
import io.ipoli.android.tutorial.fragments.SyncAndroidCalendarFragment;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Singleton
@Component(
        modules = {
                AppModule.class,
                BusModule.class,
                PersistenceModule.class,
                AnalyticsModule.class,
                RestAPIModule.class
        }
)
public interface AppComponent {

    void inject(App app);

    void inject(EditQuestActivity editQuestActivity);

    void inject(DatePickerFragment datePickerFragment);

    void inject(TimePickerFragment timePickerFragment);

    void inject(QuestActivity questActivity);

    void inject(ScheduleQuestReminderReceiver scheduleQuestReminderReceiver);

    void inject(SnoozeQuestReceiver snoozeQuestReceiver);

    void inject(ShowQuestCompleteNotificationReceiver showQuestCompleteNotificationReceiver);

    void inject(StartQuestTimerReceiver startQuestTimerReceiver);

    void inject(RemindStartQuestReceiver remindStartQuestReceiver);

    void inject(BaseActivity baseActivity);

    void inject(MainActivity mainActivity);

    void inject(AppJobService appJobService);

    void inject(DayViewFragment dayViewFragment);

    void inject(OverviewFragment overviewFragment);

    void inject(InboxFragment inboxFragment);

    void inject(RepeatingQuestListFragment repeatingQuestListFragment);

    void inject(TutorialActivity tutorialActivity);

    void inject(PickRepeatingQuestsFragment pickRepeatingQuestsFragment);

    void inject(PickQuestsFragment pickQuestsFragment);

    void inject(JsonRequestBodyBuilder jsonRequestBodyBuilder);

    void inject(CalendarFragment calendarFragment);

    void inject(AndroidCalendarEventChangedReceiver androidCalendarEventChangedReceiver);

    void inject(SyncAndroidCalendarFragment syncAndroidCalendarFragment);

    void inject(QuestRemoteViewsFactory questRemoteViewsFactory);

    void inject(AgendaWidgetProvider agendaWidgetProvider);

    void inject(RateDialog rateDialog);

    void inject(HelpDialog helpDialog);

    void inject(AddQuestActivity addQuestActivity);

    void inject(ChallengeListFragment challengeListFragment);

    void inject(RewardListFragment rewardListFragment);

    void inject(RewardActivity rewardActivity);
}

