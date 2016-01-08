package io.ipoli.android.app;

import android.app.Application;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.AppComponent;
import io.ipoli.android.DaggerAppComponent;
import io.ipoli.android.assistant.AssistantService;
import io.ipoli.android.modules.AppModule;
import io.ipoli.android.services.AnalyticsService;

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

    @Override
    public void onCreate() {
        super.onCreate();
        getAppComponent().inject(this);
        registerServices();
    }

    private void registerServices() {
        eventBus.register(analyticsService);
        eventBus.register(assistantService);
    }

    public AppComponent getAppComponent() {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(this))
                    .build();
        }

        return appComponent;
    }
}
