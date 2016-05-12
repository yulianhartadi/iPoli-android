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
import io.ipoli.android.app.services.readers.AndroidCalendarHabitListReader;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListReader;
import io.ipoli.android.app.services.readers.ListReader;
import io.ipoli.android.app.services.readers.RealmHabitListReader;
import io.ipoli.android.app.services.readers.RealmQuestListReader;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Habit;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.HabitPersistenceService;
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
    HabitPersistenceService habitPersistenceService;

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
                syncCalendars(localStorage),
                syncRemovedHabits(p),
                syncRemovedQuests(p),
                syncHabits(p, localStorage),
                syncQuests(p, localStorage),
                getHabits(p),
                getScheduleForAWeekAhead(p)
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

        return Observable.just(localStorage.readStringSet(Constants.KEY_CALENDARS_TO_SYNC)).concatMapIterable(calendarIds -> calendarIds)
                .concatMap(calendarId -> {
                    Set<String> questKeys = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE);
                    Set<String> habitKeys = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_HABITS_TO_UPDATE);
                    SyncAndroidCalendarProvider provider = new SyncAndroidCalendarProvider(getApplicationContext());
                    List<Event> events = provider.getEvents(Integer.valueOf(calendarId)).getList();
                    for (Event e : events) {
                        if (isRecurrentAndroidCalendarEvent(e)) {
                            habitKeys.add(String.valueOf(e.id));
                        } else {
                            questKeys.add(String.valueOf(e.id));
                        }
                    }
                    localStorage.saveStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE, questKeys);
                    localStorage.saveStringSet(Constants.KEY_ANDROID_CALENDAR_HABITS_TO_UPDATE, habitKeys);

                    return Observable.<Void>empty();
                }).compose(applyAPISchedulers());
    }

    private Observable<Void> syncRemovedHabits(Player player) {
        return Observable.just(LocalStorage.of(getApplicationContext()).readStringSet(Constants.KEY_REMOVED_HABITS))
                .concatMapIterable(ids -> ids)
                .concatMap(id -> apiService.deleteHabit(id, player.getId()).compose(applyAPISchedulers())
                        .concatMap(res -> {
                            LocalStorage localStorage = LocalStorage.of(getApplicationContext());
                            Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_HABITS);
                            removedQuests.remove(id);
                            localStorage.saveStringSet(Constants.KEY_REMOVED_HABITS, removedQuests);
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

    private Observable<Quest> syncQuests(Player player, LocalStorage localStorage) {
        ListReader<Quest> realmReader = new RealmQuestListReader(questPersistenceService);
        ListReader<Quest> androidCalendarReader = new AndroidCalendarQuestListReader(getApplicationContext(), new CalendarProvider(getApplicationContext()), localStorage, habitPersistenceService);

        return Observable.concat(realmReader.read(), androidCalendarReader.read()).concatMap(q -> {
            if (isLocalOnly(q)) {
                q.setId(null);
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", q).param("player_id", player.getId()).build();
                return apiService.createQuest(requestBody).compose(applyAPISchedulers())
                        .concatMap(sq -> questPersistenceService.saveRemoteObject(updateQuest(sq, q)));
            } else {
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", q).param("player_id", player.getId()).build();
                return apiService.updateQuest(requestBody, q.getId()).compose(applyAPISchedulers())
                        .concatMap(sq -> questPersistenceService.saveRemoteObject(updateQuest(sq, q)));
            }
        });
    }

    @NonNull
    private JsonRequestBodyBuilder createJsonRequestBodyBuilder() {
        return new JsonRequestBodyBuilder(getApplicationContext());
    }

    private Observable<Habit> syncHabits(Player player, LocalStorage localStorage) {
        ListReader<Habit> realmReader = new RealmHabitListReader(habitPersistenceService);
        ListReader<Habit> androidCalendarReader = new AndroidCalendarHabitListReader(getApplicationContext(), new CalendarProvider(getApplicationContext()), localStorage);

        return Observable.concat(realmReader.read(), androidCalendarReader.read()).concatMap(habit -> {
            if (isLocalOnly(habit)) {
                JsonObject data = new JsonObject();
                JsonObject qJson = (JsonObject) gson.toJsonTree(habit);
                // @TODO look into server side for raw_text
                data.addProperty("text", qJson.get("raw_text").getAsString());
                data.addProperty("context", qJson.get("context").getAsString());
                data.addProperty("created_at", qJson.get("created_at").getAsString());
                data.addProperty("updated_at", qJson.get("updated_at").getAsString());
                data.addProperty("source", qJson.get("source").getAsString());
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", data).param("player_id", player.getId()).build();
                return apiService.createHabitFromText(requestBody).compose(applyAPISchedulers())
                        .concatMap(sq -> habitPersistenceService.saveRemoteObject(updateHabit(sq, habit)));
            } else {
                RequestBody requestBody = createJsonRequestBodyBuilder().param("data", habit).param("player_id", player.getId()).build();
                return apiService.updateHabit(requestBody, habit.getId()).compose(applyAPISchedulers())
                        .concatMap(sq -> habitPersistenceService.saveRemoteObject(updateHabit(sq, habit)));
            }
        });
    }

    private Quest updateQuest(Quest serverQuest, Quest localQuest) {
        if (localQuest != null && isLocalOnly(localQuest)) {
            questPersistenceService.updateId(localQuest, serverQuest.getId());
        }
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteObject();
        return serverQuest;
    }

    private Habit updateHabit(Habit serverQuest, Habit localQuest) {
        if (localQuest != null && isLocalOnly(localQuest)) {
            habitPersistenceService.updateId(localQuest, serverQuest.getId());
        }
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteObject();
        return serverQuest;
    }

    private Observable<Habit> getHabits(Player player) {
        return apiService.getHabits(player.getId())
                .compose(applyAPISchedulers()).concatMapIterable(habits -> habits)
                .concatMap(sq -> habitPersistenceService.findById(sq.getId()).concatMap(habit -> {
                    if (habit != null && sq.getUpdatedAt().getTime() <= habit.getUpdatedAt().getTime()) {
                        return Observable.just(habit);
                    }

                    updateHabit(sq, habit);
                    return habitPersistenceService.saveRemoteObject(sq);
                }));
    }

    private Observable<Quest> getScheduleForAWeekAhead(Player player) {
        return Observable.just(DateUtils.getNext7Days()).concatMapIterable(dates -> dates)
                .concatMap(date -> apiService.getSchedule(date, player.getId()).compose(applyAPISchedulers())).concatMapIterable(quests -> quests)
                .concatMap(sq -> questPersistenceService.findById(sq.getId()).concatMap(q -> {
                    if (q != null && sq.getUpdatedAt().getTime() <= q.getUpdatedAt().getTime()) {
                        return Observable.just(q);
                    }

                    if (q != null && isLocalOnly(q)) {
                        questPersistenceService.updateId(q, sq.getId());
                    }

                    sq.setSyncedWithRemote();
                    sq.setRemoteObject();

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