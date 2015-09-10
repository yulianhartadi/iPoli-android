package com.curiousily.ipoli.input;

import com.curiousily.ipoli.user.User;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class Input {

    public String text;

    public User createdBy;

    public Status status;

    public enum Status {
        WAITING, PROCESSED
    }
}
