package io.ipoli.android.app.services;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.net.AuthProvider;
import io.ipoli.android.app.net.JsonRequestBodyBuilder;
import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.net.iPoliAPIService;
import io.ipoli.android.app.providers.SyncAndroidCalendarProvider;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListReader;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListReader;
import io.ipoli.android.app.services.readers.ListReader;
import io.ipoli.android.app.services.readers.RealmRepeatingQuestListReader;
import io.ipoli.android.app.services.readers.RealmQuestListReader;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.SourceMapping;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public class AppJobService extends JobService {

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    iPoliAPIService apiService;

    @Inject
    Gson gson;

    @Inject
    Bus eventBus;

    @Override
    public boolean onStartJob(JobParameters params) {
        App.getAppComponent(this).inject(this);
        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        getPlayer().flatMap(p -> Observable.concat(
                Observable.defer(() -> syncCalendars(localStorage)),
                Observable.defer(() -> syncRemovedRepeatingQuests(p)),
                Observable.defer(() -> syncRemovedQuests(p)),
                Observable.defer(() -> syncRepeatingQuests(p, localStorage)),
                Observable.defer(() -> syncQuests(p, localStorage)),
                Observable.defer(() -> getRepeatingQuests(p)),
                Observable.defer(() -> getJourneysForAWeekAhead(p))
        )).subscribe(res -> Log.d("RxJava", "OnNext " + res), throwable -> {
            Log.e("RxJava", "Error", throwable);
            jobFinished(params, true);
        }, () -> {
            eventBus.post(new SyncCompleteEvent());
            Log.d("RxJava", "Sync Job finished");
            jobFinished(params, false);
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("RxJava", "Stopping job" + params);
        return false;
    }

    private Observable<Player> getPlayer() {
        return playerPersistenceService.find().concatMap(player -> {
            if (player == null) {
                return createNewPlayer();
            }
            return Observable.just(player);
        });
    }

    private Observable<Void> syncCalendars(LocalStorage localStorage) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return Observable.empty();
        }

        Set<String> calendarsToSync = localStorage.readStringSet(Constants.KEY_CALENDARS_TO_SYNC);
        return Observable.just(calendarsToSync).concatMapIterable(calendarIds -> calendarIds)
                .concatMap(calendarId -> {
                    Set<String> questKeys = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE);
                    Set<String> repeatingQuestKeys = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE);
                    SyncAndroidCalendarProvider provider = new SyncAndroidCalendarProvider(getApplicationContext());
                    List<Event> events = provider.getEvents(Integer.valueOf(calendarId)).getList();
                    for (Event e : events) {
                        if (isRecurrentAndroidCalendarEvent(e)) {
                            repeatingQuestKeys.add(String.valueOf(e.id));
                        } else {
                            questKeys.add(String.valueOf(e.id));
                        }
                    }
                    localStorage.saveStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE, questKeys);
                    localStorage.saveStringSet(Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE, repeatingQuestKeys);
                    calendarsToSync.remove(calendarId);
                    localStorage.saveStringSet(Constants.KEY_CALENDARS_TO_SYNC, calendarsToSync);
                    return Observable.<Void>empty();
                }).compose(applyAPISchedulers());
    }

    private Observable<Void> syncRemovedRepeatingQuests(Player player) {
        return Observable.just(LocalStorage.of(getApplicationContext()).readStringSet(Constants.KEY_REMOVED_REPEATING_QUESTS))
                .concatMapIterable(ids -> ids)
                .concatMap(id -> apiService.deleteRepeatingQuest(id, player.getId()).compose(applyAPISchedulers())
                        .concatMap(res -> {
                            LocalStorage localStorage = LocalStorage.of(getApplicationContext());
                            Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_REPEATING_QUESTS);
                            removedQuests.remove(id);
                            localStorage.saveStringSet(Constants.KEY_REMOVED_REPEATING_QUESTS, removedQuests);
                            return Observable.<Void>empty();
                        }));
    }

    private Observable<Void> syncRemovedQuests(Player player) {
        return Observable.just(LocalStorage.of(getApplicationContext()).readStringSet(Constants.KEY_REMOVED_QUESTS))
                .concatMapIterable(ids -> ids)
                .concatMap(id -> apiService.deleteQuest(id, player.getId()).compose(applyAPISchedulers())
                        .concatMap(res -> {
                            LocalStorage localStorage = LocalStorage.of(getApplicationContext());
                            Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_QUESTS);
                            removedQuests.remove(id);
                            localStorage.saveStringSet(Constants.KEY_REMOVED_QUESTS, removedQuests);
                            return Observable.<Void>empty();
                        }));
    }

    private Observable<String> getAdvertisingId() {
        return Observable.defer(() -> {
            try {
                AdvertisingIdClient.Info idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                return Observable.just(idInfo.getId());
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    return Observable.just(UUID.randomUUID().toString());
                }
                return Observable.error(e);
            }
        }).compose(applyAPISchedulers());
    }

    private Observable<Player> createNewPlayer() {
        return getAdvertisingId().flatMap(advertisingId -> {
            RequestBody requestBody = createJsonRequestBodyBuilder().param("uid", advertisingId).param("provider", AuthProvider.ANONYMOUS).build();
            return apiService.createPlayer(requestBody).compose(applyAPISchedulers()).concatMap(sp -> {
                sp.setSyncedWithRemote();
                sp.setRemoteObject();
                sp.setAvatar(Constants.DEFAULT_PLAYER_AVATAR);
                LocalStorage localStorage = LocalStorage.of(getApplicationContext());
                localStorage.saveString(Constants.KEY_PLAYER_ID, sp.getId());
                eventBus.post(new PlayerCreatedEvent(sp.getId()));
                return playerPersistenceService.saveRemoteObject(sp);
            });
        });
    }

    private Observable<Void> syncQuests(Player player, LocalStorage localStorage) {
        ListReader<Quest> realmReader = new RealmQuestListReader(questPersistenceService);
        ListReader<Quest> androidCalendarReader = new AndroidCalendarQuestListReader(getApplicationContext(), new CalendarProvider(getApplicationContext()), localStorage, repeatingQuestPersistenceService);

        return Observable.concat(realmReader.read(), androidCalendarReader.read()).concatMap(q -> {
            if (isLocalOnly(q)) {
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", q).param("player_id", player.getId()).build();
                return apiService.createQuest(requestBody).compose(applyAPISchedulers())
                        .concatMap(sq -> updateQuest(sq, q))
                        .concatMap(updatedQuest -> questPersistenceService.saveRemoteObject(updatedQuest))
                        .concatMap(savedQuest -> deleteSyncedAndroidCalendarEvent(savedQuest.getSourceMapping(), localStorage, Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE));
            } else {
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", q).param("player_id", player.getId()).build();
                return apiService.updateQuest(requestBody, q.getId()).compose(applyAPISchedulers())
                        .concatMap(sq -> updateQuest(sq, q))
                        .concatMap(updatedQuest -> questPersistenceService.saveRemoteObject(updatedQuest))
                        .concatMap(savedQuest -> deleteSyncedAndroidCalendarEvent(savedQuest.getSourceMapping(), localStorage, Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE));
            }
        });
    }

    @NonNull
    private JsonRequestBodyBuilder createJsonRequestBodyBuilder() {
        return new JsonRequestBodyBuilder(getApplicationContext());
    }

    private Observable<Void> deleteSyncedAndroidCalendarEvent(SourceMapping sourceMapping, LocalStorage localStorage, String setKey) {
        if (sourceMapping != null && !TextUtils.isEmpty(sourceMapping.getAndroidCalendar())) {
            String eventId = sourceMapping.getAndroidCalendar();
            Set<String> questsToUpdate = localStorage.readStringSet(setKey);
            questsToUpdate.remove(eventId);
            localStorage.saveStringSet(setKey, questsToUpdate);
        }

        return Observable.empty();
    }

    private Observable<Void> syncRepeatingQuests(Player player, LocalStorage localStorage) {
        ListReader<RepeatingQuest> realmReader = new RealmRepeatingQuestListReader(repeatingQuestPersistenceService);
        ListReader<RepeatingQuest> androidCalendarReader = new AndroidCalendarRepeatingQuestListReader(getApplicationContext(), new CalendarProvider(getApplicationContext()), localStorage);

        return Observable.concat(realmReader.read(), androidCalendarReader.read()).concatMap(repeatingQuest -> {
            if (isLocalOnly(repeatingQuest) && repeatingQuest.getSourceMapping() == null) {
                JsonObject data = new JsonObject();
                JsonObject qJson = (JsonObject) gson.toJsonTree(repeatingQuest);
                data.addProperty("text", qJson.get("raw_text").getAsString());
                data.addProperty("context", qJson.get("context").getAsString());
                data.addProperty("created_at", qJson.get("created_at").getAsString());
                data.addProperty("updated_at", qJson.get("updated_at").getAsString());
                data.addProperty("source", qJson.get("source").getAsString());
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", data).param("player_id", player.getId()).build();
                return apiService.createRepeatingQuestFromText(requestBody).compose(applyAPISchedulers())
                        .concatMap(sq -> updateRepeatingQuest(sq, repeatingQuest))
                        .concatMap(updatedRepeatingQuest -> repeatingQuestPersistenceService.saveRemoteObject(updatedRepeatingQuest))
                        .concatMap(savedRepeatingQuest -> deleteSyncedAndroidCalendarEvent(savedRepeatingQuest.getSourceMapping(), localStorage, Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE));

            } else if (isLocalOnly(repeatingQuest)) {
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", repeatingQuest).param("player_id", player.getId()).build();
                return apiService.createRepeatingQuest(requestBody).compose(applyAPISchedulers())
                        .concatMap(sq -> updateRepeatingQuest(sq, repeatingQuest))
                        .concatMap(updatedRepeatingQuest -> repeatingQuestPersistenceService.saveRemoteObject(updatedRepeatingQuest)
                                .concatMap(savedRepeatingQuest -> deleteSyncedAndroidCalendarEvent(savedRepeatingQuest.getSourceMapping(), localStorage, Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE)));
            } else {
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", repeatingQuest).param("player_id", player.getId()).build();
                return apiService.updateRepeatingQuest(requestBody, repeatingQuest.getId()).compose(applyAPISchedulers())
                        .concatMap(sq -> updateRepeatingQuest(sq, repeatingQuest))
                        .concatMap(updatedRepeatingQuest -> repeatingQuestPersistenceService.saveRemoteObject(updatedRepeatingQuest))
                        .concatMap(savedRepeatingQuest -> deleteSyncedAndroidCalendarEvent(savedRepeatingQuest.getSourceMapping(), localStorage, Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE));
            }
        });
    }

    private Observable<Quest> updateQuest(Quest serverQuest, Quest localQuest) {
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteObject();
        if (localQuest != null && isLocalOnly(localQuest) && !TextUtils.isEmpty(localQuest.getId())) {
            return questPersistenceService.updateId(localQuest, serverQuest.getId()).flatMap(aVoid ->
                    Observable.just(serverQuest));
        }

        return Observable.just(serverQuest);
    }

    private Observable<RepeatingQuest> updateRepeatingQuest(RepeatingQuest serverQuest, RepeatingQuest localQuest) {
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteObject();
        if (localQuest != null && isLocalOnly(localQuest) && !TextUtils.isEmpty(localQuest.getId())) {
            return repeatingQuestPersistenceService.updateId(localQuest, serverQuest.getId()).flatMap(aVoid ->
                    Observable.just(serverQuest));
        }
        return Observable.just(serverQuest);
    }

    private Observable<RepeatingQuest> getRepeatingQuests(Player player) {
        return apiService.getRepeatingQuests(player.getId())
                .compose(applyAPISchedulers()).concatMapIterable(repeatingQuests -> repeatingQuests)
                .concatMap(sq -> repeatingQuestPersistenceService.findById(sq.getId()).concatMap(repeatingQuest -> {
                    if (repeatingQuest != null && sq.getUpdatedAt().getTime() <= repeatingQuest.getUpdatedAt().getTime()) {
                        return Observable.just(repeatingQuest);
                    }
                    return updateRepeatingQuest(sq, repeatingQuest).flatMap(h -> repeatingQuestPersistenceService.saveRemoteObject(h));
                }));
    }

    private Observable<Quest> getJourneysForAWeekAhead(Player player) {
        return Observable.just(DateUtils.getNext7Days()).concatMapIterable(dates -> dates)
                .concatMap(date -> apiService.getJourney(date, player.getId()).compose(applyAPISchedulers())).concatMapIterable(quests -> quests)
                .concatMap(sq -> questPersistenceService.findById(sq.getId()).concatMap(q -> {
                    if (q != null && sq.getUpdatedAt().getTime() <= q.getUpdatedAt().getTime()) {
                        return Observable.just(q);
                    }

                    sq.setSyncedWithRemote();
                    sq.setRemoteObject();

                    if (q != null && isLocalOnly(q)) {
                        return questPersistenceService.updateId(q, sq.getId()).flatMap(aVoid -> questPersistenceService.saveRemoteObject(sq));
                    }
                    return questPersistenceService.saveRemoteObject(sq);
                }));
    }

    private <T> Observable.Transformer<T, T> applyAPISchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private boolean isLocalOnly(RemoteObject remoteObject) {
        return !remoteObject.isRemoteObject();
    }

    private boolean isRecurrentAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }
}