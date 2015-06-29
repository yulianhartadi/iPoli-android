package com.curiousily.ipoli.assistant.data;

import com.curiousily.ipoli.APIConstants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.assistant.api.iPoliApi;
import com.curiousily.ipoli.assistant.data.events.NewResponseEvent;
import com.curiousily.ipoli.assistant.data.models.Input;
import com.curiousily.ipoli.assistant.data.models.Response;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class StorageManager {

    private final iPoliApi api;

    public StorageManager() {
        api = new RestAdapter.Builder()
                .setEndpoint(APIConstants.URL)
                .build()
                .create(iPoliApi.class);
    }

    public void save(String text) {
        api.createInput(new Input(text, "user"), new Callback<Response>() {
            @Override
            public void success(Response response, retrofit.client.Response retrofitResponse) {
                EventBus.get().post(new NewResponseEvent(response));
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }
}
