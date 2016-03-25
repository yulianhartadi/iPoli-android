package io.ipoli.android.app.net;

import java.util.List;

import io.ipoli.android.app.net.dto.QuestDTO;
import io.ipoli.android.app.net.dto.SnippetDTO;
import io.ipoli.android.app.net.dto.UserDTO;
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
public interface APIService {

    String API_ENDPOINT = "http://10.0.3.2:8080/v1/";

    @GET("schedules/{date}")
    Observable<List<QuestDTO>> getSchedule(@Path("date") String date, @Query("user_id") String userId);

    @POST("quests")
    Observable<QuestDTO> createQuest(@Body QuestDTO quest, @Query("user_id") String userId);

    @POST("snippets")
    Observable<QuestDTO> createQuestFromSnippet(@Body SnippetDTO snippet, @Query("user_id") String userId);

    @POST("users")
    Observable<UserDTO> createUser(@Body RequestBody request);
}
