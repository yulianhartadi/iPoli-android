package io.ipoli.android.app.providers;

import android.content.Context;
import android.provider.CalendarContract;

import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.core.Data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class SyncAndroidCalendarProvider extends CalendarProvider {

    public SyncAndroidCalendarProvider(Context context) {
        super(context);
    }

    public Data<Event> getDirtyEvents(long calendarId) {
        String selection = CalendarContract.Events.CALENDAR_ID + " = ? AND "
                + CalendarContract.Events.DIRTY + "= ?";
        String[] selectionArgs = new String[]{String.valueOf(calendarId), String.valueOf(1)};
        return getContentTableData(Event.uri, selection, selectionArgs, null, Event.class);
    }

}
