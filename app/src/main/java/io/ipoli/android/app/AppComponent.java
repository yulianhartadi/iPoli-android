package io.ipoli.android.app;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.android.InviteOnlyActivity;
import io.ipoli.android.app.modules.AnalyticsModule;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.AssistantModule;
import io.ipoli.android.app.modules.BusModule;
import io.ipoli.android.app.modules.CommandParserModule;
import io.ipoli.android.app.modules.PersistenceModule;
import io.ipoli.android.app.modules.PlayerModule;
import io.ipoli.android.app.services.ReminderIntentService;
import io.ipoli.android.app.ui.PlayerBarLayout;
import io.ipoli.android.assistant.PickAvatarActivity;
import io.ipoli.android.chat.ChatActivity;
import io.ipoli.android.player.LevelUpActivity;
import io.ipoli.android.quest.PlanDayActivity;
import io.ipoli.android.quest.QuestCompleteActivity;
import io.ipoli.android.quest.QuestListActivity;
import io.ipoli.android.quest.services.QuestTimerIntentService;
import io.ipoli.android.quest.services.UpdateQuestIntentService;

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
                CommandParserModule.class,
                AssistantModule.class,
                PlayerModule.class
        }
)
public interface AppComponent {
    void inject(ChatActivity activity);

    void inject(App app);

    void inject(PlanDayActivity planDayActivity);

    void inject(QuestListActivity questListActivity);

    void inject(PlayerBarLayout playerBarLayout);

    void inject(PickAvatarActivity pickAvatarActivity);

    void inject(InviteOnlyActivity inviteOnlyActivity);

    void inject(UpdateQuestIntentService updateQuestIntentService);

    void inject(ReminderIntentService reminderIntentService);

    void inject(QuestTimerIntentService questTimerIntentService);

    void inject(QuestCompleteActivity questCompleteActivity);

    void inject(LevelUpActivity levelUpActivity);
}

