package io.ipoli.android.app.modules;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.api.UrlProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */
@Module
public class ApiModule {

    @Provides
    @Singleton
    public Api provideApi(ObjectMapper objectMapper, UrlProvider urlProvider) {
        return new Api(objectMapper, urlProvider);
    }
}
