package io.ipoli.android.app.ui.calendar;

import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/20/16.
 */
public interface CalendarAdapter<E extends CalendarEvent> {
    List<E> getEvents();

    View getView(ViewGroup parent, int position);

    void onStartTimeUpdated(E calendarEvent, Date oldStartTime);

    void notifyDataSetChanged();

    void updateEvents(List<E> calendarEvents);
}
