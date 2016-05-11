package io.ipoli.android.app.receivers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.providers.SyncAndroidCalendarProvider;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.core.Data;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class AndroidCalendarEventChangedReceiver extends AsyncBroadcastReceiver {
    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    @Override
    protected Observable<Void> doOnReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);

        Log.d("CalendarReceiver", "EventChangedReceiver");

        SyncAndroidCalendarProvider provider = new SyncAndroidCalendarProvider(context);
        LocalStorage localStorage = LocalStorage.of(context);
        Set<String> calendarIds = localStorage.readStringSet(Constants.KEY_SELECTED_GOOGLE_CALENDARS);

        for (String cid : calendarIds) {
            List<Event> events = provider.getDeletedEvents(Integer.valueOf(cid)).getList();
            deleteEvents(events);
        }


        Data<Event> eventsToUpdate = provider.getDirtyEvents(1);
        Cursor cursor = eventsToUpdate.getCursor();

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
            Event e = eventsToUpdate.fromCursor(cursor, columns);
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
        return null;
    }

    private void deleteEvents(List<Event> events) {
        for(Event e : events) {
            if(isRecurrentAndroidCalendarEvent(e)) {
                recurrentQuestPersistenceService.deleteByExternalSourceMappingId("googleCalendar", String.valueOf(e.id));
            } else {
                questPersistenceService.deleteByExternalSourceMappingId("googleCalendar", String.valueOf(e.id));
            }
        }
    }

    private boolean isRecurrentAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }
}
