package io.ipoli.android.app;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.android.InviteOnlyActivity;
import io.ipoli.android.MainActivity;
import io.ipoli.android.app.modules.AnalyticsModule;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.BusModule;
import io.ipoli.android.app.modules.PersistenceModule;
import io.ipoli.android.app.modules.RestAPIModule;
import io.ipoli.android.app.services.AppJobService;
import io.ipoli.android.assistant.PickAvatarActivity;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.fragments.AddQuestFragment;
import io.ipoli.android.quest.fragments.CalendarDayFragment;
import io.ipoli.android.quest.fragments.HabitsFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.receivers.RemindStartQuestReceiver;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;
import io.ipoli.android.quest.receivers.ShowDoneQuestNotificationReceiver;
import io.ipoli.android.quest.receivers.SnoozeQuestReceiver;
import io.ipoli.android.quest.receivers.StartQuestTimerReceiver;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;
import io.ipoli.android.tutorial.TutorialActivity;

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

    void inject(PickAvatarActivity pickAvatarActivity);

    void inject(InviteOnlyActivity inviteOnlyActivity);

    void inject(EditQuestActivity editQuestActivity);

    void inject(DatePickerFragment datePickerFragment);

    void inject(TimePickerFragment timePickerFragment);

    void inject(QuestActivity questActivity);

    void inject(ScheduleQuestReminderReceiver scheduleQuestReminderReceiver);

    void inject(SnoozeQuestReceiver snoozeQuestReceiver);

    void inject(ShowDoneQuestNotificationReceiver showDoneQuestNotificationReceiver);

    void inject(StartQuestTimerReceiver startQuestTimerReceiver);

    void inject(RemindStartQuestReceiver remindStartQuestReceiver);

    void inject(BaseActivity baseActivity);

    void inject(MainActivity mainActivity);

    void inject(AppJobService appJobService);

    void inject(CalendarDayFragment calendarDayFragment);

    void inject(OverviewFragment overviewFragment);

    void inject(AddQuestFragment addQuestFragment);

    void inject(InboxFragment inboxFragment);

    void inject(HabitsFragment habitsFragment);

    void inject(TutorialActivity tutorialActivity);
}

