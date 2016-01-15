package io.ipoli.android.assistant.events;

import android.app.Activity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/14/16.
 */
public class AssistantStartActivityEvent {
    public final Class<? extends Activity> clazz;

    public AssistantStartActivityEvent(Class<? extends Activity> clazz) {
        this.clazz = clazz;
    }
}
