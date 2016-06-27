package io.ipoli.android.tutorial.adapters;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.tutorial.PickQuestViewModel;
import io.ipoli.android.tutorial.events.PredefinedQuestDeselectedEvent;
import io.ipoli.android.tutorial.events.PredefinedQuestSelectedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickQuestsAdapter extends BasePickQuestAdapter<Quest> {


    public PickQuestsAdapter(Context context, Bus eventBus, List<PickQuestViewModel<Quest>> viewModels) {
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

    @Override
    protected Category getQuestCategory(int adapterPosition) {
        return Quest.getCategory(viewModels.get(adapterPosition).getQuest());
    }
}