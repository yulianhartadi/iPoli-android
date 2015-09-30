package com.curiousily.ipoli.quest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.app.BaseActivity;
import com.curiousily.ipoli.quest.events.BuildQuestEvent;
import com.curiousily.ipoli.quest.events.CreateQuestEvent;
import com.curiousily.ipoli.quest.events.QuestBuiltEvent;
import com.curiousily.ipoli.quest.services.events.QuestCreatedEvent;
import com.curiousily.ipoli.quest.ui.AddQuestInfoFragment;
import com.curiousily.ipoli.quest.ui.AddQuestScheduleFragment;
import com.curiousily.ipoli.user.User;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddQuestActivity extends BaseActivity {

    @Bind(R.id.add_quest_loader)
    ProgressWheel loader;

    @Bind(R.id.add_quest_fragment_container)
    View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        addQuestScheduleFragment();
    }

    private void addQuestScheduleFragment() {
        Fragment questInfoFragment = new AddQuestInfoFragment();
        questInfoFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.add_quest_fragment_container, questInfoFragment).commit();
    }

    @Subscribe
    public void onBuildQuestEvent(BuildQuestEvent e) {
        AddQuestScheduleFragment questScheduleFragment = new AddQuestScheduleFragment();
        questScheduleFragment.setQuest(e.quest);
        questScheduleFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.add_quest_fragment_container, questScheduleFragment).addToBackStack(null).commit();
    }

    @Subscribe
    public void onQuestBuilt(QuestBuiltEvent e) {
        e.quest.createdBy = User.getCurrent(this);
        post(new CreateQuestEvent(e.quest));
        fragmentContainer.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onQuestSaved(QuestCreatedEvent e) {
        Toast.makeText(this, R.string.toast_quest_saved, Toast.LENGTH_LONG).show();
        finish();
    }

    private void post(Object event) {
        EventBus.post(event);
    }
}
