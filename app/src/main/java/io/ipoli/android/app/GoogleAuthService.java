package io.ipoli.android.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

import io.ipoli.android.ApiConstants;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */

public class GoogleAuthService implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private final GoogleSignInOptions gso;

    public GoogleAuthService() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ApiConstants.WEB_SERVER_GOOGLE_PLUS_CLIENT_ID)
                .requestEmail()
                .build();
    }

    public void getIdToken(Context context, TokenListener tokenListener) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();

        OptionalPendingResult<GoogleSignInResult> i = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (i.isDone()) {
            tokenListener.onIdTokenReceived(i.get().getSignInAccount().getIdToken());
            googleApiClient.disconnect();
        } else {
            i.setResultCallback(googleSignInResult -> {
                tokenListener.onIdTokenReceived(googleSignInResult.getSignInAccount().getIdToken());
                googleApiClient.disconnect();
            });
        }
    }

    public Intent getSignInIntent(AppCompatActivity activity) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        return Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("AAAA", "on connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public interface TokenListener {
        void onIdTokenReceived(String idToken);
    }
}
