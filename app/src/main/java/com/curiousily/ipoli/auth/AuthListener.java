package com.curiousily.ipoli.auth;

import com.curiousily.ipoli.models.User;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public interface AuthListener {
    void onUserAuthenticated(User user);
    void onUnableToAuthenticateUser();
}
