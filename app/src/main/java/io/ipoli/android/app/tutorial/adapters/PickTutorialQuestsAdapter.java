package io.ipoli.android.app.tutorial.adapters;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.adapters.BasePickQuestAdapter;
import io.ipoli.android.app.tutorial.PickQuestViewModel;
import io.ipoli.android.app.tutorial.events.PredefinedQuestDeselectedEvent;
import io.ipoli.android.app.tutorial.events.PredefinedQuestSelectedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickTutorialQuestsAdapter extends BasePickQuestAdapter {


    public PickTutorialQuestsAdapter(Context context, Bus eventBus, List<PickQuestViewModel> viewModels) {
        super(context, eventBus, viewModels);
    }

    @Override
    protected void sendQuestDeselectEvent(int adapterPosition) {
        evenBus.post(new PredefinedQuestDeselectedEvent(viewModels.get(adapterPosition).getText(), EventSource.TUTORIAL));
    }

    @Override
    protected void sendQuestSelectedEvent(int adapterPosition) {
        evenBus.post(new PredefinedQuestSelectedEvent(viewModels.get(adapterPosition).getText(), EventSource.TUTORIAL));
    }
}