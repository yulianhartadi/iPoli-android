package com.curiousily.ipoli.quest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.events.CreateQuestEvent;
import com.curiousily.ipoli.quest.ui.AddQuestInfoFragment;
import com.curiousily.ipoli.quest.ui.AddQuestScheduleFragment;

import butterknife.ButterKnife;

public class AddQuestActivity extends AppCompatActivity {

    private Quest quest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        addQuestScheduleFragment();
    }

    private void addQuestScheduleFragment() {
        Fragment firstFragment = new AddQuestInfoFragment();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, firstFragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overrideExitAnimation();
    }

    public void overrideExitAnimation() {
        overridePendingTransition(R.anim.reverse_slide_in, R.anim.reverse_slide_out);
    }

    public void onNextClick(Quest quest) {
        this.quest = quest;
        Fragment secondFragment = new AddQuestScheduleFragment();
        secondFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, secondFragment).addToBackStack(null).commit();
    }

    public Quest getQuest() {
        return quest;
    }

    public void onDoneClick() {
        post(new CreateQuestEvent(quest));
    }

    private void post(Object event) {
        EventBus.get().post(event);
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
