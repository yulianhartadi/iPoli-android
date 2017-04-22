package io.ipoli.android.app.events;

import java.util.Map;

import io.ipoli.android.quest.data.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/14/16.
 */
public class SyncCalendarRequestEvent {
    public final Map<Long, Category> selectedCalendars;
    public final EventSource source;

    public SyncCalendarRequestEvent(Map<Long, Category> selectedCalendars, EventSource source) {
        this.selectedCalendars = selectedCalendars;
        this.source = source;
    }
}
