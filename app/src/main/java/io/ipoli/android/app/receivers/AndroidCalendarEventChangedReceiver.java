package io.ipoli.android.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;

import io.ipoli.android.app.providers.SyncAndroidCalendarProvider;
import io.ipoli.android.quest.data.Quest;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.core.Data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class AndroidCalendarEventChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CalendarReceiver", "EventChangedReceiver");
        SyncAndroidCalendarProvider provider = new SyncAndroidCalendarProvider(context);
        Data<Event> events = provider.getDirtyEvents(1);
        Cursor cursor = events.getCursor();

        String[] columns = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DURATION,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.RRULE,
                CalendarContract.Events.RDATE,
                CalendarContract.Events.EVENT_TIMEZONE,
                CalendarContract.Events.EVENT_END_TIMEZONE,
                CalendarContract.Events.ORIGINAL_ID,
                CalendarContract.Events.ORIGINAL_SYNC_ID
        };

        while (cursor.moveToNext()) {
            Event e = events.fromCursor(cursor, columns);
//            if (!TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate)) {
            Quest q = new Quest(e.title);
            DateTime startDateTime = new DateTime(e.dTStart, DateTimeZone.forID(e.eventTimeZone));
            DateTime endDateTime = new DateTime(e.dTend, DateTimeZone.forID(e.eventTimeZone));
            q.setId(String.valueOf(e.id));
//                Duration dur = new Duration();
//                dur.setValue(e.duration);

            q.setDuration(Minutes.minutesBetween(startDateTime, endDateTime).getMinutes());
            q.setStartMinute(startDateTime.getMinuteOfDay());
            q.setEndDate(startDateTime.toLocalDate().toDate());
            q.setSource("google-calendar");
//                Log.d("CalendarSyncEvent", e.title + " " + new Date(e.dTStart) + " " + e.eventTimeZone + " endtz: " + e.eventEndTimeZone);
//                Log.d("CalendarSyncEvent", " " + q.getId());
            Log.d("ReceiverSyncEvent", " " + q.getName());
            Log.d("ReceiverSyncEvent", " " + e.rRule);
            Log.d("ReceiverSyncEvent", " " + e.id);
            Log.d("ReceiverSyncEvent", " " + e.originalId);
            Log.d("ReceiverSyncEvent", " " + e.dTStart);
            Log.d("ReceiverSyncEvent", " " + e.duration);
//                Log.d("CalendarSyncEvent", " " + q.getStartMinute());
//                Log.d("CalendarSyncEvent", " " + q.getEndDate());
//                Log.d("CalendarSyncEvent", " " + q.getDuration());
//                Log.d("CalendarSyncEvent", " " + e.duration);
//                Log.d("CalendarSyncEvent", " " + TimeUnit.MILLISECONDS.toMinutes(dur.getDuration().getTime(new Date(0)).getTime()));
//                Log.d("CalendarSyncEvent", " " + dur.getDuration().getHours());
//                Log.d("CalendarSyncEvent", " " + dur.getDuration().getDays());
//                Log.d("CalendarSyncEvent", " " + dur.getDuration().getSeconds());
//                Log.d("CalendarSyncEvent", " " + dur.getDuration().getWeeks());
//                Log.d("CalendarSyncEvent", " " + startDateTime);
//                Log.d("CalendarSyncEvent", " " + endDateTime);
//            }
//                T t = Entity.create(mCursor, mCls);
//                data.add(t);
        }

    }
}
