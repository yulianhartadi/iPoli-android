package com.curiousily.ipoli.quest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.ui.AddQuestInfoFragment;
import com.curiousily.ipoli.quest.ui.AddQuestScheduleFragment;

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

    public void onNextClick() {
        Fragment secondFragment = new AddQuestScheduleFragment();
        secondFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, secondFragment).addToBackStack(null).commit();
    }

}
