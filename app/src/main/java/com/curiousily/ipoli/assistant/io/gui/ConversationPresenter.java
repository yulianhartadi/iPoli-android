package com.curiousily.ipoli.assistant.io.gui;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.assistant.io.event.NewResponseEvent;
import com.curiousily.ipoli.assistant.io.event.NewMessageEvent;
import com.curiousily.ipoli.assistant.io.event.NewQueryEvent;
import com.curiousily.ipoli.ui.events.ChangeInputEvent;
import com.squareup.otto.Subscribe;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class ConversationPresenter {

    public ConversationPresenter() {
        EventBus.get().register(this);
    }

    @Subscribe
    public void onResponseReceived(NewResponseEvent e) {
        post(new NewMessageEvent(e.getResponse(), ChangeInputEvent.Author.iPoli));
    }

    @Subscribe
    public void onQueryReceived(NewQueryEvent e) {
        post(new NewMessageEvent(e.getQuery(), ChangeInputEvent.Author.User));
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }
}
