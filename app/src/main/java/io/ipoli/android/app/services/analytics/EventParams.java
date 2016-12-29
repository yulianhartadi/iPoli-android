package io.ipoli.android.app.services.analytics;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */

public class EventParams {
    private JSONObject params = new JSONObject();

    private EventParams() {
    }

    public static EventParams create() {
        return new EventParams();
    }

    public static EventParams of(String key, int value) {
        return of(key, String.valueOf(value));
    }

    public static EventParams of(String key, String value) {
        EventParams eventParams = new EventParams();
        eventParams.add(key, value);
        return eventParams;
    }

    public static EventParams of(String key, boolean value) {
        return of(key, String.valueOf(value));
    }

    public EventParams add(String key, String value) {
        try {
            params.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public EventParams add(String key, int value) {
        return add(key, String.valueOf(value));
    }

    public EventParams add(String key, long value) {
        return add(key, String.valueOf(value));
    }

    public JSONObject getParams() {
        return params;
    }


}
