package com.curiousily.ipoli.snippet;

import com.curiousily.ipoli.user.User;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class Snippet {

    public String text;

    public User createdBy;

    public Status status;

    public enum Status {
        WAITING, PROCESSED
    }
}
