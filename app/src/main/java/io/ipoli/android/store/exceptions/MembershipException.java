package io.ipoli.android.store.exceptions;

import org.solovyev.android.checkout.ResponseCodes;

import io.ipoli.android.app.App;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/8/16.
 */
public class MembershipException extends Exception {

    public MembershipException(String action, int responseCode, Throwable causes) {
        super("Player with id " + App.getPlayerId() + " was unable to " + action + " with response code "
                + ResponseCodes.toString(responseCode) + " " + responseCode, causes);
    }
}
