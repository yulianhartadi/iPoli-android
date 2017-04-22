package io.ipoli.android.app.auth;

import com.facebook.AccessToken;
import com.squareup.otto.Bus;

import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.exceptions.SignInException;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */

public class FacebookAuthService {

    private final Bus eventBus;

    public FacebookAuthService(Bus eventBus) {
        this.eventBus = eventBus;
    }

    public String getAccessToken(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken == null) {
            eventBus.post(new AppErrorEvent(new SignInException("Facebook access token in null")));
        }
        return accessToken != null ? accessToken.getToken() : null;
    }

}
