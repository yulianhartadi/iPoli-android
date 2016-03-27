package io.ipoli.android.app.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Date;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.net.APIService;
import io.ipoli.android.app.net.AuthProvider;
import io.ipoli.android.app.net.JsonRequestBodyBuilder;
import io.ipoli.android.app.net.UtcDateTypeAdapter;
import io.ipoli.android.app.net.dto.QuestDTO;
import io.ipoli.android.app.net.dto.SnippetDTO;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.realm.RealmObject;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public class AppJobService extends JobService {

    public static final int SYNC_JOB_ID = 1;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    APIService apiService;

    private Handler syncJobHandler = new Handler(msg -> {
        playerPersistenceService.find().concatMap(player -> {
            if (player == null) {
                return createNewPlayer();
            }
            if (player.needsSyncWithRemote()) {
                return syncUser(player);
            }
            return Observable.just(player);
        }).concatMap(player -> {
            return questPersistenceService.findAllWhoNeedSyncWithRemote();
//            List<Observable<QuestDTO>> res = new ArrayList<>();
////            for (Quest q : questPersistenceService.findAllWhoNeedSyncWithRemote()) {
////                SnippetDTO snippet = new SnippetDTO();
////                snippet.text = q.getName();
////                Log.d("Runnable", "Create snippet " + snippet.text + " player: " + player);
////                res.add(createSnippet(snippet, player.getRemoteId()));
////            }
//            return Observable.merge(res);
        }).map(quests -> {
            for (Quest q : quests) {
                if (TextUtils.isEmpty(q.getRemoteId())) {
                    q.setId(null);
                } else {
                    q.setId(q.getRemoteId());
                }
            }
            return quests;
        }).map(quests -> {
            Gson gson = createGson();
            JsonArray jQuests = (JsonArray) gson.toJsonTree(quests);
            for(int i = 0; i < jQuests.size(); i++) {
                JsonObject qJson = (JsonObject) jQuests.get(i);
                qJson.addProperty("end_date", DateUtils.toDateString(quests.get(i).getEndDate()));
            }
            return gson.toJson(jQuests);
        }).subscribe(questDTO -> {
            Log.d("Runnable", questDTO);
        }, Throwable::printStackTrace);
        jobFinished((JobParameters) msg.obj, false);
        return true;
    });

    @NonNull
    private Gson createGson() {
        return new GsonBuilder()
                        .setExclusionStrategies(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                if (f.getName().equals("remoteId")) {
                                    return true;
                                }
                                return f.getDeclaringClass().equals(RealmObject.class);
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        }).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .registerTypeAdapter(Date.class, new UtcDateTypeAdapter())
                        .create();
    }

    private Observable<Player> createNewPlayer() {
        Player defaultPlayer = new Player(Constants.DEFAULT_PLAYER_EXPERIENCE, Constants.DEFAULT_PLAYER_LEVEL, Constants.DEFAULT_PLAYER_AVATAR);
        return playerPersistenceService.save(defaultPlayer).concatMap(this::syncUser);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        App.getAppComponent(this).inject(this);
        syncJobHandler.sendMessage(Message.obtain(syncJobHandler, SYNC_JOB_ID, params));
        return true;
    }

    <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<Player> syncUser(Player p) {
        Log.d("Runnable", "Sync user");
        RequestBody requestBody = new JsonRequestBodyBuilder().param("uid", p.getId()).param("provider", AuthProvider.ANONYMOUS).build();
        return apiService.createUser(requestBody).compose(applySchedulers()).concatMap(userDTO -> {
            p.setRemoteId(userDTO.id);
            p.setSyncedWithRemote();
            return playerPersistenceService.save(p);
        });
    }

    private Observable<QuestDTO> createSnippet(SnippetDTO
                                                       snippet, String userId) {
        return apiService.createQuestFromSnippet(snippet, userId).compose(applySchedulers());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        syncJobHandler.removeMessages(SYNC_JOB_ID);
        return false;
    }
}