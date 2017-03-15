package io.ipoli.android.player.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.ApiConstants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
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

    private static final String DATABASE_NAME = "sync_gateway";
    private static final String USER_LOCAL_DOC_ID = "user";
    private static final String SERVER_DB_URL = "http://10.0.2.2:4984/sync_gateway/";
    private static final String SERVER_ADMIN_DB_URL = "http://10.0.2.2:4985/sync_gateway/";

    @Inject
    Gson gson;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.google_sign_in)
    SignInButton signInButton;

    private GoogleApiClient googleApiClient;
    private OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ApiConstants.WEB_SERVER_GOOGLE_PLUS_CLIENT_ID)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
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
