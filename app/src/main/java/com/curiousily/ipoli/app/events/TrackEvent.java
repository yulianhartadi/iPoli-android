package com.curiousily.ipoli.app.events;

import java.util.Map;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/15.
 */
public class TrackEvent {
    private Map<String, String> event;

    public static TrackEvent from(Map<String, String> parameters) {
        TrackEvent e = new TrackEvent();
        e.event = parameters;
        return e;
    }

    public Map<String, String> getEvent() {
        return event;
    }
}
