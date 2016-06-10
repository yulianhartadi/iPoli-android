package io.ipoli.android.app.net;

import java.util.List;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/23/16.
 */
public interface iPoliAPIService {

    @POST("players")
    Call<Player> createPlayer(@Body RequestBody request);

    @POST("players/{player_id}")
    Call<Player> updatePlayer(@Body RequestBody data, @Path("player_id") String playerId);

    @POST("quests")
    Call<List<Quest>> syncQuests(@Body RequestBody data);

    @GET("quests")
    Call<List<Quest>> getQuests(@Query("player_id") String playerId);

    @POST("repeating-quests")
    Call<List<RepeatingQuest>> syncRepeatingQuests(@Body RequestBody data);

    @GET("repeating-quests")
    Call<List<RepeatingQuest>> getRepeatingQuests(@Query("player_id") String playerId);
}
