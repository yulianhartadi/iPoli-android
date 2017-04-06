package io.ipoli.android.quest.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class SourceMapping {
    private String calendarId;

    private String androidCalendar;

    private SourceMapping() {
    }

    public static SourceMapping fromGoogleCalendar(long calendarId, long eventId) {
        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.calendarId = String.valueOf(calendarId);
        sourceMapping.androidCalendar = String.valueOf(eventId);
        return sourceMapping;
    }

    public String getAndroidCalendar() {
        return androidCalendar;
    }

    public void setAndroidCalendar(String googleCalendar) {
        this.androidCalendar = googleCalendar;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }
}
