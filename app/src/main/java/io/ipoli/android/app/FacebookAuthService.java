package io.ipoli.android.app;

import com.facebook.AccessToken;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */

public class FacebookAuthService {

    public String getAccessToken(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null ? accessToken.getToken() : null;
    }

}
