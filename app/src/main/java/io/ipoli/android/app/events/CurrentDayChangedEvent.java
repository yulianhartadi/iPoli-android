package io.ipoli.android.app.events;

import org.joda.time.LocalDate;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/1/16.
 */
public class CurrentDayChangedEvent {
    public final LocalDate date;
    public final Source source;

    public CurrentDayChangedEvent(LocalDate date, Source source) {
        this.date = date;
        this.source = source;
    }

    public enum Source {SWIPE, CALENDAR}
}
