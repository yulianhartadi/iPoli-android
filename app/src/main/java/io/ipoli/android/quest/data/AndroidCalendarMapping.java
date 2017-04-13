package io.ipoli.android.quest.data;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/10/17.
 */

public class AndroidCalendarMapping {
    private Long calendarId;
    private Long eventId;

    public AndroidCalendarMapping() {
    }

    public AndroidCalendarMapping(Long calendarId, Long eventId) {
        this.calendarId = calendarId;
        this.eventId = eventId;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
