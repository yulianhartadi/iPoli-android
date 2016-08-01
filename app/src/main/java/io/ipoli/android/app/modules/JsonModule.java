package io.ipoli.android.app.modules;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/31/16.
 */
@Module
public class JsonModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }
}
