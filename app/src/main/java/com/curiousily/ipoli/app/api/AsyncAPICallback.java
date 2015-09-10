package com.curiousily.ipoli.app.api;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.app.api.events.APIErrorEvent;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public abstract class AsyncAPICallback<T> implements Callback<T> {

    @Override
    public void failure(RetrofitError error) {
        EventBus.post(new APIErrorEvent(error));
    }
}
