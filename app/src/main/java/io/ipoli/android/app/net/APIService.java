package io.ipoli.android.app.net;

import java.util.List;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RecurrentQuest;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/23/16.
 */
public interface APIService {

    String API_ENDPOINT = "http://10.0.3.2:8080/v1/";

    @GET("schedules/{date}")
    Observable<List<Quest>> getSchedule(@Path("date") String date, @Query("user_id") String userId);

    @POST("users")
    Observable<Player> createPlayer(@Body RequestBody request);

    @POST("quests")
    Observable<List<Quest>> updateQuests(@Body RequestBody data);

    @POST("quests")
    Observable<Quest> updateQuest(@Body RequestBody data);

    @POST("snippets")
    Observable<RecurrentQuest> createRecurrentQuest(@Body RequestBody data);

    @PUT("quests/{quest_id}?quest_type=recurrent")
    Observable<RecurrentQuest> updateRecurrentQuest(@Body RequestBody data, @Path("quest_id") String questId);

    @GET("quests?quest_type=recurrent")
    Observable<List<RecurrentQuest>> getRecurrentQuests(@Query("user_id") String userId);
}
