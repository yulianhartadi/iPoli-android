package com.curiousily.ipoli.quest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.events.BuildQuestEvent;
import com.curiousily.ipoli.quest.events.CreateQuestEvent;
import com.curiousily.ipoli.quest.events.QuestBuiltEvent;
import com.curiousily.ipoli.quest.ui.AddQuestInfoFragment;
import com.curiousily.ipoli.quest.ui.AddQuestScheduleFragment;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

public class AddQuestActivity extends AppCompatActivity {

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
                .add(R.id.fragment_container, questInfoFragment).commit();
    }

    @Subscribe
    public void onBuildQuestEvent(BuildQuestEvent e) {
        AddQuestScheduleFragment questScheduleFragment = new AddQuestScheduleFragment();
        questScheduleFragment.setQuest(e.quest);
        questScheduleFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, questScheduleFragment).addToBackStack(null).commit();
    }

    @Subscribe
    public void onQuestBuiltEvent(QuestBuiltEvent e) {
        post(new CreateQuestEvent(e.quest));
    }

    private void post(Object event) {
        EventBus.post(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.get().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.get().unregister(this);
    }
}
