package io.ipoli.android.app.modules;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/4/17.
 */
@Module
public class ObjectMapperModule {

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return objectMapper;
    }

}
