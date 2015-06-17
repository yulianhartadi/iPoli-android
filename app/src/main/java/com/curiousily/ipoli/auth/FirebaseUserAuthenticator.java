package com.curiousily.ipoli.auth;

import com.curiousily.ipoli.FirebaseConstants;
import com.curiousily.ipoli.models.User;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class FirebaseUserAuthenticator {

    private static final Firebase firebase = new Firebase(FirebaseConstants.URL);

    public void authenticateAnonymousUser(final AuthListener listener) {
        AuthData authData = firebase.getAuth();
        if (authData != null) {
            callCallback(listener, authData);
            return;
        }

        firebase.authAnonymously(new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                callCallback(listener, authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                listener.onUnableToAuthenticateUser();
            }
        });
    }

    private void callCallback(AuthListener listener, AuthData authData) {
        listener.onUserAuthenticated(new User(authData.getUid()));
    }

    public static User getUser() {
        return new User(firebase.getAuth().getUid());
    }
}
