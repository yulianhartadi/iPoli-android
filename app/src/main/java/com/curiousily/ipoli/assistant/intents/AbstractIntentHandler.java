package com.curiousily.ipoli.assistant.intents;


import android.content.Context;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.auth.FirebaseUserAuthenticator;
import com.curiousily.ipoli.assistant.data.StorageManager;

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
