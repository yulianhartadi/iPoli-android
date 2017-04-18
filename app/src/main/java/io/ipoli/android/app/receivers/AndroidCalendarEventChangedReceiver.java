package io.ipoli.android.app.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.App;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import me.everything.providers.android.calendar.Event;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class AndroidCalendarEventChangedReceiver extends BroadcastReceiver {

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    SyncAndroidCalendarProvider syncAndroidCalendarProvider;

    @Inject
    AndroidCalendarEventParser androidCalendarEventParser;

    @Inject
    CalendarPersistenceService calendarPersistenceService;

    @Inject
    Bus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AAAA", "receive");
        if (!EasyPermissions.hasPermissions(context, Manifest.permission.READ_CALENDAR)) {
            return;
        }

        App.getAppComponent(context).inject(this);
        Player player = playerPersistenceService.get();

        List<Quest> quests = new ArrayList<>();
        Map<Quest, Long> questToOriginalId = new HashMap<>();
        List<RepeatingQuest> repeatingQuests = new ArrayList<>();

        for (Map.Entry<Long, Category> calendar : player.getAndroidCalendars().entrySet()) {
            List<Event> dirtyEvents = syncAndroidCalendarProvider.getDirtyEvents(calendar.getKey());
            AndroidCalendarEventParser.Result result = androidCalendarEventParser.parse(dirtyEvents, calendar.getValue());
            quests.addAll(result.quests);
            questToOriginalId.putAll(result.questToOriginalId);
            repeatingQuests.addAll(result.repeatingQuests);
        }


        for(RepeatingQuest rq : repeatingQuests) {
            RepeatingQuest existingRepeatingQuest = repeatingQuestPersistenceService.findNotCompletedFromAndroidCalendar(rq.getSourceMapping().getAndroidCalendarMapping());
            if(existingRepeatingQuest != null) {
                rq.setId(existingRepeatingQuest.getId());
                rq.setCreatedAt(existingRepeatingQuest.getCreatedAt());
            }
        }

        for(Quest q : quests) {
            Quest existingQuest = questPersistenceService.findNotCompletedFromAndroidCalendar(q.getSourceMapping().getAndroidCalendarMapping());
            if(existingQuest != null) {
                q.setId(existingQuest.getId());
                q.setCreatedAt(existingQuest.getCreatedAt());
            }
        }

//        calendarPersistenceService.updateAsync(quests, questToOriginalId, repeatingQuests);




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
//        androidCalendarQuestService.saveSync(nonRepeating);
//        androidCalendarRepeatingQuestService.saveSync(repeating);
//    }
//
//    private void deleteEvents(List<Event> events) {
//        for (Event e : events) {
//            if (isRepeatingAndroidCalendarEvent(e)) {
//                repeatingQuestPersistenceService.findByExternalSourceMappingId(Constants.EXTERNAL_SOURCE_ANDROID_CALENDAR, String.valueOf(e.id), repeatingQuest -> {
//                    if (repeatingQuest == null) {
//                        return;
//                    }
//                    repeatingQuestPersistenceService.updateCalendars(repeatingQuest);
//                });
//            } else {
//                questPersistenceService.findByExternalSourceMappingId(Constants.EXTERNAL_SOURCE_ANDROID_CALENDAR, String.valueOf(e.id), quest -> {
//                    if (quest == null) {
//                        return;
//                    }
//                    questPersistenceService.updateCalendars(quest);
//                });
//            }
//        }
//    }
//
//    private boolean isRepeatingAndroidCalendarEvent(Event e) {
//        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
//    }
}