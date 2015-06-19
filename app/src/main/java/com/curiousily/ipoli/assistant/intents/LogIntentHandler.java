package com.curiousily.ipoli.assistant.intents;

import android.content.Context;
import android.util.Log;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.assistant.intents.events.IntentProcessedEvent;

import java.util.HashMap;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class LogIntentHandler extends AbstractIntentHandler {

    public LogIntentHandler(Context context) {
        super(context);
    }

    @Override
    public void process(String task) {
        Log.d("PoliVoice", "log " + task);
        HashMap<String, String> log = new HashMap<>();
        log.put("message", task);
//        log.put("user", getUserId());
//        storageManager.save("logs", log);
        post(new IntentProcessedEvent(context.getString(R.string.log_recorded_response)));
    }

    @Override
    public boolean canHandle(String task) {
        return task.toLowerCase().startsWith("record");
    }
}
