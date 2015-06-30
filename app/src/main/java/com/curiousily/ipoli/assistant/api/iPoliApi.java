package com.curiousily.ipoli.assistant.api;

import com.curiousily.ipoli.assistant.data.models.Input;
import com.curiousily.ipoli.assistant.data.models.Response;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/29/15.
 */
public interface iPoliApi {
    @POST("/inputs/")
    void createInput(@Body Input input, Callback<Response> cb);
}
