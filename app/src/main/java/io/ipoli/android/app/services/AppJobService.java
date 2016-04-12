package io.ipoli.android.app.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
                return syncPlayer(player);
            }
            return Observable.just(player);
        });
    }

    private Observable<Player> createNewPlayer() {
        Player defaultPlayer = new Player(Constants.DEFAULT_PLAYER_EXPERIENCE, Constants.DEFAULT_PLAYER_LEVEL, Constants.DEFAULT_PLAYER_AVATAR);
        return playerPersistenceService.save(defaultPlayer, false).concatMap(this::syncPlayer);
    }

    Observable<Quest> syncQuests(Player player) {
        return questPersistenceService.findAllWhoNeedSyncWithRemote().concatMapIterable(quests -> quests).concatMap(q -> {
            JsonObject qJson = (JsonObject) gson.toJsonTree(q);
            if (!q.isRemoteObject()) {
                String id = null;
                qJson.addProperty("id", id);
            }
            RequestBody requestBody = new JsonRequestBodyBuilder().param("data", qJson).param("user_id", player.getId()).build();
            return apiService.updateQuest(requestBody).compose(applySchedulers()).concatMap(sq -> {
                if (!q.isRemoteObject()) {
                    questPersistenceService.updateId(q, sq.getId());
                }
                sq.setSyncedWithRemote();
                sq.setRemoteObject();
                return questPersistenceService.save(sq, false);
            });
        });
    }

    Observable<RecurrentQuest> syncRecurrentQuests(Player player) {
        return recurrentQuestPersistenceService.findAllWhoNeedSyncWithRemote().flatMapIterable(recurrentQuests -> recurrentQuests).flatMap(rq -> {
            if (rq.isRemoteObject()) {
                JsonObject qJson = (JsonObject) gson.toJsonTree(rq);
                qJson.addProperty("id", rq.getId());
                RequestBody requestBody = new JsonRequestBodyBuilder().param("data", qJson).param("user_id", player.getId()).build();
                return apiService.updateRecurrentQuest(requestBody, rq.getId()).compose(applySchedulers()).flatMap(sq -> {
                    updateRecurrentQuest(sq, rq, rq.getRecurrence());
                    return recurrentQuestPersistenceService.save(sq, false);
                });
            } else {
                JsonObject data = new JsonObject();
                JsonObject qJson = (JsonObject) gson.toJsonTree(rq);
                data.addProperty("text", qJson.get("raw_text").getAsString());
                data.addProperty("context", qJson.get("context").getAsString());
                data.addProperty("created_at", qJson.get("created_at").getAsString());
                data.addProperty("updated_at", qJson.get("updated_at").getAsString());
                RequestBody requestBody = new JsonRequestBodyBuilder().param("data", data).param("user_id", player.getId()).build();
                return apiService.createRecurrentQuest(requestBody).compose(applySchedulers()).flatMap(sq -> {
                    updateRecurrentQuest(sq, rq, rq.getRecurrence());
                    return recurrentQuestPersistenceService.save(sq, false);
                });
            }
        });
    }

    private void updateRecurrentQuest(RecurrentQuest serverQuest, RecurrentQuest localQuest, Recurrence localRecurrence) {
        if (localRecurrence != null) {
            String localId = localRecurrence.getId();
            serverQuest.getRecurrence().setId(localId);
        }
        if (!localQuest.isRemoteObject()) {
            recurrentQuestPersistenceService.updateId(localQuest, serverQuest.getId());
        }
        serverQuest.setSyncedWithRemote();
        serverQuest.setRemoteObject();
    }

    Observable<RecurrentQuest> getRecurrentQuests(Player player) {
        return apiService.getRecurrentQuests(player.getId())
                .compose(applySchedulers()).flatMapIterable(recurrentQuests -> recurrentQuests)
                .flatMap(sq -> recurrentQuestPersistenceService.findById(sq.getId()).flatMap(rq -> {
                    if (rq != null && sq.getUpdatedAt().getTime() <= rq.getUpdatedAt().getTime()) {
                        return Observable.just(rq);
                    }

                    if (rq == null) {
                        sq.setRemoteObject();
                        sq.setSyncedWithRemote();
                        return recurrentQuestPersistenceService.save(sq, false);
                    }
                    updateRecurrentQuest(sq, rq, rq.getRecurrence());
                    return recurrentQuestPersistenceService.save(sq, false);
                }));
    }

    Observable<Quest> getScheduleForAWeekAhead(Player player) {
        return Observable.just(DateUtils.getNext7Days()).concatMapIterable(dates -> dates)
                .concatMap(date -> apiService.getSchedule(date, player.getId()).compose(applySchedulers())).concatMapIterable(quests -> quests)
                .concatMap(sq -> questPersistenceService.findById(sq.getId()).concatMap(q -> {
                    if (q != null && sq.getUpdatedAt().getTime() <= q.getUpdatedAt().getTime()) {
                        return Observable.just(q);
                    }

                    sq.setSyncedWithRemote();
                    sq.setRemoteObject();

                    if (q != null && !q.isRemoteObject()) {
                        questPersistenceService.updateId(q, sq.getId());
                    }

                    return questPersistenceService.save(sq, false);
                }));
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        App.getAppComponent(this).inject(this);

        subscription = getPlayer().flatMap(p -> Observable.concat(
                syncRecurrentQuests(p),
                syncQuests(p),
                getRecurrentQuests(p),
                getScheduleForAWeekAhead(p))).subscribe(res -> {
            Log.d("RxJava", "OnNext " + res);
        }, throwable -> {
            Log.e("RxJava", "Error", throwable);
            jobFinished(params, true);
        }, () -> {
            Log.d("RxJava", "Sync Job finished");
            jobFinished(params, false);
        });
        return true;
    }

    <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<Player> syncPlayer(Player p) {
        RequestBody requestBody = new JsonRequestBodyBuilder().param("uid", p.getId()).param("provider", AuthProvider.ANONYMOUS).build();
        return apiService.createPlayer(requestBody).compose(applySchedulers()).concatMap(sp -> {
            playerPersistenceService.updateId(p, sp.getId());
            sp.setSyncedWithRemote();
            sp.setRemoteObject();
            return playerPersistenceService.save(sp, false);
        });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("RxJava", "Stopping job" + params);
//        if (subscription != null) {
//            subscription.unsubscribe();
//        }
        return false;
    }
}