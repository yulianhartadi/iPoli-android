package io.ipoli.android.app.ui.calendar;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/20/16.
 */
public interface CalendarAdapter<E extends CalendarEvent> {
    List<E> getEvents();

    View getView(ViewGroup parent, int position);

    void onStartTimeUpdated(E calendarEvent, int oldStartTime);

    void notifyDataSetChanged();

    void updateEvents(List<E> calendarEvents);

    void onDragStarted(View dragView, Time time);

    void onDragEnded(View dragView);

    void removeEvent(E calendarEvent);

    void onDragMoved(View dragView, Time time);
}
