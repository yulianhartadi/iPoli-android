package com.curiousily.ipoli.input.services;

import com.curiousily.ipoli.app.api.APIClient;
import com.curiousily.ipoli.app.api.AsyncAPICallback;
import com.curiousily.ipoli.input.Input;
import com.curiousily.ipoli.input.events.CreateInputEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.client.Response;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class InputStorageService {
    private final APIClient client;
    private final Bus bus;

    public InputStorageService(APIClient client, Bus bus) {
        this.client = client;
        this.bus = bus;
    }

    @Subscribe
    public void onCreateInput(CreateInputEvent e) {
        client.createInput(e.input, new AsyncAPICallback<Input>() {
            @Override
            public void success(Input input, Response response) {
            }
        });
    }
}
