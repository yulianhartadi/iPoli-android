package com.curiousily.ipoli.app.api;

import com.curiousily.ipoli.app.api.parameters.PathDate;
import com.curiousily.ipoli.snippet.Snippet;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.schedule.DailySchedule;
import com.curiousily.ipoli.user.User;
import com.curiousily.ipoli.user.api.request.CreateUserRequest;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/13/15.
 */
public interface APIClient {

    @Headers("Content-Type: application/json")
    @POST("/quests")
    void createQuest(@Body Quest quest, Callback<Object> cb);

    @Headers("Content-Type: application/json")
    @PUT("/quests")
    void updateQuest(@Body Quest quest, Callback<Quest> cb);

    @GET("/schedules/{date}")
    void getDailySchedule(@Path("date") PathDate scheduledFor, @Query("user_id") String userId, Callback<DailySchedule> cb);

    @Headers("Content-Type: application/json")
    @PUT("/schedules/")
    void updateSchedule(@Body DailySchedule schedule, Callback<DailySchedule> cb);

    @Headers("Content-Type: application/json")
    @POST("/users")
    void createUser(@Body CreateUserRequest request, Callback<User> cb);

    @Headers("Content-Type: application/json")
    @POST("/inputs")
    void createSnippet(@Body Snippet snippet, Callback<Snippet> cb);
}
