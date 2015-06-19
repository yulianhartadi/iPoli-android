package com.curiousily.ipoli.assistant.handlers;


import android.content.Context;

import com.curiousily.ipoli.EventBus;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public abstract class AbstractIntentHandler implements IntentHandler {


    protected final Context context;

    public AbstractIntentHandler(Context context) {
        this.context = context;
    }


    protected void post(Object e) {
        EventBus.get().post(e);
    }

}
