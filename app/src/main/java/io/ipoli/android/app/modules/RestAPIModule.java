package io.ipoli.android.app.modules;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.APIConstants;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.app.net.iPoliAPIService;
import io.ipoli.android.app.net.UtcDateTypeAdapter;
import io.ipoli.android.app.scheduling.SchedulingAPIService;
import io.realm.RealmObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
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

    private final String ipoliAPIBaseUrl;
    private final String schedulingAPIBaseUrl;

    public RestAPIModule(String ipoliAPIBaseUrl, String schedulingAPIBaseUrl) {
        this.ipoliAPIBaseUrl = ipoliAPIBaseUrl;
        this.schedulingAPIBaseUrl = schedulingAPIBaseUrl;
    }

    @Provides
    @Singleton
    public iPoliAPIService provideApiService(Converter.Factory jsonFactory, CallAdapter.Factory callAdapterFactory, OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ipoliAPIBaseUrl)
                .addConverterFactory(jsonFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .client(client)
                .build();
        return retrofit.create(iPoliAPIService.class);
    }

    @Provides
    @Singleton
    public SchedulingAPIService provideSchedulingApiService(Converter.Factory jsonFactory, CallAdapter.Factory callAdapterFactory, OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(schedulingAPIBaseUrl)
                .addConverterFactory(jsonFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .client(client)
                .build();
        return retrofit.create(SchedulingAPIService.class);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .serializeNulls()
                .setDateFormat(Constants.API_DATETIME_ISO_8601_FORMAT)
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new UtcDateTypeAdapter())
                .create();
    }

    @Provides
    @Singleton
    public Converter.Factory provideJsonFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @Singleton
    public CallAdapter.Factory provideCallAdapterFactory() {
        return RxJavaCallAdapterFactory.create();
    }

    @Provides
    @Singleton
    public OkHttpClient provideHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(chain -> {
            Request request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Api-Key", APIConstants.API_KEY)
                    .build();
            return chain.proceed(request);
        });
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(logging);
        }
        return builder.build();
    }
}
