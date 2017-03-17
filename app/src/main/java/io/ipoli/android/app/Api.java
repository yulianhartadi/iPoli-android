package io.ipoli.android.app;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
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
import io.ipoli.android.app.utils.StringUtils;
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
        httpClient = new OkHttpClient().newBuilder().addInterceptor(chain -> {
            Log.i("REQUEST INFO", chain.request().url().toString());
            Log.i("REQUEST INFO", chain.request().headers().toString());

            Response response = chain.proceed(chain.request());
            Log.i("RESPONSE INFO", response.toString());
            Log.i("RESPONSE INFO", response.body().toString());
            Log.i("RESPONSE INFO", response.message());

            return chain.proceed(chain.request());
        }).build();
    }

    public void createSession(AuthProvider authProvider, Map<String, String> params, String authHeader, SessionResponseListener responseListener) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        if(params == null) {
            params = new HashMap<>();
        }
        params.put("auth_provider", authProvider.getProvider());
        params.put("username", authProvider.getId());
        JSONObject jsonObject = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(ApiConstants.URL).post(body);
        if (!StringUtils.isEmpty(authHeader)) {
            builder.addHeader("Authorization", authHeader);
        }

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
                    Map<String, Object> session = objectMapper.convertValue(response.body().charStream(), mapTypeReference);
                    String username = (String) session.get("username");
                    String email = (String) session.get("email");
                    List<Cookie> cookies = Cookie.parseAll(HttpUrl.get(getUrl(ApiConstants.URL)), response.headers());
                    boolean newUserCreated = (boolean) session.get("newUserCreated");
                    responseListener.onSuccess(username, email, cookies, newUserCreated);
                }
            }
        });
    }

    public void testCreateSession(AuthProvider authProvider, final Map<String, String> params, String authHeader, SessionResponseListener responseListener) {
        String gatewayUrl = "http://10.0.2.2:4984/sync_gateway/";
        String gatewayAdminUrl = "http://10.0.2.2:4984/sync_gateway/";

        Request request = new Request.Builder()
                .url(getUrl(gatewayAdminUrl + "_user/" + authProvider.getId()))
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseListener.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean newUserCreated = true;
                if (response.isSuccessful()) {
                    newUserCreated = false;
                }

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                Map<String, String> bodyParams = params;
                if(bodyParams == null) {
                    bodyParams = new HashMap<>();
                }
                bodyParams.put("auth_provider", authProvider.getProvider());
                JSONObject jsonObject = new JSONObject(bodyParams);
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                String url = gatewayUrl;
                if(authProvider.getProviderType() == AuthProvider.Provider.FACEBOOK) {
                    url += "_facebook";
                } else {
                    url += "_session";
                }

                Request.Builder builder = new Request.Builder();
                builder.url(url).post(body);
                if (!StringUtils.isEmpty(authHeader)) {
                    builder.addHeader("Authorization", authHeader);
                }

                boolean finalNewUserCreated = newUserCreated;
                httpClient.newCall(builder.build()).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        responseListener.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {

//                            TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
//                            };
//                            Map<String, Object> session = objectMapper.convertValue(response.body().charStream(), mapTypeReference);
                            Type type = new TypeToken<Map<String, Object>>() {
                            }.getType();
                            Map<String, Object> session = gson.fromJson(response.body().charStream(), type);
                            Map<String, Object> userInfo = (Map<String, Object>) session.get("userCtx");
                            String username = (userInfo != null ? (String) userInfo.get("name") : null);
                            String email = (userInfo != null ? (String) userInfo.get("email") : null);
                            List<Cookie> cookies = Cookie.parseAll(HttpUrl.get(getUrl(gatewayUrl)), response.headers());
                            responseListener.onSuccess(username, email, cookies, finalNewUserCreated);
                        }
                    }
                });
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

    public interface SessionResponseListener {
        void onSuccess(String username, String email, List<Cookie> cookies, boolean newUserCreated);

        void onError(Exception e);
    }
}
