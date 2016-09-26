package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Chronometer;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.quest.adapters.SubQuestListAdapter;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.ui.formatters.TimerFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/26/16.
 */

public class QuestDetailActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.quest_details_progress)
    ProgressBar timerProgress;

    @BindView(R.id.quest_details_time)
    Chronometer timer;

    @BindView(R.id.quest_details)
    RecyclerView details;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle("Workout");
        }
        collapsingToolbarLayout.setTitleEnabled(false);

        timer.setBase(0);
        timer.setText(TimerFormatter.format(TimeUnit.MINUTES.toMillis(20)));

        timerProgress.setProgress(33);

        List<SubQuest> subQuests = new ArrayList<>();
        subQuests.add(new SubQuest("Prepare Barbell"));
        subQuests.add(new SubQuest("Do 10 pull-ups"));
        subQuests.add(new SubQuest("Do 10 push-ups"));
        SubQuestListAdapter adapter = new SubQuestListAdapter(this, eventBus, subQuests);

        details.setLayoutManager(new LinearLayoutManager(this));
        details.setHasFixedSize(true);

        details.setAdapter(adapter);

//        adapter.setSubQuests(subQuests);
    }
}
