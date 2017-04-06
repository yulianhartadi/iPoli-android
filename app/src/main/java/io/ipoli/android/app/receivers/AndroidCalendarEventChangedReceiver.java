package io.ipoli.android.app.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.app.App;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import me.everything.providers.android.calendar.Event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class AndroidCalendarEventChangedReceiver extends BroadcastReceiver {

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

//    @Inject
//    AndroidCalendarQuestListPersistenceService androidCalendarQuestService;
//
//    @Inject
//    AndroidCalendarRepeatingQuestListPersistenceService androidCalendarRepeatingQuestService;

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AAAA", "receive");
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        App.getAppComponent(context).inject(this);
        SyncAndroidCalendarProvider provider = new SyncAndroidCalendarProvider(context);
        List<Event> dirtyEvents = provider.getDirtyEvents(1).getList();
        for(Event e : dirtyEvents) {
            Log.d("AAAA event", e.title);
        }
//        Set<String> calendarIds = localStorage.readStringSet(Constants.KEY_SELECTED_ANDROID_CALENDARS);
//        List<Event> dirtyEvents = new ArrayList<>();
//        List<Event> deletedEvents = new ArrayList<>();
//        for (String cid : calendarIds) {
//            int calendarId = Integer.valueOf(cid);
//            addDirtyEvents(provider, calendarId, dirtyEvents);
//            addDeletedEvents(calendarId, provider, deletedEvents);
//        }
//        createOrUpdateEvents(dirtyEvents, context);
//        deleteEvents(deletedEvents);
    }

//    private void addDeletedEvents(int calendarId, SyncAndroidCalendarProvider provider, List<Event> deletedEvents) {
//        Data<Event> deletedEventsData = provider.getDeletedEvents(calendarId);
//        Cursor deletedEventsCursor = deletedEventsData.getCursor();
//
//        while (deletedEventsCursor.moveToNext()) {
//            Event e = deletedEventsData.fromCursor(deletedEventsCursor, CalendarContract.Events._ID);
//            deletedEvents.add(e);
//        }
//        deletedEventsCursor.close();
//    }
//
//    private void addDirtyEvents(SyncAndroidCalendarProvider provider, int calendarId, List<Event> dirtyEvents) {
//        Data<Event> dirtyEventsData = provider.getDirtyEvents(calendarId);
//        Cursor dirtyEventsCursor = dirtyEventsData.getCursor();
//
//        while (dirtyEventsCursor.moveToNext()) {
//            Event e = dirtyEventsData.fromCursor(dirtyEventsCursor, CalendarContract.Events._ID);
//            dirtyEvents.add(e);
//        }
//
//        dirtyEventsCursor.close();
//    }
//
//    private void createOrUpdateEvents(List<Event> dirtyEvents, Context context) {
//        List<Event> repeating = new ArrayList<>();
//        List<Event> nonRepeating = new ArrayList<>();
//        CalendarProvider calendarProvider = new CalendarProvider(context);
//        for (Event e : dirtyEvents) {
//            Event event = calendarProvider.getEvent(e.id);
//            if (isRepeatingAndroidCalendarEvent(event)) {
//                repeating.add(event);
//            } else {
//                nonRepeating.add(event);
//            }
//        }
//
//        androidCalendarQuestService.save(nonRepeating);
//        androidCalendarRepeatingQuestService.save(repeating);
//    }
//
//    private void deleteEvents(List<Event> events) {
//        for (Event e : events) {
//            if (isRepeatingAndroidCalendarEvent(e)) {
//                repeatingQuestPersistenceService.findByExternalSourceMappingId(Constants.EXTERNAL_SOURCE_ANDROID_CALENDAR, String.valueOf(e.id), repeatingQuest -> {
//                    if (repeatingQuest == null) {
//                        return;
//                    }
//                    repeatingQuestPersistenceService.delete(repeatingQuest);
//                });
//            } else {
//                questPersistenceService.findByExternalSourceMappingId(Constants.EXTERNAL_SOURCE_ANDROID_CALENDAR, String.valueOf(e.id), quest -> {
//                    if (quest == null) {
//                        return;
//                    }
//                    questPersistenceService.delete(quest);
//                });
//            }
//        }
//    }
//
//    private boolean isRepeatingAndroidCalendarEvent(Event e) {
//        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
//    }
}