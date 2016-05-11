package io.ipoli.android.app.receivers;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.providers.SyncAndroidCalendarProvider;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import me.everything.providers.android.calendar.Event;
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
    RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    @Override
    protected Observable<Void> doOnReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);

        Log.d("CalendarReceiver", "EventChangedReceiver");

        SyncAndroidCalendarProvider provider = new SyncAndroidCalendarProvider(context);
        LocalStorage localStorage = LocalStorage.of(context);
        Set<String> calendarIds = localStorage.readStringSet(Constants.KEY_SELECTED_ANDROID_CALENDARS);

        return Observable.defer(() -> {
            for (String cid : calendarIds) {
                int calendarId = Integer.valueOf(cid);
                List<Event> deletedEvents = provider.getDeletedEvents(calendarId).getList();
                deleteEvents(deletedEvents);
                List<Event> dirtyEvents = provider.getDirtyEvents(calendarId).getList();
                markEventsForUpdate(dirtyEvents, localStorage);
            }
            return Observable.<Void>empty();
        }).compose(applyAndroidSchedulers());
    }

    private void markEventsForUpdate(List<Event> dirtyEvents, LocalStorage localStorage) {
        Set<String> habitKeys = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_HABITS_TO_UPDATE);
        Set<String> questKeys = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE);
        for (Event e : dirtyEvents) {
            if (isRecurrentAndroidCalendarEvent(e)) {
                habitKeys.add(String.valueOf(e.id));
            } else {
                questKeys.add(String.valueOf(e.id));
            }
        }
        localStorage.saveStringSet(Constants.KEY_ANDROID_CALENDAR_HABITS_TO_UPDATE, habitKeys);
        localStorage.saveStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE, questKeys);
    }

    private void deleteEvents(List<Event> events) {
        for (Event e : events) {
            if (isRecurrentAndroidCalendarEvent(e)) {
                recurrentQuestPersistenceService.deleteByExternalSourceMappingId("googleCalendar", String.valueOf(e.id));
            } else {
                questPersistenceService.deleteByExternalSourceMappingId("googleCalendar", String.valueOf(e.id));
            }
        }
    }

    private boolean isRecurrentAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }

    private <T> Observable.Transformer<T, T> applyAndroidSchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
