package com.curiousily.ipoli.app;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.schedule.DailySchedule;
import com.curiousily.ipoli.user.User;
import com.curiousily.ipoli.user.api.request.CreateUserRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public class PathDate {
        private static final ThreadLocal<DateFormat> DF = new ThreadLocal<DateFormat>() {
            @Override public DateFormat initialValue() {
                return new SimpleDateFormat(Constants.DEFAULT_SERVER_DATE_FORMAT, Locale.getDefault());
            }
        };

        private final Date date;

        public PathDate(Date date) {
            this.date = date;
        }

        @Override public String toString() {
            return DF.get().format(date);
        }
    }

    @Headers("Content-Type: application/json")
    @POST("/quests")
    void createQuest(@Body Quest quest, Callback<Quest> cb);

    @GET("/quests")
    void getDailyQuests(Callback<List<Quest>> cb);

    @GET("/schedules/{date}")
    void getDailySchedule(@Path("date") PathDate scheduledFor, @Query("user_id") String userId, Callback<DailySchedule> cb);

    @Headers("Content-Type: application/json")
    @PUT("/quests")
    void rateQuest(@Body Quest quest, Callback<Quest> cb);

    @Headers("Content-Type: application/json")
    @POST("/users")
    void createUser(@Body CreateUserRequest request, Callback<User> cb);
}
