package io.ipoli.android.app.ui.calendar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public abstract class BaseCalendarAdapter<E extends CalendarEvent> implements CalendarAdapter<E> {

    protected CalendarDayView calendarDayView;

    void setCalendarDayView(CalendarDayView calendarDayView) {
        this.calendarDayView = calendarDayView;
    }

    @Override
    public void notifyDataSetChanged() {
        calendarDayView.removeAllEvents();
        for (int i = 0; i < getEvents().size(); i++) {
            calendarDayView.addEvent(getEvents().get(i), i);
        }
    }
}
