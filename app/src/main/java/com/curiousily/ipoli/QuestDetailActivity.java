package com.curiousily.ipoli;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.curiousily.ipoli.databinding.ActivityQuestDetailBinding;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.viewmodel.QuestViewModel;
import com.curiousily.ipoli.ui.QuestDoneDialog;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/31/15.
 */
public class QuestDetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityQuestDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_quest_detail);
        QuestViewModel quest = new QuestViewModel();
        quest.name = "Welcome";
        quest.startTime = "10:00";
        quest.context = Quest.Context.PERSONAL.name();
        quest.backgroundColor = getResources().getColor(Quest.Context.PERSONAL.getPrimaryColor());
        quest.tags = "hello, new tag, welcome";
        quest.notes = "use the force";
        binding.setQuest(quest);
        ButterKnife.bind(this);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(quest.backgroundColor);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overrideExitAnimation();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overrideExitAnimation();
    }

    private void overrideExitAnimation() {
        overridePendingTransition(R.anim.reverse_slide_in, R.anim.reverse_slide_out);
    }

    private void showQuestRunningDialog(Quest quest) {

        DialogFragment newFragment = QuestDoneDialog.newInstance(quest);
        newFragment.show(getSupportFragmentManager(), Constants.ALERT_DIALOG_TAG);
    }

    @OnClick(R.id.quest_details_timer_icon)
    public void onTimerClick(View view) {
        showQuestRunningDialog(new Quest("Morning Routine", "Start fresh day", 9, Quest.Context.WELLNESS));
    }
}
