package io.ipoli.android.player.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;

import com.couchbase.lite.Database;
import com.couchbase.lite.replicator.Replication;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.ApiConstants;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.FinishSignInActivityEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.events.PlayerUpdatedEvent;
import io.ipoli.android.player.events.StartReplicationEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.Cookie;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/16.
 */
public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    @Inject
    Api api;

    @Inject
    Database database;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.google_sign_in)
    SignInButton googleSignInButton;

    @BindView(R.id.facebook_login)
    FancyButton fbLoginButton;

    @BindView(R.id.guest_login)
    FancyButton guestButton;

    private CallbackManager callbackManager;
    private GoogleApiClient googleApiClient;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isNewPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
        }

        isNewPlayer = playerPersistenceService.get() == null;

        initGoogleSingIn();
        initFBLogin();
        initGuestLogin();
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    private void initGuestLogin() {
        if (!StringUtils.isEmpty(App.getPlayerId())) {
            guestButton.setVisibility(View.GONE);
        }
    }

    private void initFBLogin() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
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
        googleSignInButton.setSize(SignInButton.SIZE_ICON_ONLY);
    }

    @OnClick(R.id.facebook_login)
    public void onFacebookSignIn(View v) {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));
    }

    @OnClick(R.id.google_sign_in)
    public void onGoogleSignIn(View v) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @OnClick(R.id.guest_login)
    public void onGuestLogin(View v) {
        createPlayer();
        finish();
    }

    public void getUserDetailsFromFB(AccessToken accessToken) {

        GraphRequest req = GraphRequest.newMeRequest(accessToken, (object, response) -> {
            try {
                String id = object.getString("id");
                String email = object.getString("email");
                login(new AuthProvider(id, AuthProvider.Provider.FACEBOOK), accessToken.getToken(), email);

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
            login(new AuthProvider(account.getId(), AuthProvider.Provider.GOOGLE), idToken, account.getEmail());
        }
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

    private void login(AuthProvider authProvider, String accessToken, String email) {
        api.createSession(authProvider, accessToken, email, new Api.SessionResponseListener() {
            @Override
            public void onSuccess(String username, String email, List<Cookie> cookies, String playerId, boolean isNew, boolean shouldCreatePlayer) {
                if (shouldCreatePlayer) {
                    createPlayer(playerId, authProvider, email);
                } else if (isNew) {
                    Player player = getPlayer();
                    player.setCurrentAuthProvider(authProvider);
                    List<AuthProvider> authProviders = new ArrayList<>();
                    authProviders.add(authProvider);
                    player.setAuthProviders(authProviders);
                    playerPersistenceService.save(player);
                }
                if (!isNew) {
                    //stop replication
                    List<Replication> replications = database.getAllReplications();
                    for (Replication replication : replications) {
                        replication.stop();
                        replication.clearAuthenticationStores();
                    }
                    //clean DB
                    playerPersistenceService.deletePlayer();

                    // single pull for shouldPullPlayerData
                    URL syncURL = null;
                    try {
                        syncURL = new URL(ApiConstants.IPOLI_SYNC_URL);
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    }
                    Replication pull = database.createPullReplication(syncURL);
                    for (Cookie cookie : cookies) {
                        pull.setCookie(cookie.name(), cookie.value(), cookie.path(),
                                new Date(cookie.expiresAt()), cookie.secure(), cookie.httpOnly());
                    }
                    pull.setContinuous(false);
                    List<String> channels = new ArrayList<>();
                    channels.add(playerId);
                    pull.setChannels(channels);
                    pull.addChangeListener(event -> {
                        if (event.getStatus() == Replication.ReplicationStatus.REPLICATION_STOPPED) {
                            eventBus.post(new PlayerUpdatedEvent(playerId));
                            eventBus.post(new StartReplicationEvent(cookies));
                            finish();
                        }
                    });
                    pull.start();
                } else {
                    eventBus.post(new StartReplicationEvent(cookies));
                    finish();
                }
            }

            @Override
            public void onError(Exception e) {
                eventBus.post(new AppErrorEvent(e));
            }
        });
    }

    private void createPlayer() {
        createPlayer(null, null, null);
    }

    private void createPlayer(String playerId, AuthProvider authProvider, String email) {
        Pet pet = new Pet(Constants.DEFAULT_PET_NAME, Constants.DEFAULT_PET_AVATAR, Constants.DEFAULT_PET_BACKGROUND_IMAGE, Constants.DEFAULT_PET_HP);
        Player player = new Player(String.valueOf(Constants.DEFAULT_PLAYER_XP),
                Constants.DEFAULT_AVATAR_LEVEL,
                Constants.DEFAULT_PLAYER_COINS,
                Constants.DEFAULT_PLAYER_PICTURE,
                DateFormat.is24HourFormat(this), pet);
        player.setEmail(email);
        if (authProvider != null) {
            player.setCurrentAuthProvider(authProvider);
            player.getAuthProviders().add(authProvider);
        }

        playerPersistenceService.save(player, playerId);
        eventBus.post(new PlayerCreatedEvent(player.getId()));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void finish() {
        eventBus.post(new FinishSignInActivityEvent(isNewPlayer));
        super.finish();
    }
}
