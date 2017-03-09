package io.ipoli.android.quest.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class SourceMapping {

    private String androidCalendar;

    private SourceMapping() {
    }

    public static SourceMapping fromGoogleCalendar(long eventId) {
        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.androidCalendar = String.valueOf(eventId);
        return sourceMapping;
    }

    public String getAndroidCalendar() {
        return androidCalendar;
    }

    public void setAndroidCalendar(String googleCalendar) {
        this.androidCalendar = String.valueOf(googleCalendar);
    }
}
