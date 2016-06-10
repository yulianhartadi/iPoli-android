package io.ipoli.android.app.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
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
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.player.persistence.RealmPlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmRepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.realm.Realm;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public class AppJobService extends JobService {

    PlayerPersistenceService playerPersistenceService;

    QuestPersistenceService questPersistenceService;

    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    iPoliAPIService apiService;

    @Inject
    RepeatingQuestScheduler repeatingQuestScheduler;

    @Inject
    Gson gson;

    @Inject
    Bus eventBus;

    @Override
    public boolean onStartJob(JobParameters params) {
        App.getAppComponent(this).inject(this);

        Realm realm = Realm.getDefaultInstance();
        playerPersistenceService = new RealmPlayerPersistenceService(realm);
        questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);
        repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, realm);
        Log.d("RxJava", "Sync start");
        syncPlayer().flatMap(p -> {
                    if (p == null) {
                        return Observable.empty();
                    }
                    return Observable.concat(
                            Observable.defer(this::scheduleQuestsFor2WeeksAhead),
                            Observable.defer(() -> syncRepeatingQuests(p)),
                            Observable.defer(() -> syncQuests(p)),
                            Observable.defer(() -> getRepeatingQuests(p)),
                            Observable.defer(() -> getQuests(p))
                    );
                }
        ).subscribe(res -> Log.d("RxJava", "OnNext " + res), throwable -> {
            if (!realm.isClosed()) {
                realm.close();
            }
            Log.e("RxJava", "Error", throwable);
            jobFinished(params, true);
        }, () -> {
            if (!realm.isClosed()) {
                realm.close();
            }
            eventBus.post(new SyncCompleteEvent());
            Log.d("RxJava", "Sync Job finished");
            jobFinished(params, false);
        });
        return true;
    }

    private Observable<List<List<Quest>>> scheduleQuestsFor2WeeksAhead() {
        LocalDate currentDate = LocalDate.now();
        LocalDate startOfWeek = currentDate.dayOfWeek().withMinimumValue();
        LocalDate endOfWeek = currentDate.dayOfWeek().withMaximumValue();
        LocalDate startOfNextWeek = startOfWeek.plusDays(7);
        LocalDate endOfNextWeek = endOfWeek.plusDays(7);
        List<RepeatingQuest> repeatingQuests = repeatingQuestPersistenceService.findAllNonAllDayActiveRepeatingQuests();
        return Observable.from(repeatingQuests)
                .flatMap(rq -> Observable.concat(
                        saveQuestsInRange(rq, startOfWeek, endOfWeek),
                        saveQuestsInRange(rq, startOfNextWeek, endOfNextWeek)
                )).toList();
    }

    private Observable<List<Quest>> saveQuestsInRange(RepeatingQuest rq, LocalDate startOfWeek, LocalDate endOfWeek) {
        long createdQuestsCount = questPersistenceService.countAllForRepeatingQuest(rq, startOfWeek, endOfWeek);
        if (createdQuestsCount == 0) {
            List<Quest> questsToCreate = repeatingQuestScheduler.schedule(rq, DateUtils.toStartOfDayUTC(startOfWeek));
            return questPersistenceService.saveRemoteObjects(questsToCreate);
        }
        return Observable.just(new ArrayList<>());
    }

    private Observable<Player> syncPlayer() {
        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        Player player = getPlayer();
        if (player == null) {
            return Observable.just(null);
        }
        if (isLocalOnly(player)) {
            return createPlayer(localStorage, player);
        }
        if (player.needsSyncWithRemote()) {
            return updatePlayer(player);
        }
        return Observable.just(player);
    }

    private Observable<Player> updatePlayer(Player player) {
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

    private Observable<Player> createPlayer(LocalStorage localStorage, Player player) {
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

    private Player getPlayer() {
        return playerPersistenceService.find();
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

    private Observable<List<Quest>> syncQuests(Player player) {
        List<Quest> quests = questPersistenceService.findAllWhoNeedSyncWithRemote();
        if (quests.isEmpty()) {
            return Observable.just(new ArrayList<>());
        }
        List<String> localIds = new ArrayList<>();
        for (Quest q : quests) {
            localIds.add(getLocalIdForRemoteObject(q));
            q.setId(isLocalOnly(q) ? null : q.getRemoteId());
            if (q.getRepeatingQuest() != null) {
                q.getRepeatingQuest().setId(q.getRepeatingQuest().getRemoteId());
            }
        }
        RequestBody requestBody = createRequestBody().param("data", quests).param("player_id", player.getRemoteId()).build();
        return apiService.syncQuests(requestBody).compose(applyAPISchedulers()).flatMap(serverQuests -> {
            for (int i = 0; i < serverQuests.size(); i++) {
                Quest sq = serverQuests.get(i);
                updateQuest(sq, localIds.get(i));
            }
            return questPersistenceService.saveRemoteObjects(serverQuests);
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

    private Observable<List<RepeatingQuest>> syncRepeatingQuests(Player player) {
        List<RepeatingQuest> quests = repeatingQuestPersistenceService.findAllWhoNeedSyncWithRemote();
        if (quests.isEmpty()) {
            return Observable.just(new ArrayList<>());
        }
        List<String> localIds = new ArrayList<>();
        for (RepeatingQuest q : quests) {
            localIds.add(getLocalIdForRemoteObject(q));
            q.setId(isLocalOnly(q) ? null : q.getRemoteId());
        }
        RequestBody requestBody = createRequestBody().param("data", quests).param("player_id", player.getRemoteId()).build();
        return apiService.syncRepeatingQuests(requestBody).compose(applyAPISchedulers()).flatMap(serverQuests -> {
            for (int i = 0; i < serverQuests.size(); i++) {
                RepeatingQuest sq = serverQuests.get(i);
                updateRepeatingQuest(sq, localIds.get(i));
            }
            return repeatingQuestPersistenceService.saveRemoteObjects(serverQuests);
        });
    }

    private RepeatingQuest updateRepeatingQuest(RepeatingQuest serverQuest, String localId) {
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteId(serverQuest.getId());
        serverQuest.setId(localId);
        return serverQuest;
    }

    private Observable<List<RepeatingQuest>> getRepeatingQuests(Player player) {
        return apiService.getRepeatingQuests(player.getRemoteId())
                .compose(applyAPISchedulers()).flatMapIterable(repeatingQuests -> repeatingQuests)
                .flatMap(sq -> {
                            RepeatingQuest repeatingQuest = repeatingQuestPersistenceService.findByRemoteIdSync(sq.getId());
                            if (repeatingQuest != null && sq.getUpdatedAt().getTime() <= repeatingQuest.getUpdatedAt().getTime()) {
                                return Observable.just(repeatingQuest);
                            }
                            String localId = getLocalIdForRemoteObject(repeatingQuest);
                            return repeatingQuestPersistenceService.saveRemoteObject(updateRepeatingQuest(sq, localId));
                        }
                ).toList();
    }

    private Observable<List<Quest>> getQuests(Player player) {
        return apiService.getQuests(player.getRemoteId())
                .compose(applyAPISchedulers())
                .flatMapIterable(quests -> quests)
                .flatMap(sq -> {
                            Quest quest = questPersistenceService.findByRemoteIdSync(sq.getId());
                            if (quest != null && sq.getUpdatedAt().getTime() <= quest.getUpdatedAt().getTime()) {
                                return Observable.just(quest);
                            }
                            String localId = getLocalIdForRemoteObject(quest);
                            return questPersistenceService.saveRemoteObject(updateQuest(sq, localId));
                        }
                ).toList();
    }

    private Quest updateQuest(Quest serverQuest, String localId) {
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
            RepeatingQuest repeatingQuest = repeatingQuestPersistenceService.findByRemoteIdSync(serverQuest.getRepeatingQuest().getId());
            if (repeatingQuest != null) {
                serverQuest.setRepeatingQuest(repeatingQuest);
            }
        }
        return serverQuest;
    }

    private String getLocalIdForRemoteObject(RemoteObject<?> remoteObject) {
        if (remoteObject != null) {
            return remoteObject.getId();
        } else {
            return IDGenerator.generate();
        }
    }

    private <T> Observable.Transformer<T, T> applyAPISchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private boolean isLocalOnly(RemoteObject remoteObject) {
        return TextUtils.isEmpty(remoteObject.getRemoteId());
    }
}