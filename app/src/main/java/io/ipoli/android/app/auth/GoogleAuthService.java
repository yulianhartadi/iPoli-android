package io.ipoli.android.app.auth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.squareup.otto.Bus;

import io.ipoli.android.ApiConstants;
import io.ipoli.android.Constants;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.player.SignInException;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */

public class GoogleAuthService implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private final GoogleSignInOptions gso;
    private final Bus eventBus;

    public GoogleAuthService(Bus eventBus) {
        this.eventBus = eventBus;
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ApiConstants.WEB_SERVER_GOOGLE_PLUS_CLIENT_ID)
                .requestScopes(Constants.GOOGLE_SCOPE_CALENDAR)
                .requestEmail()
                .build();
    }

    public void getIdToken(Context context, TokenListener tokenListener) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();

        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        pendingResult.setResultCallback(googleSignInResult -> {
            if (googleSignInResult.isSuccess()) {
                tokenListener.onIdTokenReceived(googleSignInResult.getSignInAccount().getIdToken());
            }
            googleApiClient.disconnect();
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        eventBus.post(new AppErrorEvent(new SignInException("Google silent connection failed: " + connectionResult.getErrorMessage())));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        eventBus.post(new AppErrorEvent(new SignInException("Google silent connection suspended " + i)));

    }

    public interface TokenListener {
        void onIdTokenReceived(String idToken);
    }
}
