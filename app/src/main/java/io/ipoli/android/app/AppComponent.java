package io.ipoli.android.app;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.android.MainActivity;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.modules.AnalyticsModule;
import io.ipoli.android.app.modules.AndroidCalendarPersistenceModule;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.BusModule;
import io.ipoli.android.app.modules.JsonModule;
import io.ipoli.android.app.modules.LocalStorageModule;
import io.ipoli.android.app.modules.PersistenceModule;
import io.ipoli.android.app.modules.RewardGeneratorModule;
import io.ipoli.android.app.modules.SchedulerModule;
import io.ipoli.android.app.rate.RateDialog;
import io.ipoli.android.app.receivers.AndroidCalendarEventChangedReceiver;
import io.ipoli.android.app.receivers.DateChangedReceiver;
import io.ipoli.android.app.settings.SettingsFragment;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.tutorial.fragments.PickTutorialQuestsFragment;
import io.ipoli.android.app.tutorial.fragments.PickTutorialRepeatingQuestsFragment;
import io.ipoli.android.app.tutorial.fragments.SyncAndroidCalendarFragment;
import io.ipoli.android.challenge.activities.ChallengeActivity;
import io.ipoli.android.challenge.activities.EditChallengeActivity;
import io.ipoli.android.challenge.activities.PersonalizeChallengeActivity;
import io.ipoli.android.challenge.activities.PickChallengeActivity;
import io.ipoli.android.challenge.activities.PickChallengeQuestsActivity;
import io.ipoli.android.challenge.activities.PickDailyChallengeQuestsActivity;
import io.ipoli.android.challenge.fragments.ChallengeListFragment;
import io.ipoli.android.challenge.fragments.ChallengeOverviewFragment;
import io.ipoli.android.challenge.fragments.ChallengeQuestListFragment;
import io.ipoli.android.challenge.receivers.DailyChallengeReminderReceiver;
import io.ipoli.android.challenge.receivers.ScheduleDailyChallengeReminderReceiver;
import io.ipoli.android.pet.PetActivity;
import io.ipoli.android.player.activities.PickAvatarPictureActivity;
import io.ipoli.android.player.activities.SignInActivity;
import io.ipoli.android.player.fragments.GrowthFragment;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.activities.RepeatingQuestActivity;
import io.ipoli.android.quest.fragments.CalendarFragment;
import io.ipoli.android.quest.fragments.DayViewFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.fragments.RepeatingQuestListFragment;
import io.ipoli.android.quest.receivers.RemindStartQuestReceiver;
import io.ipoli.android.quest.receivers.ScheduleNextRemindersReceiver;
import io.ipoli.android.quest.receivers.ShowQuestCompleteNotificationReceiver;
import io.ipoli.android.quest.receivers.SnoozeQuestReceiver;
import io.ipoli.android.quest.receivers.StartQuestTimerReceiver;
import io.ipoli.android.quest.ui.dialogs.ChallengePickerFragment;
import io.ipoli.android.quest.ui.dialogs.EditReminderFragment;
import io.ipoli.android.quest.ui.dialogs.RecurrencePickerFragment;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;
import io.ipoli.android.quest.widgets.QuestRemoteViewsFactory;
import io.ipoli.android.reward.activities.EditRewardActivity;
import io.ipoli.android.reward.fragments.RewardListFragment;
import io.ipoli.android.shop.ShopActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Singleton
@Component(
        modules = {
                AppModule.class,
                BusModule.class,
                JsonModule.class,
                LocalStorageModule.class,
                PersistenceModule.class,
                AnalyticsModule.class,
                AndroidCalendarPersistenceModule.class,
                RewardGeneratorModule.class,
                SchedulerModule.class
        }
)
public interface AppComponent {

    void inject(App app);

    void inject(QuestActivity questActivity);

    void inject(SnoozeQuestReceiver snoozeQuestReceiver);

    void inject(ShowQuestCompleteNotificationReceiver showQuestCompleteNotificationReceiver);

    void inject(StartQuestTimerReceiver startQuestTimerReceiver);

    void inject(RemindStartQuestReceiver remindStartQuestReceiver);

    void inject(BaseActivity baseActivity);

    void inject(MainActivity mainActivity);

    void inject(DayViewFragment dayViewFragment);

    void inject(OverviewFragment overviewFragment);

    void inject(InboxFragment inboxFragment);

    void inject(RepeatingQuestListFragment repeatingQuestListFragment);

    void inject(TutorialActivity tutorialActivity);

    void inject(PickTutorialRepeatingQuestsFragment pickTutorialRepeatingQuestsFragment);

    void inject(PickTutorialQuestsFragment pickTutorialQuestsFragment);

    void inject(CalendarFragment calendarFragment);

    void inject(AndroidCalendarEventChangedReceiver androidCalendarEventChangedReceiver);

    void inject(SyncAndroidCalendarFragment syncAndroidCalendarFragment);

    void inject(QuestRemoteViewsFactory questRemoteViewsFactory);

    void inject(AgendaWidgetProvider agendaWidgetProvider);

    void inject(RateDialog rateDialog);

    void inject(HelpDialog helpDialog);

    void inject(EditQuestActivity editQuestActivity);

    void inject(ChallengeListFragment challengeListFragment);

    void inject(RewardListFragment rewardListFragment);

    void inject(EditRewardActivity editRewardActivity);

    void inject(PickAvatarPictureActivity pickAvatarPictureActivity);

    void inject(GrowthFragment growthFragment);

    void inject(EditChallengeActivity editChallengeActivity);

    void inject(RecurrencePickerFragment recurrencePickerFragment);

    void inject(SettingsFragment settingsFragment);

    void inject(PickDailyChallengeQuestsActivity pickDailyChallengeQuestsActivity);

    void inject(ChallengePickerFragment challengePickerFragment);

    void inject(RepeatingQuestActivity repeatingQuestActivity);

    void inject(ChallengeActivity challengeActivity);

    void inject(ChallengeOverviewFragment challengeOverviewFragment);

    void inject(ChallengeQuestListFragment challengeQuestListFragment);

    void inject(PickChallengeQuestsActivity pickChallengeQuestsActivity);

    void inject(EditReminderFragment editReminderFragment);

    void inject(ScheduleDailyChallengeReminderReceiver scheduleDailyChallengeReminderReceiver);

    void inject(DailyChallengeReminderReceiver dailyChallengeReminderReceiver);

    void inject(ScheduleNextRemindersReceiver scheduleNextRemindersReceiver);

    void inject(SignInActivity signInActivity);

    void inject(PetActivity petActivity);

    void inject(ShopActivity shopActivity);

    void inject(DateChangedReceiver dateChangedReceiver);

    void inject(PickChallengeActivity pickChallengeActivity);

    void inject(PersonalizeChallengeActivity personalizeChallengeActivity);

}

