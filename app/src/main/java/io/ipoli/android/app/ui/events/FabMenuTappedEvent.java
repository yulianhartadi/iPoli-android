package io.ipoli.android.app.ui.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 10/8/16.
 */
public class FabMenuTappedEvent {
    public final String name;
    public final EventSource source;

    public FabMenuTappedEvent(String name, EventSource source) {
        this.name = name;
        this.source = source;
    }
}
