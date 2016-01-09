package io.ipoli.android;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.android.app.App;
import io.ipoli.android.chat.ChatActivity;
import io.ipoli.android.chat.ChatFragment;
import io.ipoli.android.modules.AnalyticsModule;
import io.ipoli.android.modules.AppModule;
import io.ipoli.android.modules.AssistantModule;
import io.ipoli.android.modules.BusModule;
import io.ipoli.android.modules.CommandParserModule;
import io.ipoli.android.modules.PersistenceModule;
import io.ipoli.android.quest.PlanDayActivity;

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

    void inject(ChatFragment fragment);

    void inject(PlanDayActivity fragment);
}

