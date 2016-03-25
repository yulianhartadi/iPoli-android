package io.ipoli.android.app.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.net.APIService;
import io.ipoli.android.app.net.AuthProvider;
import io.ipoli.android.app.net.JsonRequestBodyBuilder;
import io.ipoli.android.app.net.dto.QuestDTO;
import io.ipoli.android.app.net.dto.SnippetDTO;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
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
            SnippetDTO snippet = new SnippetDTO();
            snippet.text = "Meet with Jill at 12:00 +work";
            Log.d("Runnable", "Create snippet " + player);
            return createSnippet(snippet, player.getRemoteId());
        }).subscribe(questDTO -> {
            Log.d("Runnable", questDTO.toString());
        }, Throwable::printStackTrace);
        jobFinished((JobParameters) msg.obj, false);
        return true;
    });

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