package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.NewQuestPriorityPickedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/17.
 */
public class AddQuestPriorityFragment extends BaseFragment {

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_priority, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    @OnClick({
            R.id.quest_priority_important_urgent,
            R.id.quest_priority_important_not_urgent,
            R.id.quest_priority_not_important_urgent,
            R.id.quest_priority_not_important_not_urgent
    })
    public void onPriorityClick(View view) {
        int priority = Quest.PRIORITY_IMPORTANT_URGENT;
        switch (view.getId()) {
            case R.id.quest_priority_important_not_urgent:
                priority = Quest.PRIORITY_IMPORTANT_NOT_URGENT;
                break;
            case R.id.quest_priority_not_important_urgent:
                priority = Quest.PRIORITY_NOT_IMPORTANT_URGENT;
                break;
            case R.id.quest_priority_not_important_not_urgent:
                priority = Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT;
                break;
        }
        postEvent(new NewQuestPriorityPickedEvent(priority));
    }
}