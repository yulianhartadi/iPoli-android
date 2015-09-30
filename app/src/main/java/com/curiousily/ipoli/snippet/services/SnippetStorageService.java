package com.curiousily.ipoli.snippet.services;

import com.curiousily.ipoli.app.api.APIClient;
import com.curiousily.ipoli.app.api.AsyncAPICallback;
import com.curiousily.ipoli.snippet.Snippet;
import com.curiousily.ipoli.snippet.events.CreateSnippetEvent;
import com.curiousily.ipoli.snippet.services.events.SnippetCreatedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.client.Response;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class SnippetStorageService {
    private final APIClient client;
    private final Bus bus;

    public SnippetStorageService(APIClient client, Bus bus) {
        this.client = client;
        this.bus = bus;
    }

    @Subscribe
    public void onCreateInput(CreateSnippetEvent e) {
        client.createSnippet(e.snippet, new AsyncAPICallback<Snippet>() {
            @Override
            public void success(Snippet snippet, Response response) {
                bus.post(new SnippetCreatedEvent());
            }
        });
    }
}
