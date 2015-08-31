package com.curiousily.ipoli.user.events;

import com.curiousily.ipoli.user.User;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/31/15.
 */
public class UserLoadedEvent {
    public final User user;

    public UserLoadedEvent(User user) {
        this.user = user;
    }
}
