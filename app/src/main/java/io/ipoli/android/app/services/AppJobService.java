package io.ipoli.android.app.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.net.APIService;
import io.ipoli.android.app.net.AuthProvider;
import io.ipoli.android.app.net.JsonRequestBodyBuilder;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscription;
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
    RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    @Inject
    APIService apiService;

    @Inject
    Gson gson;

    private Subscription subscription;

    private Observable<Player> getPlayer() {
        return playerPersistenceService.find().concatMap(player -> {
            if (player == null) {
                return createNewPlayer();
            }
            if (player.needsSyncWithRemote()) {
                return syncUser(player);
            }
            return Observable.just(player);
        });
    }

    private Observable<Player> createNewPlayer() {
        Player defaultPlayer = new Player(Constants.DEFAULT_PLAYER_EXPERIENCE, Constants.DEFAULT_PLAYER_LEVEL, Constants.DEFAULT_PLAYER_AVATAR);
        return playerPersistenceService.save(defaultPlayer, false).concatMap(this::syncUser);
    }

    Observable<List<Quest>> syncQuests(Player player) {
        return questPersistenceService.findAllWhoNeedSyncWithRemote().flatMap(quests -> {
            if (quests.isEmpty()) {
                return Observable.just(quests);
            }
            JsonArray jsonQuests = new JsonArray();
            for (Quest q : quests) {
                JsonObject qJson = (JsonObject) gson.toJsonTree(q);
                if (TextUtils.isEmpty(q.getRemoteId())) {
                    String id = null;
                    qJson.addProperty("id", id);
                } else {
                    qJson.addProperty("id", q.getRemoteId());
                }
                if (q.getEndDate() != null) {
                    qJson.addProperty("end_date", DateUtils.toDateString(q.getEndDate()));
                }
                if (q.getStartDate() != null) {
                    qJson.addProperty("start_date", DateUtils.toDateString(q.getStartDate()));
                }
                jsonQuests.add(qJson);
            }
            RequestBody requestBody = new JsonRequestBodyBuilder().param("data", jsonQuests).param("user_id", player.getRemoteId()).build();
            return apiService.updateQuests(requestBody).compose(applySchedulers())
                    .flatMap(serverQuests -> {
                        List<Quest> questsToSave = new ArrayList<>();
                        for (int i = 0; i < quests.size(); i++) {
                            Quest q = quests.get(i);
                            Quest sq = serverQuests.get(i);
                            sq.setRemoteId(sq.getId());
                            sq.setId(q.getId());
                            sq.setSyncedWithRemote();
                            questsToSave.add(sq);
                        }
                        return questPersistenceService.saveAll(questsToSave, false);
                    });
        });
    }

    Observable<RecurrentQuest> syncRecurrentQuests(Player player) {
        return recurrentQuestPersistenceService.findAllWhoNeedSyncWithRemote().flatMapIterable(recurrentQuests -> recurrentQuests).flatMap(rq -> {
            if (TextUtils.isEmpty(rq.getRemoteId())) {
                JsonObject data = new JsonObject();
                JsonObject qJson = (JsonObject) gson.toJsonTree(rq);
                data.addProperty("text", qJson.get("raw_text").getAsString());
                data.addProperty("context", qJson.get("context").getAsString());
                data.addProperty("created_at", qJson.get("created_at").getAsString());
                data.addProperty("updated_at", qJson.get("updated_at").getAsString());
                RequestBody requestBody = new JsonRequestBodyBuilder().param("data", data).param("user_id", player.getRemoteId()).build();
                return apiService.createRecurrentQuest(requestBody).compose(applySchedulers()).flatMap(sq -> {
                    updateRecurrentQuest(sq, rq.getId(), rq.getRecurrence());
                    return recurrentQuestPersistenceService.save(sq, false);
                });
            } else {
                JsonObject qJson = (JsonObject) gson.toJsonTree(rq);
                qJson.addProperty("id", rq.getRemoteId());
                RequestBody requestBody = new JsonRequestBodyBuilder().param("data", qJson).param("user_id", player.getRemoteId()).build();
                return apiService.updateRecurrentQuest(requestBody, rq.getRemoteId()).compose(applySchedulers()).flatMap(sq -> {
                    updateRecurrentQuest(sq, rq.getId(), rq.getRecurrence());
                    return recurrentQuestPersistenceService.save(sq, false);
                });
            }
        });
    }

    private void updateRecurrentQuest(RecurrentQuest recurrentQuest, String recurrentQuestLocalId, Recurrence localRecurrence) {
        if (localRecurrence != null) {
            String localId = localRecurrence.getId();
            recurrentQuest.getRecurrence().setId(localId);
        }
        recurrentQuest.setRemoteId(recurrentQuest.getId());
        recurrentQuest.setId(recurrentQuestLocalId);
        recurrentQuest.setSyncedWithRemote();
    }

    Observable<RecurrentQuest> getRecurrentQuests(Player player) {
        return apiService.getRecurrentQuests(player.getRemoteId())
                .compose(applySchedulers()).flatMapIterable(recurrentQuests -> recurrentQuests)
                .flatMap(sq -> recurrentQuestPersistenceService.findByRemoteId(sq.getId()).flatMap(rq -> {
                    if (rq != null && sq.getUpdatedAt().getTime() <= rq.getUpdatedAt().getTime()) {
                        return Observable.just(rq);
                    }
                    String id = rq == null ? UUID.randomUUID().toString() : rq.getId();
                    Recurrence localRecurrence = rq == null ? null : rq.getRecurrence();
                    updateRecurrentQuest(sq, id, localRecurrence);
                    return recurrentQuestPersistenceService.save(sq, false);
                }));
    }

    Observable<Quest> getScheduleForAWeekAhead(Player player) {
        return Observable.just(DateUtils.getNext7Days()).concatMapIterable(dates -> dates)
                .concatMap(date -> apiService.getSchedule(date, player.getRemoteId()).compose(applySchedulers())).concatMapIterable(quests -> quests)
                .concatMap(sq -> questPersistenceService.findByRemoteId(sq.getId()).concatMap(q -> {
                    if (q != null && sq.getUpdatedAt().getTime() <= q.getUpdatedAt().getTime()) {
                        return Observable.just(q);
                    }
                    String id = q == null ? UUID.randomUUID().toString() : q.getId();
                    sq.setRemoteId(sq.getId());
                    sq.setId(id);
                    sq.setSyncedWithRemote();
                    if (sq.getRecurrentQuest() != null) {
                        return recurrentQuestPersistenceService.findByRemoteId(sq.getRecurrentQuest().getId()).flatMap(recurrentQuest -> {
                            sq.setRecurrentQuest(recurrentQuest);
                            return questPersistenceService.save(sq, false);
                        });
                    }
                    return questPersistenceService.save(sq, false);
                }));
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        App.getAppComponent(this).inject(this);

        subscription = getPlayer().flatMap(p -> Observable.concat(
                syncQuests(p),
                syncRecurrentQuests(p),
                getRecurrentQuests(p),
                getScheduleForAWeekAhead(p))).subscribe(res -> {
        }, throwable -> {
            Log.e("Observable", "Error", throwable);
            jobFinished(params, true);
        }, () -> jobFinished(params, false));
        return true;
    }

    <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<Player> syncUser(Player p) {
        RequestBody requestBody = new JsonRequestBodyBuilder().param("uid", p.getId()).param("provider", AuthProvider.ANONYMOUS).build();
        return apiService.createPlayer(requestBody).compose(applySchedulers()).concatMap(remotePlayer -> {
            remotePlayer.setRemoteId(remotePlayer.getId());
            remotePlayer.setId(p.getId());
            remotePlayer.setSyncedWithRemote();
            return playerPersistenceService.save(remotePlayer, false);
        });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        return false;
    }
}