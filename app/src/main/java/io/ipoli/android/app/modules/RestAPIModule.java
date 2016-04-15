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
import io.ipoli.android.Constants;
import io.ipoli.android.app.net.APIService;
import io.ipoli.android.app.net.UtcDateTypeAdapter;
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

    private final String baseApiUrl;

    public RestAPIModule(String baseApiUrl) {
        this.baseApiUrl = baseApiUrl;
    }

    @Provides
    @Singleton
    public APIService provideApiService(Converter.Factory jsonFactory, CallAdapter.Factory callAdapterFactory, OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseApiUrl)
                .addConverterFactory(jsonFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .client(client)
                .build();
        return retrofit.create(APIService.class);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
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
        return new OkHttpClient.Builder().addInterceptor(chain -> {
            Request request = chain.request();
            request = request.newBuilder().addHeader("Content-Type", "application/json").build();
            return chain.proceed(chain.request());
        }).addInterceptor(logging).build();
    }
}
