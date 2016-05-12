package io.ipoli.android.app.net;

import java.util.List;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Habit;
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

    @GET("schedules/{date}")
    Observable<List<Quest>> getSchedule(@Path("date") String date, @Query("player_id") String playerId);

    @POST("players")
    Observable<Player> createPlayer(@Body RequestBody request);

    @POST("quests")
    Observable<Quest> createQuest(@Body RequestBody data);

    @POST("quests/{quest_id}")
    Observable<Quest> updateQuest(@Body RequestBody data, @Path("quest_id") String questId);

    @DELETE("quests/{quest_id}")
    Observable<Void> deleteQuest(@Path("quest_id") String questId, @Query("player_id") String playerId);

    @POST("snippets")
    Observable<Habit> createHabitFromText(@Body RequestBody data);

    @POST("habits/{quest_id}")
    Observable<Habit> updateHabit(@Body RequestBody data, @Path("quest_id") String questId);

    @GET("habits")
    Observable<List<Habit>> getHabits(@Query("player_id") String playerId);

    @DELETE("habits/{quest_id}")
    Observable<Void> deleteHabit(@Path("quest_id") String questId, @Query("player_id") String playerId);
}
