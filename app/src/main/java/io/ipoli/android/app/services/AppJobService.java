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

import java.io.IOException;
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

        Observable.defer(() -> {
            Log.d("RxJava", "Sync start");
            Realm realm = Realm.getDefaultInstance();
            try {
                PlayerPersistenceService playerPersistenceService = new RealmPlayerPersistenceService(realm);
                QuestPersistenceService questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);
                RepeatingQuestPersistenceService repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, realm);
                Player p = syncPlayer(playerPersistenceService);
                if (p == null) {
                    return Observable.empty();
                }
                scheduleQuestsFor2WeeksAhead(questPersistenceService, repeatingQuestPersistenceService);
                syncRepeatingQuests(repeatingQuestPersistenceService, p);
                syncQuests(questPersistenceService, repeatingQuestPersistenceService, p);
                getRepeatingQuests(repeatingQuestPersistenceService, p);
                getQuests(questPersistenceService, repeatingQuestPersistenceService, p);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (realm != null && !realm.isClosed()) {
                    realm.close();
                }
            }
            return Observable.empty();
        }).compose(applyAPISchedulers())
                .subscribe(res -> Log.d("RxJava", "OnNext " + res), throwable -> {
                    Log.e("RxJava", "Error", throwable);
                    jobFinished(params, true);
                }, () -> {
                    eventBus.post(new SyncCompleteEvent());
                    Log.d("RxJava", "Sync Job finished");
                    jobFinished(params, false);
                });
        return true;
    }

    private void scheduleQuestsFor2WeeksAhead(QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        LocalDate currentDate = LocalDate.now();
        LocalDate startOfWeek = currentDate.dayOfWeek().withMinimumValue();
        LocalDate endOfWeek = currentDate.dayOfWeek().withMaximumValue();
        LocalDate startOfNextWeek = startOfWeek.plusDays(7);
        LocalDate endOfNextWeek = endOfWeek.plusDays(7);
        List<RepeatingQuest> repeatingQuests = repeatingQuestPersistenceService.findAllNonAllDayActiveRepeatingQuests();
        for (RepeatingQuest rq : repeatingQuests) {
            saveQuestsInRange(questPersistenceService, rq, startOfWeek, endOfWeek);
            saveQuestsInRange(questPersistenceService, rq, startOfNextWeek, endOfNextWeek);
        }
    }

    private void saveQuestsInRange(QuestPersistenceService questPersistenceService, RepeatingQuest rq, LocalDate startOfWeek, LocalDate endOfWeek) {
        long createdQuestsCount = questPersistenceService.countAllForRepeatingQuest(rq, startOfWeek, endOfWeek);
        if (createdQuestsCount == 0) {
            List<Quest> questsToCreate = repeatingQuestScheduler.schedule(rq, DateUtils.toStartOfDayUTC(startOfWeek));
            questPersistenceService.saveSync(questsToCreate);
        }
    }

    private Player syncPlayer(PlayerPersistenceService playerPersistenceService) throws IOException {
        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        Player player = playerPersistenceService.find();
        if (player == null) {
            return null;
        }
        if (isLocalOnly(player)) {
            return createPlayer(playerPersistenceService, localStorage, player);
        }
        if (player.needsSyncWithRemote()) {
            return updatePlayer(player, playerPersistenceService);
        }
        return player;
    }

    private Player updatePlayer(Player player, PlayerPersistenceService playerPersistenceService) throws IOException {
        String localId = player.getId();
        player.setId(null);
        Player sp = apiService.updatePlayer(createRequestBody("data", player), player.getRemoteId()).execute().body();
        sp.setSyncedWithRemote();
        sp.setRemoteId(sp.getId());
        sp.setId(localId);
        playerPersistenceService.saveSync(sp);
        return sp;
    }

    private Player createPlayer(PlayerPersistenceService playerPersistenceService, LocalStorage localStorage, Player player) throws IOException {
        String advertisingId = getAdvertisingId();
        String localId = player.getId();
        AuthProvider authProvider = new AuthProvider(advertisingId, AuthProviderName.GOOGLE_ADVERTISING_ID.name());
        playerPersistenceService.addAuthProvider(player, authProvider);
        player.setId(null);
        Player sp = apiService.createPlayer(createRequestBody("data", player)).execute().body();
        sp.setSyncedWithRemote();
        sp.setRemoteId(sp.getId());
        sp.setId(localId);
        localStorage.saveString(Constants.KEY_PLAYER_REMOTE_ID, sp.getRemoteId());
        eventBus.post(new PlayerCreatedEvent(sp.getRemoteId()));
        playerPersistenceService.saveSync(sp);
        return sp;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("RxJava", "Stopping job" + params);
        return false;
    }

    private String getAdvertisingId() {
        try {
            AdvertisingIdClient.Info idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
            return idInfo.getId();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                return UUID.randomUUID().toString();
            }
            throw new RuntimeException(e);
        }
    }

    private void syncQuests(QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService, Player player) throws IOException {
        List<Quest> quests = questPersistenceService.findAllWhoNeedSyncWithRemote();
        if (quests.isEmpty()) {
            return;
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
        List<Quest> serverQuests = apiService.syncQuests(requestBody).execute().body();
        for (int i = 0; i < serverQuests.size(); i++) {
            Quest sq = serverQuests.get(i);
            updateQuest(repeatingQuestPersistenceService, sq, localIds.get(i));
        }
        questPersistenceService.saveSync(serverQuests);
    }

    @NonNull
    private JsonRequestBodyBuilder createRequestBody() {
        return new JsonRequestBodyBuilder(getApplicationContext());
    }

    @NonNull
    private RequestBody createRequestBody(String param, Object value) {
        return new JsonRequestBodyBuilder(getApplicationContext()).param(param, value).build();
    }

    private void syncRepeatingQuests(RepeatingQuestPersistenceService repeatingQuestPersistenceService, Player player) throws IOException {
        List<RepeatingQuest> quests = repeatingQuestPersistenceService.findAllWhoNeedSyncWithRemote();
        if (quests.isEmpty()) {
            return;
        }
        List<String> localIds = new ArrayList<>();
        for (RepeatingQuest q : quests) {
            localIds.add(getLocalIdForRemoteObject(q));
            q.setId(isLocalOnly(q) ? null : q.getRemoteId());
        }
        RequestBody requestBody = createRequestBody().param("data", quests).param("player_id", player.getRemoteId()).build();
        List<RepeatingQuest> serverQuests = apiService.syncRepeatingQuests(requestBody).execute().body();
        for (int i = 0; i < serverQuests.size(); i++) {
            RepeatingQuest sq = serverQuests.get(i);
            updateRepeatingQuest(sq, localIds.get(i));
        }
        repeatingQuestPersistenceService.saveSync(serverQuests);
    }

    private RepeatingQuest updateRepeatingQuest(RepeatingQuest serverQuest, String localId) {
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteId(serverQuest.getId());
        serverQuest.setId(localId);
        return serverQuest;
    }

    private void getRepeatingQuests(RepeatingQuestPersistenceService repeatingQuestPersistenceService, Player player) throws IOException {
        List<RepeatingQuest> serverQuests = apiService.getRepeatingQuests(player.getRemoteId()).execute().body();
        for (RepeatingQuest sq : serverQuests) {
            RepeatingQuest repeatingQuest = repeatingQuestPersistenceService.findByRemoteId(sq.getId());
            if (repeatingQuest != null && sq.getUpdatedAt().getTime() <= repeatingQuest.getUpdatedAt().getTime()) {
                return;
            }
            String localId = getLocalIdForRemoteObject(repeatingQuest);
            repeatingQuestPersistenceService.saveSync(updateRepeatingQuest(sq, localId));
        }
    }

    private void getQuests(QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService, Player player) throws IOException {
        List<Quest> serverQuests = apiService.getQuests(player.getRemoteId()).execute().body();
        for (Quest sq : serverQuests) {
            Quest quest = questPersistenceService.findByRemoteId(sq.getId());
            if (quest != null && sq.getUpdatedAt().getTime() <= quest.getUpdatedAt().getTime()) {
                return;
            }
            String localId = getLocalIdForRemoteObject(quest);
            questPersistenceService.saveSync(updateQuest(repeatingQuestPersistenceService, sq, localId));
        }
    }

    private Quest updateQuest(RepeatingQuestPersistenceService repeatingQuestPersistenceService, Quest serverQuest, String localId) {
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
            RepeatingQuest repeatingQuest = repeatingQuestPersistenceService.findByRemoteId(serverQuest.getRepeatingQuest().getId());
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