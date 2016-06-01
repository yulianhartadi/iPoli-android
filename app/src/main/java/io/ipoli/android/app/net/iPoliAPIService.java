package io.ipoli.android.app.net;

import java.util.List;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.Quest;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @GET("journeys/{date}")
    Observable<List<Quest>> getJourney(@Path("date") String date, @Query("player_id") String playerId);

    @POST("players")
    Observable<Player> createPlayer(@Body RequestBody request);

    @POST("players/{player_id}")
    Observable<Player> updatePlayer(@Body RequestBody data, @Path("player_id") String playerId);

    @POST("quests")
    Observable<Quest> createQuest(@Body RequestBody data);

    @POST("quests/{quest_id}")
    Observable<Quest> updateQuest(@Body RequestBody data, @Path("quest_id") String questId);

    @DELETE("quests/{quest_id}")
    Observable<Void> deleteQuest(@Path("quest_id") String questId, @Query("player_id") String playerId);

    @POST("snippets")
    Observable<RepeatingQuest> createRepeatingQuestFromText(@Body RequestBody data);

    @POST("repeating-quests")
    Observable<RepeatingQuest> createRepeatingQuest(@Body RequestBody data);

    @POST("repeating-quests/{repeating_quest_id}")
    Observable<RepeatingQuest> updateRepeatingQuest(@Body RequestBody data, @Path("repeating_quest_id") String repeatingQuestId);

    @GET("repeating-quests")
    Observable<List<RepeatingQuest>> getRepeatingQuests(@Query("player_id") String playerId);

    @DELETE("repeating-quests/{repeating_quest_id}")
    Observable<Void> deleteRepeatingQuest(@Path("repeating_quest_id") String repeatingQuestId, @Query("player_id") String playerId);
}
