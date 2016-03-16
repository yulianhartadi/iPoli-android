package io.ipoli.android.app;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.android.InviteOnlyActivity;
import io.ipoli.android.app.modules.AnalyticsModule;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.AssistantModule;
import io.ipoli.android.app.modules.BusModule;
import io.ipoli.android.app.modules.PersistenceModule;
import io.ipoli.android.app.modules.PlayerModule;
import io.ipoli.android.app.services.ReminderIntentService;
import io.ipoli.android.app.ui.PlayerBarLayout;
import io.ipoli.android.assistant.PickAvatarActivity;
import io.ipoli.android.player.LevelUpActivity;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.activities.InboxActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.activities.QuestCompleteActivity;
import io.ipoli.android.quest.fragments.CalendarDayFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;
import io.ipoli.android.quest.receivers.ShowDoneQuestNotificationReceiver;
import io.ipoli.android.quest.receivers.SnoozeQuestReceiver;
import io.ipoli.android.quest.receivers.StartQuestTimerReceiver;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;
import io.ipoli.android.tutorial.Tutorial;

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
                AssistantModule.class,
                PlayerModule.class
        }
)
public interface AppComponent {

    void inject(App app);

    void inject(InboxActivity inboxActivity);


    void inject(PlayerBarLayout playerBarLayout);

    void inject(PickAvatarActivity pickAvatarActivity);

    void inject(InviteOnlyActivity inviteOnlyActivity);

    void inject(ReminderIntentService reminderIntentService);

    void inject(QuestCompleteActivity questCompleteActivity);

    void inject(LevelUpActivity levelUpActivity);

    void inject(EditQuestActivity editQuestActivity);

    void inject(DatePickerFragment datePickerFragment);

    void inject(TimePickerFragment timePickerFragment);

    void inject(QuestActivity questActivity);

    void inject(ScheduleQuestReminderReceiver scheduleQuestReminderReceiver);

    void inject(SnoozeQuestReceiver snoozeQuestReceiver);

    void inject(ShowDoneQuestNotificationReceiver showDoneQuestNotificationReceiver);

    void inject(StartQuestTimerReceiver startQuestTimerReceiver);

    void inject(MainActivity mainActivity);

    void inject(CalendarDayFragment calendarDayFragment);

    void inject(OverviewFragment overviewFragment);

    void inject(AddQuestActivity addQuestActivity);

    void inject(Tutorial tutorial);
}

