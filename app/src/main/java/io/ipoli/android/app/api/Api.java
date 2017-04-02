package io.ipoli.android.app.api;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.api.exceptions.ApiResponseException;
import io.ipoli.android.player.AuthProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */
public class Api {

    private final ObjectMapper objectMapper;
    private final UrlProvider urlProvider;
    private OkHttpClient httpClient;

    public Api(ObjectMapper objectMapper, UrlProvider urlProvider) {
        this.objectMapper = objectMapper;
        this.urlProvider = urlProvider;
        httpClient = new OkHttpClient().newBuilder()
                .readTimeout(Constants.API_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false).build();
    }

    public void createSession(AuthProvider authProvider, String accessToken, SessionResponseListener responseListener) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        Map<String, String> params = new HashMap<>();
        params.put("auth_provider", authProvider.getProvider());
        params.put("auth_id", authProvider.getId());
        params.put("access_token", accessToken);
        params.put("email", authProvider.getEmail());
        params.put("player_id", App.getPlayerId());

        JSONObject jsonObject = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(urlProvider.createUser()).post(body);

        httpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseListener.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
                    };
                    Map<String, Object> session = objectMapper.readValue(response.body().charStream(), mapTypeReference);
                    String authId = (String) session.get("auth_id");
                    String email = (String) session.get("email");
                    String playerId = (String) session.get("player_id");
                    boolean isNew = (boolean) session.get("is_new");
                    boolean shouldCreatePlayer = (boolean) session.get("should_create_player");
                    List<Cookie> cookies = Cookie.parseAll(HttpUrl.get(urlProvider.api()), response.headers());
                    responseListener.onSuccess(authId, email, cookies, playerId, isNew, shouldCreatePlayer);
                } else {
                    responseListener.onError(getApiResponseException(call, response));
                }
            }
        });
    }

    @NonNull
    private ApiResponseException getApiResponseException(Call call, Response response) {
        String message = response.message();
        if(response.body() != null) {
            try {
                message += " " + response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ApiResponseException(call.request().url().toString(), response.code(), message);
    }

    public void migratePlayer(String firebasePlayerId, PlayerMigratedListener responseListener) {
        Request request = new Request.Builder()
                .url(urlProvider.migrateUser(firebasePlayerId))
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseListener.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    TypeReference<Map<String, List<Map<String, Object>>>> mapTypeReference = new TypeReference<Map<String, List<Map<String, Object>>>>() {
                    };
                    Map<String, List<Map<String, Object>>> documents = objectMapper.readValue(response.body().charStream(), mapTypeReference);
                    responseListener.onSuccess(documents);
                } else {
                    responseListener.onError(getApiResponseException(call, response));
                }
            }
        });
    }

    public interface PlayerMigratedListener {
        void onSuccess(Map<String, List<Map<String, Object>>> documents);

        void onError(Exception e);
    }

    public interface SessionResponseListener {
        void onSuccess(String username, String email, List<Cookie> cookies, String playerId, boolean isNew, boolean shouldCreatePlayer);

        void onError(Exception e);
    }
}
