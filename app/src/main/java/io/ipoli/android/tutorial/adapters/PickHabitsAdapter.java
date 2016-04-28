package io.ipoli.android.tutorial.adapters;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.tutorial.PickQuestViewModel;
import io.ipoli.android.tutorial.events.PredefinedHabitDeselectedEvent;
import io.ipoli.android.tutorial.events.PredefinedHabitSelectedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickHabitsAdapter extends BasePickQuestAdapter<RecurrentQuest> {


    public PickHabitsAdapter(Context context, Bus eventBus, List<PickQuestViewModel<RecurrentQuest>> viewModels) {
        super(context, eventBus, viewModels);
    }

    @Override
    protected void sendQuestDeselectEvent(int adapterPosition) {
        evenBus.post(new PredefinedHabitDeselectedEvent(viewModels.get(adapterPosition).getQuest().getRawText(), EventSource.TUTORIAL));
    }

    @Override
    protected void sendQuestSelectedEvent(int adapterPosition) {
        evenBus.post(new PredefinedHabitSelectedEvent(viewModels.get(adapterPosition).getQuest().getRawText(), EventSource.TUTORIAL));
    }

    @Override
    protected QuestContext getQuestContext(int adapterPosition) {
        return RecurrentQuest.getContext(viewModels.get(adapterPosition).getQuest());
    }
}