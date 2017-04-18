package io.ipoli.android.app;

import android.content.Context;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.List;

import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.android.calendar.Reminder;
import me.everything.providers.core.Data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class SyncAndroidCalendarProvider extends CalendarProvider {

    public SyncAndroidCalendarProvider(Context context) {
        super(context);
    }

    public List<Event> getDirtyEvents(long calendarId) {
        String selection = CalendarContract.Events.CALENDAR_ID + " = ? AND "
                + CalendarContract.Events.DIRTY + "= ? AND "
                + CalendarContract.Events.DELETED + "= ?";
        String[] selectionArgs = new String[]{String.valueOf(calendarId), String.valueOf(1), String.valueOf(0)};
        Data<Event> data = getContentTableData(Event.uri, selection, selectionArgs, null, Event.class);
        return data == null ? new ArrayList<>() : data.getList();
    }

    public List<Event> getDeletedEvents(long calendarId) {
        String selection = CalendarContract.Events.CALENDAR_ID + " = ? AND "
                + CalendarContract.Events.DELETED + "= ?";
        String[] selectionArgs = new String[]{String.valueOf(calendarId), String.valueOf(1)};
        Data<Event> data = getContentTableData(Event.uri, selection, selectionArgs, null, Event.class);
        return data == null ? new ArrayList<>() : data.getList();
    }

    public List<Event> getCalendarEvents(long calendarId) {
        return getEvents(calendarId).getList();
    }

    public List<Calendar> getAndroidCalendars() {
        Data<Calendar> data = getCalendars();
        return data == null ? new ArrayList<>() : data.getList();
    }

    public List<Reminder> getEventReminders(long eventId) {
        Data<Reminder> data = getReminders(eventId);
        return data == null ? new ArrayList<>() : data.getList();
    }

}
