package io.ipoli.android.app.ui.calendar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public interface CalendarListener<E extends CalendarEvent> {
    void onUnableToAcceptNewEvent(E calendarEvent);

    void onAcceptEvent(E calendarEvent);
}
