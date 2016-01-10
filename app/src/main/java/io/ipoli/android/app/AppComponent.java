package io.ipoli.android.app;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.android.app.modules.AnalyticsModule;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.AssistantModule;
import io.ipoli.android.app.modules.BusModule;
import io.ipoli.android.app.modules.CommandParserModule;
import io.ipoli.android.app.modules.PersistenceModule;
import io.ipoli.android.chat.ChatActivity;
import io.ipoli.android.quest.PlanDayActivity;
import io.ipoli.android.quest.QuestListActivity;

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
                AssistantModule.class
        }
)
public interface AppComponent {
    void inject(ChatActivity activity);

    void inject(App app);

    void inject(PlanDayActivity fragment);

    void inject(QuestListActivity questListActivity);
}

