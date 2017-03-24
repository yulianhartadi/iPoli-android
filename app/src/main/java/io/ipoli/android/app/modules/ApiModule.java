package io.ipoli.android.app.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.api.Api;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */
@Module
public class ApiModule {

    @Provides
    @Singleton
    public Api provideApi(ObjectMapper objectMapper, Gson gson) {
        return new Api(objectMapper, gson);
    }
}
