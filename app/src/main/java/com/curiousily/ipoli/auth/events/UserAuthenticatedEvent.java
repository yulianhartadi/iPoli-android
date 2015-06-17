package com.curiousily.ipoli.auth.events;

import com.curiousily.ipoli.models.User;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class UserAuthenticatedEvent {
    private final User user;

    public UserAuthenticatedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
