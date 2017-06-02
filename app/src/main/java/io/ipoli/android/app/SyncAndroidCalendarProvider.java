package io.ipoli.android.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.utils.DateUtils;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.android.calendar.Instance;
import me.everything.providers.android.calendar.Reminder;
import me.everything.providers.core.Data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class SyncAndroidCalendarProvider extends CalendarProvider {

    private final ContentResolver contentResolver;

    public SyncAndroidCalendarProvider(Context context) {
        super(context);
        contentResolver = context.getContentResolver();
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

    private static final String[] INSTANCE_PROJECTION = new String[]{
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.START_MINUTE,
            CalendarContract.Instances.START_DAY
    };

    private static final int PROJECTION_EVENT_ID_INDEX = 0;
    private static final int PROJECTION_START_MINUTE_INDEX = 1;
    private static final int PROJECTION_START_DAY_INDEX = 2;

    public static class InstanceData {

        private final long eventId;
        private final int startMinute;
        private final int startDay;

        public InstanceData(long eventId, int startMinute, int startDay) {
            this.eventId = eventId;
            this.startMinute = startMinute;
            this.startDay = startDay;
        }
    }

    public Map<Event, List<Instance>> getCalendarEvents(LocalDate startDate, LocalDate endDate, String calendarId) {

        // @TODO query all calendars
        String selection = CalendarContract.Instances.CALENDAR_ID + "=?";
        String[] selectionArgs = new String[]{calendarId};
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, DateUtils.toMillis(startDate));
        ContentUris.appendId(builder, DateUtils.toMillis(endDate));

        Cursor cur = contentResolver.query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, null);
        if (cur == null) {
            return new HashMap<>();
        }
        try {
            while (cur.moveToNext()) {
                long eventId = cur.getLong(PROJECTION_EVENT_ID_INDEX);
                int startMinute = cur.getInt(PROJECTION_START_MINUTE_INDEX);
                int startDay = cur.getInt(PROJECTION_START_DAY_INDEX);
                InstanceData instanceData = new InstanceData(eventId, startMinute, startDay);
            }
        } catch (Exception e) {
            // @TODO handle it
        } finally {
            cur.close();
        }

        Data<Instance> instancesData = getInstances(DateUtils.toMillis(startDate), DateUtils.toMillis(endDate));
        if (instancesData == null) {
            return new HashMap<>();
        }

        Map<Event, List<Instance>> result = new HashMap<>();
        Map<Long, Event> idToEvent = new HashMap<>();

        for (Instance i : instancesData.getList()) {
            if (idToEvent.containsKey(i.eventId)) {
                result.get(idToEvent.get(i.eventId)).add(i);
            } else {
                Event e = getEvent(i.eventId);
                idToEvent.put(e.id, e);
                List<Instance> instances = new ArrayList<>();
                instances.add(i);
                result.put(e, instances);
            }
        }
        return result;
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
