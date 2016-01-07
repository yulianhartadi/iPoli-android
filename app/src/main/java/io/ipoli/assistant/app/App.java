package io.ipoli.assistant.app;

import android.app.Application;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.assistant.AppComponent;
import io.ipoli.assistant.DaggerAppComponent;
import io.ipoli.assistant.modules.AppModule;
import io.ipoli.assistant.services.AnalyticsService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class App extends Application {

    @Inject
    Bus eventBus;

    @Inject
    AnalyticsService analyticsService;

    @Override
    public void onCreate() {
        super.onCreate();
        getAppComponent().inject(this);
        initAnalytics();
    }

    private void initAnalytics() {
        eventBus.register(this);
        eventBus.register(analyticsService);
    }


    private static AppComponent appComponent;

    public AppComponent getAppComponent() {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(this))
                    .build();
        }

        return appComponent;
    }
}
