package io.ipoli.android.challenge.adapters;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.quest.adapters.BasePickQuestAdapter;
import io.ipoli.android.app.tutorial.PickQuestViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/15/16.
 */
public class ChallengePickQuestListAdapter extends BasePickQuestAdapter {

    public ChallengePickQuestListAdapter(Context context, Bus evenBus, List<PickQuestViewModel> viewModels, boolean isRepeatingIndicatorVisible) {
        super(context, evenBus, viewModels, isRepeatingIndicatorVisible);
    }

    @Override
    protected void sendQuestDeselectEvent(int adapterPosition) {

    }

    @Override
    protected void sendQuestSelectedEvent(int adapterPosition) {

    }
}
