package io.ipoli.android.quest.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class SourceMapping {
    private AndroidCalendarMapping androidCalendarMapping;

    private SourceMapping() {
    }

    public static SourceMapping fromGoogleCalendar(long calendarId, long eventId) {
        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.androidCalendarMapping = new AndroidCalendarMapping(calendarId, eventId);
        return sourceMapping;
    }

    public AndroidCalendarMapping getAndroidCalendarMapping() {
        return androidCalendarMapping;
    }

    public void setAndroidCalendarMapping(AndroidCalendarMapping androidCalendarMapping) {
        this.androidCalendarMapping = androidCalendarMapping;
    }
}
