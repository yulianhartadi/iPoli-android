package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/2/16.
 */
public class ItemActionsShownEvent {
    public final EventSource source;

    public ItemActionsShownEvent(EventSource source) {
        this.source = source;
    }
}
