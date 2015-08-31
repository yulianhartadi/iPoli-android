package com.curiousily.ipoli.user;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.curiousily.ipoli.Constants;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class User {
    public final String id;

    public User(String id) {
        this.id = id;
    }

    public static User getCurrent(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(!preferences.contains(Constants.KEY_USER_ID)) {
            return null;
        }
        return new User(preferences.getString(Constants.KEY_USER_ID, ""));
    }
}
