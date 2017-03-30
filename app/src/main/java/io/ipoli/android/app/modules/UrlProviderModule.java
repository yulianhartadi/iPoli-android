package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.app.api.DevUrlProvider;
import io.ipoli.android.app.api.ProdUrlProvider;
import io.ipoli.android.app.api.UrlProvider;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/17.
 */
@Module
public class UrlProviderModule {

    @Provides
    @Singleton
    public UrlProvider provideUrlProvider() {
        if (BuildConfig.DEBUG) {
            return new DevUrlProvider();
        } else {
            return new ProdUrlProvider();
        }
    }
}
