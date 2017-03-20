package io.ipoli.android.app.modules;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.Api;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */
@Module
public class ApiModule {

    @Provides
    @Singleton
    public Api provideApi(ObjectMapper objectMapper) {
        return new Api(objectMapper);
    }
}
