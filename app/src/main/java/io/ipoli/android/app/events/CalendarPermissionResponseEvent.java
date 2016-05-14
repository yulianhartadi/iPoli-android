package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/14/16.
 */
public class CalendarPermissionResponseEvent {
    public final Response response;
    public final EventSource source;

    public enum Response {
        GRANTED, DENIED;
    }
    public CalendarPermissionResponseEvent(Response response, EventSource source) {
        this.response = response;
        this.source = source;
    }
}
