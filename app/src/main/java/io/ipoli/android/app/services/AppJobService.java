package io.ipoli.android.app.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.net.APIService;
import io.ipoli.android.app.net.AuthProvider;
import io.ipoli.android.app.net.JsonRequestBodyBuilder;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
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
        return playerPersistenceService.save(defaultPlayer).concatMap(this::syncUser);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        App.getAppComponent(this).inject(this);

        subscription = getPlayer().flatMap(player -> recurrentQuestPersistenceService.findAllWhoNeedSyncWithRemote().flatMapIterable(recurrentQuests -> recurrentQuests).flatMap(rq -> {
            if (TextUtils.isEmpty(rq.getRemoteId())) {
                RequestBody requestBody = new JsonRequestBodyBuilder().param("text", rq.getRawText()).param("user_id", player.getRemoteId()).build();
                return apiService.createRecurrentQuest(requestBody).compose(applySchedulers()).flatMap(sq -> {
                    sq.setRemoteId(sq.getId());
                    sq.setId(rq.getId());
                    sq.setSyncedWithRemote();
                    return recurrentQuestPersistenceService.save(sq);
                });
            } else {
                JsonObject qJson = (JsonObject) gson.toJsonTree(rq);
                qJson.addProperty("id", rq.getRemoteId());
                RequestBody requestBody = new JsonRequestBodyBuilder().param("data", qJson).param("user_id", player.getRemoteId()).build();
                return apiService.updateRecurrentQuest(requestBody, rq.getRemoteId()).compose(applySchedulers()).flatMap(sq -> {
                    sq.setRemoteId(sq.getId());
                    sq.setId(rq.getId());
                    sq.setSyncedWithRemote();
                    return recurrentQuestPersistenceService.save(sq);
                });
            }
        })).subscribe(res -> {
            Log.d("Complete", "all done " + res.toString());
//            jobFinished(params, false);
        });

//        subscription = getPlayer().flatMap(player -> questPersistenceService.findAllWhoNeedSyncWithRemote().flatMap(quests -> {
//            if (quests.isEmpty()) {
//                return Observable.just(new ArrayList<Quest>());
//            }
//            JsonArray jsonQuests = new JsonArray();
//            for (Quest q : quests) {
//                JsonObject qJson = (JsonObject) gson.toJsonTree(q);
//                if (TextUtils.isEmpty(q.getRemoteId())) {
//                    String id = null;
//                    qJson.addProperty("id", id);
//                } else {
//                    qJson.addProperty("id", q.getRemoteId());
//                }
//                if (q.getEndDate() != null) {
//                    qJson.addProperty("end_date", DateUtils.toDateString(q.getEndDate()));
//                }
//                if (q.getStartDate() != null) {
//                    qJson.addProperty("start_date", DateUtils.toDateString(q.getStartDate()));
//                }
//                jsonQuests.add(qJson);
//            }
//            RequestBody requestBody = new JsonRequestBodyBuilder().param("data", jsonQuests).param("user_id", player.getRemoteId()).build();
//            return apiService.updateQuests(requestBody).compose(applySchedulers())
//                    .flatMap(serverQuests -> {
//                        List<Quest> questsToSave = new ArrayList<>();
//                        for (int i = 0; i < quests.size(); i++) {
//                            Quest q = quests.get(i);
//                            Quest sq = serverQuests.get(i);
//                            sq.setRemoteId(sq.getId());
//                            sq.setId(q.getId());
//                            sq.setSyncedWithRemote();
//                            questsToSave.add(sq);
//                        }
//                        return questPersistenceService.saveAll(questsToSave, false);
//                    });
//        })).subscribe(res -> {
//            Log.d("Complete", "all done ");
//            jobFinished(params, false);
//        });

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