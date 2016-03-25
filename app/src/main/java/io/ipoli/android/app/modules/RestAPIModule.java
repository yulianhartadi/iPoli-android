package io.ipoli.android.app.modules;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.net.APIService;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
@Module
public class RestAPIModule {

    private final String baseApiUrl;

    public RestAPIModule(String baseApiUrl) {
        this.baseApiUrl = baseApiUrl;
    }

    @Provides
    @Singleton
    public APIService provideApiService(Converter.Factory jsonFactory, CallAdapter.Factory callAdapterFactory) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseApiUrl)
                .addConverterFactory(jsonFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
        return retrofit.create(APIService.class);
    }

    @Provides
    @Singleton
    public Converter.Factory provideJsonFactory() {
        return GsonConverterFactory.create(
                new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create());
    }

    @Provides
    @Singleton
    public CallAdapter.Factory provideCallAdapterFactory() {
        return RxJavaCallAdapterFactory.create();
    }
}
