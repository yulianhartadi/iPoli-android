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
import io.ipoli.android.app.net.AuthProviderName;
import io.ipoli.android.app.net.JsonRequestBodyBuilder;
import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.net.iPoliAPIService;
import io.ipoli.android.app.providers.SyncAndroidCalendarProvider;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListReader;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListReader;
import io.ipoli.android.app.services.readers.ListReader;
import io.ipoli.android.app.services.readers.RealmQuestListReader;
import io.ipoli.android.app.services.readers.RealmRepeatingQuestListReader;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SourceMapping;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
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
        syncPlayer(localStorage).flatMap(p -> {
                    if (p == null) {
                        return Observable.empty();
                    }
                    return Observable.concat(
                            Observable.defer(() -> syncCalendars(localStorage)),
                            Observable.defer(() -> syncRemovedRepeatingQuests(p)),
                            Observable.defer(() -> syncRemovedQuests(p)),
                            Observable.defer(() -> syncRepeatingQuests(p, localStorage)),
                            Observable.defer(() -> syncQuests(p, localStorage)),
                            Observable.defer(() -> getRepeatingQuests(p)),
                            Observable.defer(() -> getJourneysForAWeekAhead(p)));
                }
        ).subscribe(res -> Log.d("RxJava", "OnNext " + res), throwable -> {
            Log.e("RxJava", "Error", throwable);
            jobFinished(params, true);
        }, () -> {
            eventBus.post(new SyncCompleteEvent());
            Log.d("RxJava", "Sync Job finished");
            jobFinished(params, false);
        });
        return true;
    }

    private Observable<Player> syncPlayer(LocalStorage localStorage) {
        return getPlayer().flatMap(player -> {
            if (player == null) {
                return Observable.just(null);
            }
            if (isLocalOnly(player)) {
                return createPlayer(localStorage, player);
            } else if (player.needsSyncWithRemote()) {
                return updatePlayer(player);
            }
            return Observable.just(player);
        });
    }

    private Observable<? extends Player> updatePlayer(Player player) {
        String localId = player.getId();
        player.setId(null);
        return apiService.updatePlayer(createRequestBody("data", player), player.getRemoteId())
                .compose(applyAPISchedulers()).flatMap(sp -> {
                    sp.setSyncedWithRemote();
                    sp.setRemoteId(sp.getId());
                    sp.setId(localId);
                    return playerPersistenceService.saveRemoteObject(sp);
                });
    }

    private Observable<? extends Player> createPlayer(LocalStorage localStorage, Player player) {
        return getAdvertisingId().flatMap(advertisingId -> {
            String localId = player.getId();
            AuthProvider authProvider = new AuthProvider(advertisingId, AuthProviderName.GOOGLE_ADVERTISING_ID.name());
            return playerPersistenceService.addAuthProvider(player, authProvider)
                    .flatMap(p -> {
                        p.setId(null);
                        return apiService.createPlayer(createRequestBody("data", p)).compose(applyAPISchedulers());
                    })
                    .flatMap(sp -> {
                        sp.setSyncedWithRemote();
                        sp.setRemoteId(sp.getId());
                        sp.setId(localId);
                        localStorage.saveString(Constants.KEY_PLAYER_REMOTE_ID, sp.getRemoteId());
                        eventBus.post(new PlayerCreatedEvent(sp.getRemoteId()));
                        return playerPersistenceService.saveRemoteObject(sp);
                    });
        });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("RxJava", "Stopping job" + params);
        return false;
    }

    private Observable<Player> getPlayer() {
        return playerPersistenceService.find();
    }

    private Observable<Void> syncCalendars(LocalStorage localStorage) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return Observable.empty();
        }

        Set<String> calendarsToSync = localStorage.readStringSet(Constants.KEY_CALENDARS_TO_SYNC);
        return Observable.just(calendarsToSync).flatMapIterable(calendarIds -> calendarIds)
                .flatMap(calendarId -> {
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
                .flatMapIterable(ids -> ids)
                .flatMap(id -> apiService.deleteRepeatingQuest(id, player.getRemoteId()).compose(applyAPISchedulers())
                        .flatMap(res -> {
                            LocalStorage localStorage = LocalStorage.of(getApplicationContext());
                            Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_REPEATING_QUESTS);
                            removedQuests.remove(id);
                            localStorage.saveStringSet(Constants.KEY_REMOVED_REPEATING_QUESTS, removedQuests);
                            return Observable.<Void>empty();
                        }));
    }

    private Observable<Void> syncRemovedQuests(Player player) {
        return Observable.just(LocalStorage.of(getApplicationContext()).readStringSet(Constants.KEY_REMOVED_QUESTS))
                .flatMapIterable(ids -> ids)
                .flatMap(id -> apiService.deleteQuest(id, player.getRemoteId()).compose(applyAPISchedulers())
                        .flatMap(res -> {
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

    private Observable<Void> syncQuests(Player player, LocalStorage localStorage) {
        ListReader<Quest> realmReader = new RealmQuestListReader(questPersistenceService);
        ListReader<Quest> androidCalendarReader = new AndroidCalendarQuestListReader(getApplicationContext(), new CalendarProvider(getApplicationContext()), localStorage, repeatingQuestPersistenceService);

        return Observable.concat(realmReader.read(), androidCalendarReader.read()).flatMap(q -> {
            String localId = getLocalIdForRemoteObject(q);
            q.setId(null);
            RequestBody requestBody = createRequestBody().param("data", q).param("player_id", player.getRemoteId()).build();
            Observable<Quest> apiCall = isLocalOnly(q) ? apiService.createQuest(requestBody) : apiService.updateQuest(requestBody, q.getRemoteId());
            return apiCall.compose(applyAPISchedulers())
                    .flatMap(sq -> updateQuest(sq, localId))
                    .flatMap(updatedQuest -> questPersistenceService.saveRemoteObject(updatedQuest))
                    .flatMap(savedQuest -> deleteSyncedAndroidCalendarEvent(savedQuest.getSourceMapping(), localStorage, Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE));
        });
    }

    @NonNull
    private JsonRequestBodyBuilder createRequestBody() {
        return new JsonRequestBodyBuilder(getApplicationContext());
    }

    @NonNull
    private RequestBody createRequestBody(String param, Object value) {
        return new JsonRequestBodyBuilder(getApplicationContext()).param(param, value).build();
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

        return Observable.concat(realmReader.read(), androidCalendarReader.read()).flatMap(repeatingQuest -> {
            String localId = getLocalIdForRemoteObject(repeatingQuest);
            if (isLocalOnly(repeatingQuest) && repeatingQuest.getSourceMapping() == null) {
                JsonObject data = new JsonObject();
                JsonObject qJson = (JsonObject) gson.toJsonTree(repeatingQuest);
                data.addProperty("text", qJson.get("raw_text").getAsString());
                data.addProperty("context", qJson.get("context").getAsString());
                data.addProperty("created_at", qJson.get("created_at").getAsString());
                data.addProperty("updated_at", qJson.get("updated_at").getAsString());
                data.addProperty("source", qJson.get("source").getAsString());
                RequestBody requestBody = createRequestBody().param("data", data).param("player_id", player.getRemoteId()).build();
                return apiService.createRepeatingQuestFromText(requestBody).compose(applyAPISchedulers())
                        .flatMap(sq -> repeatingQuestPersistenceService.saveRemoteObject(updateRepeatingQuest(sq, localId))
                                .flatMap(q -> Observable.just(null)));
            } else {
                repeatingQuest.setId(null);
                RequestBody requestBody = createRequestBody().param("data", repeatingQuest).param("player_id", player.getRemoteId()).build();
                Observable<RepeatingQuest> apiCall = isLocalOnly(repeatingQuest) ? apiService.createRepeatingQuest(requestBody) : apiService.updateRepeatingQuest(requestBody, repeatingQuest.getRemoteId());
                return apiCall.compose(applyAPISchedulers())
                        .flatMap(sq -> repeatingQuestPersistenceService.saveRemoteObject(updateRepeatingQuest(sq, localId))
                                .flatMap(savedRepeatingQuest -> deleteSyncedAndroidCalendarEvent(savedRepeatingQuest.getSourceMapping(), localStorage, Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE)));
            }
        });
    }


    private RepeatingQuest updateRepeatingQuest(RepeatingQuest serverQuest, String localId) {
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteId(serverQuest.getId());
        serverQuest.setId(localId);
        return serverQuest;
    }

    private Observable<RepeatingQuest> getRepeatingQuests(Player player) {
        return apiService.getRepeatingQuests(player.getRemoteId())
                .compose(applyAPISchedulers()).flatMapIterable(repeatingQuests -> repeatingQuests)
                .flatMap(sq -> repeatingQuestPersistenceService.findByRemoteId(sq.getId()).flatMap(repeatingQuest -> {
                    if (repeatingQuest != null && sq.getUpdatedAt().getTime() <= repeatingQuest.getUpdatedAt().getTime()) {
                        return Observable.just(repeatingQuest);
                    }
                    String localId = getLocalIdForRemoteObject(repeatingQuest);
                    return repeatingQuestPersistenceService.saveRemoteObject(updateRepeatingQuest(sq, localId));
                }));
    }

    private Observable<Quest> updateQuest(Quest serverQuest, String localId) {
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteId(serverQuest.getId());
        serverQuest.setId(localId);
        if (serverQuest.getExperience() == null) {
            serverQuest.setExperience(new ExperienceRewardGenerator().generate(serverQuest));
        }
        if (serverQuest.getCoins() == null) {
            serverQuest.setCoins(new CoinsRewardGenerator().generate(serverQuest));
        }
        if (serverQuest.getRepeatingQuest() != null) {
            return repeatingQuestPersistenceService.findByRemoteId(serverQuest.getRepeatingQuest().getId()).flatMap(repeatingQuest -> {
                if (repeatingQuest != null) {
                    serverQuest.setRepeatingQuest(repeatingQuest);
                }
                return Observable.just(serverQuest);
            });
        }
        return Observable.just(serverQuest);
    }

    private Observable<Quest> getJourneysForAWeekAhead(Player player) {
        return Observable.just(DateUtils.getNext7Days()).concatMapIterable(dates -> dates)
                .concatMap(date -> apiService.getJourney(date, player.getRemoteId()).compose(applyAPISchedulers()))
                .concatMapIterable(quests -> quests)
                .concatMap(sq -> questPersistenceService.findByRemoteId(sq.getId())
                        .concatMap(q -> {
                            if (q != null && sq.getUpdatedAt().getTime() <= q.getUpdatedAt().getTime()) {
                                return Observable.just(q);
                            }
                            String localId = getLocalIdForRemoteObject(q);
                            return updateQuest(sq, localId).flatMap(updatedQuest ->
                                    questPersistenceService.saveRemoteObject(updatedQuest));
                        }));
    }

    private String getLocalIdForRemoteObject(RemoteObject<?> remoteObject) {
        if (remoteObject != null) {
            return remoteObject.getId();
        } else {
            return UUID.randomUUID().toString();
        }
    }

    private <T> Observable.Transformer<T, T> applyAPISchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private boolean isLocalOnly(RemoteObject remoteObject) {
        return TextUtils.isEmpty(remoteObject.getRemoteId());
    }

    private boolean isRecurrentAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }
}