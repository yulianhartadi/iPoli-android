package com.curiousily.ipoli.user.events;

import com.curiousily.ipoli.user.User;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/31/15.
 */
public class UserLoadedEvent {
    public final User user;
    public final boolean isNewUser;

    public UserLoadedEvent(User user, boolean isNewUser) {
        this.user = user;
        this.isNewUser = isNewUser;
    }
}
