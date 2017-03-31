package io.ipoli.android.app.modules;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.utils.LocalStorage;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/31/16.
 */
@Module
public class LocalStorageModule {

    @Provides
    @Singleton
    public LocalStorage provideLocalStorage(Context context, ObjectMapper objectMapper) {
        return LocalStorage.of(context, objectMapper);
    }

}
