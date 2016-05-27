package io.ipoli.android.tutorial.adapters;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.tutorial.PickQuestViewModel;
import io.ipoli.android.tutorial.events.PredefinedRepeatingQuestDeselectedEvent;
import io.ipoli.android.tutorial.events.PredefinedRepeatingQuestSelectedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickRepeatingQuestsAdapter extends BasePickQuestAdapter<RepeatingQuest> {


    public PickRepeatingQuestsAdapter(Context context, Bus eventBus, List<PickQuestViewModel<RepeatingQuest>> viewModels) {
        super(context, eventBus, viewModels);
    }

    @Override
    protected void sendQuestDeselectEvent(int adapterPosition) {
        evenBus.post(new PredefinedRepeatingQuestDeselectedEvent(viewModels.get(adapterPosition).getQuest().getRawText(), EventSource.TUTORIAL));
    }

    @Override
    protected void sendQuestSelectedEvent(int adapterPosition) {
        evenBus.post(new PredefinedRepeatingQuestSelectedEvent(viewModels.get(adapterPosition).getQuest().getRawText(), EventSource.TUTORIAL));
    }

    @Override
    protected QuestContext getQuestContext(int adapterPosition) {
        return RepeatingQuest.getContext(viewModels.get(adapterPosition).getQuest());
    }
}