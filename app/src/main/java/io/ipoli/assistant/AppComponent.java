package io.ipoli.assistant;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.assistant.app.App;
import io.ipoli.assistant.modules.AnalyticsModule;
import io.ipoli.assistant.modules.AppModule;
import io.ipoli.assistant.modules.BusModule;
import io.ipoli.assistant.modules.CommandParserModule;
import io.ipoli.assistant.modules.PersistenceModule;
import io.ipoli.assistant.services.CommandParserService;

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
                CommandParserModule.class
        }
)
public interface AppComponent {
    void inject(MainActivity activity);

    void inject(App app);
}

