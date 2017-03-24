package io.ipoli.android.app.api.exceptions;

import io.ipoli.android.app.App;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/24/17.
 */

public class ApiResponseException extends Exception {

    public ApiResponseException(String url, int code, String message) {
        super("Request of player with id " + App.getPlayerId() + " to " + url + " returned response " + code + " " + message);
    }
}
