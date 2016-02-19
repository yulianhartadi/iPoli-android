package io.ipoli.android.app.ui.calendar;

import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public abstract class BaseCalendarAdapter<E extends CalendarEvent> {

    protected CalendarDayView calendarDayView;

    abstract public List<E> getEvents();

    abstract public View getView(ViewGroup parent, int position);

    abstract public void onStartTimeUpdated(E calendarEvent, Date oldStartTime);

    void setCalendarDayView(CalendarDayView calendarDayView) {
        this.calendarDayView = calendarDayView;
    }

    public void notifyDataSetChanged() {
        calendarDayView.removeAllEvents();
        for (int i = 0; i < getEvents().size(); i++) {
            calendarDayView.addEvent(getEvents().get(i), i);
        }
    }
}
