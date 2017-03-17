package io.ipoli.android.player.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONException;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.ApiConstants;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.Api;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/16.
 */
public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    @Inject
    Api api;

    @Inject
    Gson gson;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.google_sign_in)
    SignInButton googleSignInButton;

    @BindView(R.id.facebook_login)
    LoginButton fbLoginButton;

    @BindView(R.id.anonymous_login)
    Button anonymousButton;

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

        initGoogleSingIn();
        initFBLogin();
        initAnonymousLogin();
    }

    private void initAnonymousLogin() {
        if (!StringUtils.isEmpty(App.getPlayerId())) {
            anonymousButton.setVisibility(View.GONE);
        }
    }

    private void initFBLogin() {
        fbLoginButton.setReadPermissions(Arrays.asList(new String[]{"email"}));
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
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
    }

    private void initGoogleSingIn() {
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
    }

    @OnClick(R.id.google_sign_in)
    public void onGoogleSignIn(View v) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @OnClick(R.id.anonymous_login)
    public void onAnonymousLogin(View v) {
        createPlayer();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void getUserDetailsFromFB(AccessToken accessToken) {

        GraphRequest req = GraphRequest.newMeRequest(accessToken, (object, response) -> {
            try {
                String id = object.getString("id");
                String email = object.getString("email");
                Map<String, String> params = new HashMap<>();
                params.put("access_token", accessToken.getToken());
                params.put("email", email);
                params.put("remote_url", "");
                login(new AuthProvider(id, AuthProvider.Provider.FACEBOOK), params, null);

            } catch (JSONException e) {
                eventBus.post(new AppErrorEvent(e));
            }

        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,id");
        req.setParameters(parameters);
        req.executeAsync();
    }


    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String idToken = account.getIdToken();
            if (idToken == null) {
                return;
            }
            login(new AuthProvider(getGoogleId(account), AuthProvider.Provider.GOOGLE), null, "Bearer " + idToken);
        }
    }

    @NonNull
    private String getGoogleId(GoogleSignInAccount account) {
        return "accounts.google.com_" + account.getId();
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

    private void login(AuthProvider authProvider, Map<String, String> params, String authHeader) {
        api.testCreateSession(authProvider, params, authHeader, new Api.SessionResponseListener() {
            @Override
            public void onSuccess(String username, List<Cookie> cookies, boolean userExists) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
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

    private void createPlayer() {
        createPlayer(null);
    }

    private void createPlayer(AuthProvider authProvider) {
        Pet pet = new Pet(Constants.DEFAULT_PET_NAME, Constants.DEFAULT_PET_AVATAR, Constants.DEFAULT_PET_BACKGROUND_IMAGE, Constants.DEFAULT_PET_HP);
        Player player = new Player(String.valueOf(Constants.DEFAULT_PLAYER_XP),
                Constants.DEFAULT_AVATAR_LEVEL,
                Constants.DEFAULT_PLAYER_COINS,
                Constants.DEFAULT_PLAYER_PICTURE,
                DateFormat.is24HourFormat(this), pet);
        if (authProvider != null) {
            player.setCurrentAuthProvider(authProvider);
            player.getAuthProviders().add(authProvider);
        }

        playerPersistenceService.save(player);
        eventBus.post(new PlayerCreatedEvent(player.getId()));
    }
}
