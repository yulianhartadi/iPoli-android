package com.curiousily.ipoli.app.api.events;

import retrofit.RetrofitError;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class APIErrorEvent {
    public final RetrofitError error;

    public APIErrorEvent(RetrofitError error) {
        this.error = error;
    }
}
