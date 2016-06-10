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
import io.ipoli.android.app.events.ServerSyncRequestEvent;
import io.ipoli.android.app.providers.SyncAndroidCalendarProvider;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListReader;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListReader;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmRepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import me.everything.providers.android.calendar.CalendarProvider;
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

    QuestPersistenceService questPersistenceService;

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
        questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);
        repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, realm);
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
            createOrUpdateEvents(dirtyEvents, context);
            deleteEvents(deletedEvents);
            eventBus.post(new SyncCompleteEvent());
            eventBus.post(new ServerSyncRequestEvent());
            return Observable.<Void>empty();
        }).compose(applyAndroidSchedulers());
    }

    private void addDeletedEvents(int calendarId, SyncAndroidCalendarProvider provider, List<Event> deletedEvents) {
        Data<Event> deletedEventsData = provider.getDeletedEvents(calendarId);
        Cursor deletedEventsCursor = deletedEventsData.getCursor();

        while (deletedEventsCursor.moveToNext()) {
            Event e = deletedEventsData.fromCursor(deletedEventsCursor, CalendarContract.Events._ID);
            deletedEvents.add(e);
        }
        deletedEventsCursor.close();
    }

    private void addDirtyEvents(SyncAndroidCalendarProvider provider, int calendarId, List<Event> dirtyEvents) {
        Data<Event> dirtyEventsData = provider.getDirtyEvents(calendarId);
        Cursor dirtyEventsCursor = dirtyEventsData.getCursor();

        while (dirtyEventsCursor.moveToNext()) {
            Event e = dirtyEventsData.fromCursor(dirtyEventsCursor, CalendarContract.Events._ID);
            dirtyEvents.add(e);
        }

        dirtyEventsCursor.close();
    }

    private void createOrUpdateEvents(List<Event> dirtyEvents, Context context) {

        AndroidCalendarQuestListReader questReader = new AndroidCalendarQuestListReader(questPersistenceService, repeatingQuestPersistenceService);
        AndroidCalendarRepeatingQuestListReader repeatingQuestReader = new AndroidCalendarRepeatingQuestListReader(repeatingQuestPersistenceService);
        List<Event> repeating = new ArrayList<>();
        List<Event> nonRepeating = new ArrayList<>();
        CalendarProvider calendarProvider = new CalendarProvider(context);
        for (Event e : dirtyEvents) {
            Event event = calendarProvider.getEvent(e.id);
            if (isRepeatingAndroidCalendarEvent(event)) {
                repeating.add(event);
            } else {
                nonRepeating.add(event);
            }
        }
        questPersistenceService.saveSync(questReader.read(nonRepeating));
        repeatingQuestPersistenceService.saveSync(repeatingQuestReader.read(repeating));
    }

    private void deleteEvents(List<Event> events) {
        for (Event e : events) {
            if (isRepeatingAndroidCalendarEvent(e)) {
                RepeatingQuest repeatingQuest = repeatingQuestPersistenceService.findByExternalSourceMappingIdSync(Constants.EXTERNAL_SOURCE_ANDROID_CALENDAR, String.valueOf(e.id));
                if (repeatingQuest == null) {
                    continue;
                }
                repeatingQuest.markDeleted();
                repeatingQuestPersistenceService.saveSync(repeatingQuest);
            } else {
                Quest quest = questPersistenceService.findByExternalSourceMappingIdSync(Constants.EXTERNAL_SOURCE_ANDROID_CALENDAR, String.valueOf(e.id));
                if (quest == null) {
                    continue;
                }
                quest.markDeleted();
                questPersistenceService.saveSync(quest);
            }
        }
    }

    private boolean isRepeatingAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }

    private <T> Observable.Transformer<T, T> applyAndroidSchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}