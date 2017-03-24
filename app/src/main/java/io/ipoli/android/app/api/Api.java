package io.ipoli.android.app.api;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.ApiConstants;
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
    private final Gson gson;
    private OkHttpClient httpClient;


    public Api(ObjectMapper objectMapper, Gson gson) {
        this.objectMapper = objectMapper;
        this.gson = gson;
        httpClient = new OkHttpClient().newBuilder().retryOnConnectionFailure(false).build();

    }

    public void createSession(AuthProvider authProvider, String accessToken, String email, SessionResponseListener responseListener) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        Map<String, String> params = new HashMap<>();
        params.put("auth_provider", authProvider.getProvider());
        params.put("auth_id", authProvider.getId());
        params.put("access_token", accessToken);
        params.put("email", email);

        JSONObject jsonObject = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(ApiConstants.IPOLI_SERVER_URL + "users/").post(body);

        httpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseListener.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

//                    TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
//                    };
//                    Map<String, Object> session = objectMapper.convertValue(response.body().charStream(), mapTypeReference);
                    Type type = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    Map<String, Object> session = gson.fromJson(response.body().charStream(), type);
                    String authId = (String) session.get("auth_id");
                    String email = (String) session.get("email");
                    String playerId = (String) session.get("player_id");
                    List<Cookie> cookies = Cookie.parseAll(HttpUrl.get(getUrl(ApiConstants.IPOLI_SERVER_URL)), response.headers());
                    responseListener.onSuccess(authId, email, cookies, playerId);
                } else {
                    responseListener.onError(new ApiResponseException(call.request().url().toString(), response.code(), response.message()));
                }
            }
        });
    }

    private URL getUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void migratePlayer(String firebasePlayerId, PlayerMigratedListener responseListener) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Map<String, String> params = new HashMap<>();
        params.put("player_id", firebasePlayerId);
        JSONObject jsonObject = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(ApiConstants.IPOLI_SERVER_URL + "migration/").post(body);

        httpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseListener.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Type type = new TypeToken<Map<String, List<Object>>>() {
                    }.getType();
                    Map<String, List<Object>> documents = gson.fromJson(response.body().charStream(), type);
                    responseListener.onSuccess(documents);
                } else {
                    responseListener.onError(new ApiResponseException(call.request().url().toString(), response.code(), response.message()));
                }
            }
        });
    }

    public interface PlayerMigratedListener {
        void onSuccess(Map<String, List<Object>> documents);

        void onError(Exception e);
    }

    public interface SessionResponseListener {
        void onSuccess(String username, String email, List<Cookie> cookies, String playerId);

        void onError(Exception e);
    }
}
