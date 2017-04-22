package io.ipoli.android.app.exceptions;

import io.ipoli.android.app.App;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/29/17.
 */

public class SignInException extends Exception {
    public SignInException(String message) {
        super(message + ". Player with id: " + App.getPlayerId());
    }
}
