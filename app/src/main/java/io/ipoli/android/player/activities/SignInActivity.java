package io.ipoli.android.player.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.ApiConstants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
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
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/16.
 */
public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private static final String SERVER_DB_URL = "http://10.0.2.2:4984/sync_gateway/";
    private static final String SERVER_ADMIN_DB_URL = "http://10.0.2.2:4985/sync_gateway/";

    @Inject
    Gson gson;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.google_sign_in)
    SignInButton googleSignInButton;

    @BindView(R.id.facebook_login)
    LoginButton fbLoginButton;

    private GoogleApiClient googleApiClient;
    private OkHttpClient httpClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);

        httpClient = new OkHttpClient().newBuilder().addInterceptor(chain -> {
            Log.i("REQUEST INFO", chain.request().url().toString());
            Log.i("REQUEST INFO", chain.request().headers().toString());

            Response response = chain.proceed(chain.request());
            Log.i("RESPONSE INFO", response.toString());
            Log.i("RESPONSE INFO", response.body().toString());
            Log.i("RESPONSE INFO", response.message());

            return chain.proceed(chain.request());
        }).build();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ApiConstants.WEB_SERVER_GOOGLE_PLUS_CLIENT_ID)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });


        fbLoginButton.setReadPermissions(Arrays.asList(new String[]{"email"}));
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("AAAA FB: ", loginResult.getAccessToken().getToken());
                        getUserDetailsFromFB(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        eventBus.post(new AppErrorEvent(exception));
                    }
                });

//        Pet pet = new Pet(Constants.DEFAULT_PET_NAME, Constants.DEFAULT_PET_AVATAR, Constants.DEFAULT_PET_BACKGROUND_IMAGE, Constants.DEFAULT_PET_HP);
//        Player player = new Player(String.valueOf(Constants.DEFAULT_PLAYER_XP),
//                Constants.DEFAULT_AVATAR_LEVEL,
//                Constants.DEFAULT_PLAYER_COINS,
//                Constants.DEFAULT_PLAYER_PICTURE,
//                DateFormat.is24HourFormat(this), pet);
//        playerPersistenceService.save(player);
//        eventBus.post(new PlayerCreatedEvent(player.getId()));
//        startActivity(new Intent(this, MainActivity.class));
//        finish();
    }

    public void getUserDetailsFromFB(AccessToken accessToken) {

        GraphRequest req = GraphRequest.newMeRequest(accessToken, (object, response) -> {
            try {
                String email = object.getString("email");
                loginWithFacebook(accessToken, email);
            } catch (JSONException e) {
                eventBus.post(new AppErrorEvent(e));
            }

        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name");
        req.setParameters(parameters);
        req.executeAsync();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {

            GoogleSignInAccount acct = result.getSignInAccount();
            String username = "accounts.google.com_" + acct.getId();

            checkUserStatus(username, new UserStatusListener() {
                @Override
                public void onUserExists() {
                    loginWithGoogle(acct);
                }

                @Override
                public void onUserDoesNotExist() {
                    //create player
                    Log.d("AAA player", "Should create new player");
                    loginWithGoogle(acct);
                }

                @Override
                public void onFailure(Exception e) {
                    showMessage("Failed to get user : " + username, e);
                }
            });

        } else {
            String errorMessage = "Google Sign-in failed: (" +
                    result.getStatus().getStatusCode() + ") " +
                    result.getStatus().getStatusMessage();
            showMessage(errorMessage, null);
        }

    }

    private void loginWithGoogle(GoogleSignInAccount account) {
        String idToken = account.getIdToken();
        if (idToken != null) {
            loginWithGoogleSignIn(idToken);
        } else {
            showMessage("Google Sign-in failed : No ID Token returned", null);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        String errorMessage = "Google Sign-in connection failed: (" +
                result.getErrorCode() + ") " +
                result.getErrorMessage();
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void checkUserStatus(String username, UserStatusListener listener) {
        Request request = new Request.Builder()
                .url(getServerAdminDbUserUrl(username))
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    listener.onUserExists();
                } else if (response.code() == 404) {
                    listener.onUserDoesNotExist();
                }
            }
        });
    }

    public void loginWithFacebook(final AccessToken accessToken, String email) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Map<String, String> params = new HashMap<>();
        params.put("access_token", accessToken.getToken());
        params.put("email", email);
        params.put("remote_url", "http://localhost:4984");
        JSONObject jsonObject = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(getServerDbFacebookUrl())
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showMessage("Failed to create a new SGW session with IDToken : " + accessToken, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Type type = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    Map<String, Object> session = gson.fromJson(response.body().charStream(), type);
                    Map<String, Object> userInfo = (Map<String, Object>) session.get("userCtx");
                    final String username = (userInfo != null ? (String) userInfo.get("name") : null);
                    final List<Cookie> cookies =
                            Cookie.parseAll(HttpUrl.get(getServerDbUrl()), response.headers());
//                    if (login(username, cookies)) {
//                        completeLogin();
//                    }
                }
            }
        });
    }

    public void loginWithGoogleSignIn(final String idToken) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{}");
        Request request = new Request.Builder()
                .url(getServerDbSessionUrl())
                .header("Authorization", "Bearer " + idToken)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showMessage("Failed to create a new SGW session with IDToken : " + idToken, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Type type = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    Map<String, Object> session = gson.fromJson(response.body().charStream(), type);
                    Map<String, Object> userInfo = (Map<String, Object>) session.get("userCtx");
                    final String username = (userInfo != null ? (String) userInfo.get("name") : null);
                    final List<Cookie> cookies =
                            Cookie.parseAll(HttpUrl.get(getServerDbUrl()), response.headers());
//                    if (login(username, cookies)) {
//                        completeLogin();
//                    }
                }
            }
        });
    }

    public void showMessage(final String message, final Throwable throwable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder(message);
                if (throwable != null) {
                    sb.append(": " + throwable);
                    Log.e("AAA", message, throwable);
                }
                Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public URL getServerDbUrl() {
        try {
            return new URL(SERVER_DB_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public URL getServerDbFacebookUrl() {
        String serverUrl = SERVER_DB_URL;
        if (!serverUrl.endsWith("/"))
            serverUrl = serverUrl + "/";
        try {
            return new URL(serverUrl + "_facebook");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private URL getServerDbSessionUrl() {
        String serverUrl = SERVER_DB_URL;
        if (!serverUrl.endsWith("/"))
            serverUrl = serverUrl + "/";
        try {
            return new URL(serverUrl + "_session");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private URL getServerAdminDbUserUrl(String username) {
        String serverUrl = SERVER_ADMIN_DB_URL;
        if (!serverUrl.endsWith("/"))
            serverUrl = serverUrl + "/";
        try {
            return new URL(serverUrl + "_user/" + username);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    interface UserStatusListener {
        void onUserExists();

        void onUserDoesNotExist();

        void onFailure(Exception e);
    }
}
