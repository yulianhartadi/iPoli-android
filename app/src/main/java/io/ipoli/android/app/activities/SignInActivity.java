package io.ipoli.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

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
import org.threeten.bp.LocalDate;

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
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.api.UrlProvider;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FinishSignInActivityEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.exceptions.SignInException;
import io.ipoli.android.app.ui.UsernameValidator;
import io.ipoli.android.app.ui.dialogs.LoadingDialog;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.data.Profile;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.events.PlayerSignedInEvent;
import io.ipoli.android.player.events.PlayerSyncedEvent;
import io.ipoli.android.player.events.StartReplicationEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.Upgrade;
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

    @Inject
    FeedPersistenceService feedPersistenceService;

    @Inject
    UrlProvider urlProvider;

    @BindView(R.id.player_username)
    TextInputEditText usernameView;

    @BindView(R.id.existing_player)
    CheckBox existingPlayerView;

    @BindView(R.id.google_sign_in)
    SignInButton googleSignInButton;

    @BindView(R.id.facebook_login)
    FancyButton fbLoginButton;

    @BindView(R.id.guest_login)
    FancyButton guestButton;

    @BindView(R.id.divider)
    View divider;

    @BindView(R.id.divider_or)
    TextView dividerOr;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private CallbackManager callbackManager;

    private GoogleApiClient googleApiClient;

    private boolean isNewPlayer;

    private LoadingDialog dialog;

    private String displayName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);

        this.displayName = getIntent().getStringExtra(Constants.DISPLAY_NAME_EXTRA_KEY);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
        }

        isNewPlayer = playerPersistenceService.get() == null;

        initGoogleSingIn();
        initFBLogin();
        initGuestLogin();

        existingPlayerView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                usernameView.setEnabled(false);
                usernameView.setFocusable(false);
            } else {
                usernameView.setEnabled(true);
                usernameView.setFocusable(true);
            }
        });

        eventBus.post(new ScreenShownEvent(this, EventSource.SIGN_IN));
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    private void initGuestLogin() {
        if (!StringUtils.isEmpty(App.getPlayerId())) {
//            guestButton.setVisibility(View.GONE);
//            divider.setVisibility(View.GONE);
//            dividerOr.setVisibility(View.GONE);
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
                        closeLoadingDialog();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        showErrorMessage(exception);
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
        signInIfValid(() -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));
        });
    }

    private void showNoInternetMessage() {
        Toast.makeText(this, R.string.sign_in_internet, Toast.LENGTH_LONG).show();
    }

    private void createLoadingDialog() {
        dialog = LoadingDialog.show(this, getString(R.string.sign_in_loading_dialog_title), getString(R.string.sign_in_loading_dialog_message));
    }

    @OnClick(R.id.google_sign_in)
    public void onGoogleSignIn(View v) {
        signInIfValid(() -> {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });
    }

    @OnClick(R.id.guest_login)
    public void onGuestLogin(View v) {
        signUpAsGuest();
    }

    private void signInIfValid(ValidationListener validationListener) {
        if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
            showNoInternetMessage();
            return;
        }

        if (existingPlayerView.isChecked()) {
            createLoadingDialog();
            validationListener.onSuccess();
            return;
        }

        createLoadingDialog();
        String username = usernameView.getText().toString();
        UsernameValidator.validate(username, feedPersistenceService, new UsernameValidator.ResultListener() {
            @Override
            public void onValid() {
                validationListener.onSuccess();
            }

            @Override
            public void onInvalid(UsernameValidator.UsernameValidationError error) {
                closeLoadingDialog();
                switch (error) {
                    case EMPTY:
                        usernameView.setError(getString(R.string.username_is_empty));
                        break;
                    case NOT_UNIQUE:
                        usernameView.setError(getString(R.string.username_is_taken));
                        break;
                    default:
                        usernameView.setError(getString(R.string.username_wrong_format));
                }
            }
        });
    }

    public void getUserDetailsFromFB(AccessToken accessToken) {

        GraphRequest req = GraphRequest.newMeRequest(accessToken, (object, response) -> {
            if (object == null) {
                showErrorMessage(new Exception("Failed to get fields from Facebook"));
                return;
            }
            try {
                String id = object.getString("id");
                String email = "";
                if (object.has("email")) {
                    email = object.getString("email");
                }
                String firstName = object.getString("first_name");
                String lastName = object.getString("last_name");
                String picture = object.getJSONObject("picture").getJSONObject("data").getString("url");
                AuthProvider authProvider = new AuthProvider(id, AuthProvider.Provider.FACEBOOK);
                authProvider.setEmail(email);
                authProvider.setFirstName(firstName);
                authProvider.setLastName(lastName);
                authProvider.setUsername(firstName);
                authProvider.setPicture(picture);
                authProvider.setEmail(email);
                login(authProvider, accessToken.getToken());

            } catch (JSONException e) {
                showErrorMessage(e);
            }

        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,id,first_name,last_name,picture");
        req.setParameters(parameters);
        req.executeAsync();
    }


    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String idToken = account.getIdToken();
            if (idToken == null) {
                showErrorMessage(new SignInException("Google id token is null"));
                return;
            }
            AuthProvider authProvider = new AuthProvider(account.getId(), AuthProvider.Provider.GOOGLE);
            authProvider.setFirstName(account.getGivenName());
            authProvider.setLastName(account.getFamilyName());
            authProvider.setUsername(account.getDisplayName());
            authProvider.setEmail(account.getEmail());
            authProvider.setPicture(account.getPhotoUrl().toString());
            login(authProvider, idToken);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK && requestCode == RC_GOOGLE_SIGN_IN) {
            closeLoadingDialog();
            return;
        }

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void login(AuthProvider authProvider, String accessToken) {
        api.createSession(authProvider, accessToken, new Api.SessionResponseListener() {
            @Override
            public void onSuccess(String username, String email, List<Cookie> cookies, String playerId, boolean isNew, boolean shouldCreatePlayer) {
                authProvider.setEmail(email);
                eventBus.post(new PlayerSignedInEvent(authProvider.getProvider(), isNew));
                String usernameText = usernameView.getText().toString();
                Player existingPlayer = null;
                if (shouldCreatePlayer) {
                    existingPlayer = createPlayer(playerId, usernameText, SignInActivity.this.displayName, authProvider);
                } else if (isNew) {
                    updatePlayerWithAuthProviderAndUsername(authProvider, usernameText);
                }
                if (!existingPlayerView.isChecked() && isNew) {
                    if (existingPlayer == null) {
                        existingPlayer = getPlayer();
                    }
                    existingPlayer.setId(playerId);
                    feedPersistenceService.createProfile(new Profile(existingPlayer));
                }
                if (!isNew) {
                    pullPlayerDocs(cookies, playerId);
                } else {
                    eventBus.post(new StartReplicationEvent(cookies));
                    onFinish();
                }
            }

            @Override
            public void onError(Exception e) {
                showErrorMessage(e);
            }
        });
    }

    private void pullPlayerDocs(List<Cookie> cookies, String playerId) {
        //stop replication
        List<Replication> replications = database.getAllReplications();
        for (Replication replication : replications) {
            replication.stop();
            replication.clearAuthenticationStores();
        }
        //clean DB
        playerPersistenceService.deletePlayer();

        // single pull for shouldPullPlayerData
        Replication pull = database.createPullReplication(urlProvider.sync());
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
                eventBus.post(new PlayerSyncedEvent(playerId));
                eventBus.post(new StartReplicationEvent(cookies));
                onFinish();
            }
        });
        pull.start();
    }

    private void updatePlayerWithAuthProviderAndUsername(AuthProvider authProvider, String username) {
        Player player = getPlayer();
        player.setCurrentAuthProvider(authProvider);
        List<AuthProvider> authProviders = new ArrayList<>();
        authProviders.add(authProvider);
        player.setAuthProviders(authProviders);
        player.setUsername(username);
        playerPersistenceService.save(player);
    }

    private void createPlayer() {
        createPlayer("", "", "", null);
    }

    private Player createPlayer(String playerId, String username, String displayName, AuthProvider authProvider) {
        Pet pet = new Pet(Constants.DEFAULT_PET_NAME, Constants.DEFAULT_PET_AVATAR.code,
                Constants.DEFAULT_PET_BACKGROUND_PICTURE, Constants.DEFAULT_PET_HP);

        Player player = new Player(username, displayName,
                String.valueOf(Constants.DEFAULT_PLAYER_XP),
                Constants.DEFAULT_PLAYER_LEVEL,
                Constants.DEFAULT_PLAYER_COINS,
                Constants.DEFAULT_PLAYER_REWARD_POINTS,
                Constants.DEFAULT_PLAYER_AVATAR.code,
                DateFormat.is24HourFormat(this), pet);

        player.getInventory().addAvatar(Constants.DEFAULT_PLAYER_AVATAR, LocalDate.now());
        player.getInventory().addPet(Constants.DEFAULT_PET_AVATAR, LocalDate.now());
        player.getInventory().addUpgrade(Upgrade.REPEATING_QUESTS, LocalDate.now());

        if (authProvider != null) {
            player.setCurrentAuthProvider(authProvider);
            player.getAuthProviders().add(authProvider);
        }

        playerPersistenceService.save(player, playerId);
        eventBus.post(new PlayerCreatedEvent(player.getId()));
        return player;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showErrorMessage(new SignInException("Google connection failed: " + connectionResult.getErrorMessage()));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // intentional
    }

    @Override
    public void onConnectionSuspended(int i) {
        showErrorMessage(new SignInException("Google connection suspended " + i));
    }

    private void showErrorMessage(Exception e) {
        eventBus.post(new AppErrorEvent(e));
        runOnUiThread(() -> {
            closeLoadingDialog();
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
        });
    }

    private void closeLoadingDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (!App.hasPlayer()) {
            signUpAsGuest();
        } else {
            super.onBackPressed();
        }
    }

    private void signUpAsGuest() {
        createLoadingDialog();
        createPlayer();
        eventBus.post(new PlayerSignedInEvent("GUEST", true));
        Toast.makeText(this, R.string.using_as_guest, Toast.LENGTH_SHORT).show();
        onFinish();
    }

    private void onFinish() {
        closeLoadingDialog();
        eventBus.post(new FinishSignInActivityEvent(isNewPlayer));
        finish();
    }

    private interface ValidationListener {
        void onSuccess();
    }
}
