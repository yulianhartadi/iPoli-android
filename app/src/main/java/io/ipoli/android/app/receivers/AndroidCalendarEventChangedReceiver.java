package io.ipoli.android.app.receivers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.SyncRequestEvent;
import io.ipoli.android.app.providers.SyncAndroidCalendarProvider;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.core.Data;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class AndroidCalendarEventChangedReceiver extends AsyncBroadcastReceiver {
    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    Bus eventBus;

    @Override
    protected Observable<Void> doOnReceive(Context context, Intent intent) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return Observable.empty();
        }

        App.getAppComponent(context).inject(this);

        SyncAndroidCalendarProvider provider = new SyncAndroidCalendarProvider(context);
        LocalStorage localStorage = LocalStorage.of(context);
        Set<String> calendarIds = localStorage.readStringSet(Constants.KEY_SELECTED_ANDROID_CALENDARS);
        return Observable.defer(() -> {
            List<Event> dirtyEvents = new ArrayList<>();
            List<Event> deletedEvents = new ArrayList<>();
            for (String cid : calendarIds) {
                int calendarId = Integer.valueOf(cid);
                addDirtyEvents(provider, calendarId, dirtyEvents);
                addDeletedEvents(calendarId, provider, deletedEvents);
            }
            markEventsForUpdate(dirtyEvents, localStorage);
            return deleteEvents(deletedEvents).flatMap(ignored -> {
                eventBus.post(new SyncRequestEvent());
                return Observable.<Void>empty();
            });
        }).compose(applyAndroidSchedulers());
    }

    private void addDeletedEvents(int calendarId, SyncAndroidCalendarProvider provider, List<Event> deletedEvents) {
        Data<Event> deletedEventsData = provider.getDeletedEvents(calendarId);
        Cursor deletedEventsCursor = deletedEventsData.getCursor();

        while (deletedEventsCursor.moveToNext()) {
            Event e = deletedEventsData.fromCursor(deletedEventsCursor, CalendarContract.Events._ID,
                    CalendarContract.Events.RRULE,
                    CalendarContract.Events.RDATE);
            deletedEvents.add(e);
        }
        deletedEventsCursor.close();
    }

    private void addDirtyEvents(SyncAndroidCalendarProvider provider, int calendarId, List<Event> dirtyEvents) {
        Data<Event> dirtyEventsData = provider.getDirtyEvents(calendarId);
        Cursor dirtyEventsCursor = dirtyEventsData.getCursor();

        while (dirtyEventsCursor.moveToNext()) {
            Event e = dirtyEventsData.fromCursor(dirtyEventsCursor, CalendarContract.Events._ID,
                    CalendarContract.Events.RRULE,
                    CalendarContract.Events.RDATE);
            dirtyEvents.add(e);
        }

        dirtyEventsCursor.close();
    }

    private void markEventsForUpdate(List<Event> dirtyEvents, LocalStorage localStorage) {
        Set<String> repeatingQuestIds = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE);
        Set<String> questIds = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE);
        for (Event e : dirtyEvents) {
            if (isRecurrentAndroidCalendarEvent(e)) {
                repeatingQuestIds.add(String.valueOf(e.id));
            } else {
                questIds.add(String.valueOf(e.id));
            }
        }
        localStorage.saveStringSet(Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE, repeatingQuestIds);
        localStorage.saveStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE, questIds);
    }

    private Observable<List<String>> deleteEvents(List<Event> events) {
        return Observable.from(events).flatMap(e -> {
            if (isRecurrentAndroidCalendarEvent(e)) {
                return repeatingQuestPersistenceService.deleteBySourceMappingId("googleCalendar", String.valueOf(e.id));
            } else {
                return questPersistenceService.deleteBySourceMappingId("googleCalendar", String.valueOf(e.id));
            }
        }).toList();
    }

    private boolean isRecurrentAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }

    private <T> Observable.Transformer<T, T> applyAndroidSchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
