package io.ipoli.android.app.net;

import java.util.List;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/23/16.
 */
public interface iPoliAPIService {

    @POST("players")
    Observable<Player> createPlayer(@Body RequestBody request);

    @POST("players/{player_id}")
    Observable<Player> updatePlayer(@Body RequestBody data, @Path("player_id") String playerId);

    @POST("quests")
    Observable<List<Quest>> syncQuests(@Body RequestBody data);

    @POST("repeating-quests")
    Observable<List<RepeatingQuest>> syncRepeatingQuests(@Body RequestBody data);

    @GET("repeating-quests")
    Observable<List<RepeatingQuest>> getRepeatingQuests(@Query("player_id") String playerId);
}
