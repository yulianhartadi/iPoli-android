package io.ipoli.android.challenge.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.adapters.ChallengeQuestListAdapter;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.viewmodels.ChallengeQuestViewModel;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.PeriodHistory;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;

import static io.ipoli.android.app.utils.DateUtils.getMonthShortName;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class ChallengeActivity extends BaseActivity {

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.challenge_name)
    TextView challengeName;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.challenge_progress_fraction)
    TextView progressFraction;

    @BindView(R.id.challenge_progress_percent)
    TextView progressPercent;

    @BindView(R.id.challenge_history)
    BarChart history;

    @BindView(R.id.challenge_progress)
    ProgressBar progress;

    @BindView(R.id.challenge_summary_stats_container)
    ViewGroup summaryStatsContainer;

    @BindView(R.id.challenge_category_image)
    ImageView categoryImage;

    @BindView(R.id.challenge_category_name)
    TextView categoryName;

    @BindView(R.id.challenge_next_scheduled_date)
    TextView nextScheduledDate;

    @BindView(R.id.challenge_due_date)
    TextView dueDate;

    @BindView(R.id.challenge_total_time_spent)
    TextView totalTimeSpent;

    @BindView(R.id.quest_list_container)
    ViewGroup questListContainer;

    @BindView(R.id.quest_list)
    EmptyStateRecyclerView questList;

    @Inject
    Bus eventBus;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    private ChallengeQuestListAdapter adapter;

    private String challengeId;
    private Challenge challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || TextUtils.isEmpty(getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY))) {
            finish();
            return;
        }
        setContentView(R.layout.activity_challenge);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
        collapsingToolbarLayout.setTitleEnabled(false);
        history.setNoDataText("");
        getWindow().setBackgroundDrawable(null);
        appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (collapsingToolbarLayout.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbarLayout)) {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            } else {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        });

        challengeId = getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);
        questList.setEmptyView(questListContainer, R.string.empty_daily_challenge_quests_text, R.drawable.ic_compass_grey_24dp);
        adapter = new ChallengeQuestListAdapter(this, new ArrayList<>(), eventBus);
        questList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        challengePersistenceService.listenById(challengeId, challenge -> {
            if (challenge == null) {
                finish();
                return;
            }
            this.challenge = challenge;
            displayChallenge();
            setBackgroundColors(Challenge.getCategory(challenge));
            onQuestListUpdated();
        });
    }

    private void onQuestListUpdated() {
        List<ChallengeQuestViewModel> viewModels = new ArrayList<>();
        for (Quest q : challenge.getChallengeQuests().values()) {
            viewModels.add(new ChallengeQuestViewModel(q, false));
        }
        for (RepeatingQuest rq : challenge.getChallengeRepeatingQuests().values()) {
            viewModels.add(new ChallengeQuestViewModel(rq, true));
        }
        adapter.setViewModels(viewModels);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.challenge_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent i = new Intent(this, EditChallengeActivity.class);
                i.putExtra(Constants.CHALLENGE_ID_EXTRA_KEY, challenge.getId());
                startActivity(i);
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_challenge, R.string.help_dialog_challenge_title, "challenge").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    public Challenge getChallenge() {
        return challenge;
    }

    @Override
    protected void onStop() {
        challengePersistenceService.removeAllListeners();
        super.onStop();
    }

    private void displayChallenge() {
        challengeName.setText(challenge.getName());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(challenge.getName());
        }
        showProgress();
        showSummaryStats();
        setupChart();
    }

    private void showProgress() {

        int totalCount = challenge.getTotalQuestCount();
        int completed = challenge.getCompletedQuestCount();

        progressFraction.setText(completed + " / " + totalCount);

        int percentDone = Math.round((completed / (float) totalCount) * 100);

        progressPercent.setText(getString(R.string.challenge_percentage_done, percentDone));

        int progressColor = R.color.colorAccent;

        Category category = Challenge.getCategory(challenge);
        if (category == Category.WORK || category == Category.FUN || category == Category.CHORES) {
            progressColor = R.color.colorAccentAlternative;
        }
        progress.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, progressColor), PorterDuff.Mode.SRC_IN);
        progress.setProgress(percentDone);

        ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", 0, percentDone);
        int animationTime = getResources().getInteger(percentDone > 50 ? android.R.integer.config_longAnimTime : android.R.integer.config_mediumAnimTime);
        animation.setDuration(animationTime);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void showSummaryStats() {
        Category category = Challenge.getCategory(challenge);

        summaryStatsContainer.setBackgroundResource(category.color500);

        categoryName.setText(StringUtils.capitalize(category.name()));
        categoryImage.setImageResource(category.whiteImage);

        String nextScheduledDateText = DateFormatter.formatWithoutYear(
                challenge.getNextScheduledDate(LocalDate.now()),
                getString(R.string.unscheduled)
        );
        nextScheduledDate.setText(nextScheduledDateText);

        dueDate.setText(DateFormatter.formatWithoutYear(challenge.getEndDate()));

        int timeSpent = challenge.getTotalTimeSpent();
        totalTimeSpent.setText(timeSpent > 0 ? DurationFormatter.formatShort(timeSpent, "") : "0");
    }

    private void setupChart() {
        history.setDescription("");
        history.setTouchEnabled(false);
        history.setPinchZoom(false);
        history.setExtraBottomOffset(20);

        history.setDrawGridBackground(false);
        history.setDrawBarShadow(true);

        history.setDrawValueAboveBar(false);
        history.setDrawGridBackground(false);

        YAxis leftAxis = history.getAxisLeft();
        leftAxis.setAxisMinValue(0f);
        leftAxis.setSpaceTop(0);
        leftAxis.setEnabled(false);
        history.getAxisRight().setEnabled(false);

        XAxis xLabels = history.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_54));
        xLabels.setLabelsToSkip(0);
        xLabels.setTextSize(13f);
        xLabels.setDrawAxisLine(false);
        xLabels.setDrawGridLines(false);
        xLabels.setYOffset(5);
        history.getLegend().setEnabled(false);

        List<PeriodHistory> periodHistories = challenge.getPeriodHistories(LocalDate.now());

        List<BarEntry> yValues = new ArrayList<>();
        for (int i = 0; i < periodHistories.size(); i++) {
            PeriodHistory p = periodHistories.get(i);
            yValues.add(new BarEntry(p.getCompletedCount(), i));
        }

        BarDataSet dataSet = new BarDataSet(yValues, "");
        dataSet.setColors(getColors());
        dataSet.setBarShadowColor(ContextCompat.getColor(this, Challenge.getCategory(challenge).color100));

        List<String> xValues = new ArrayList<>();
        xValues.add(getWeekRangeText(periodHistories.get(0).getStartDate(), periodHistories.get(0).getEndDate()));
        xValues.add(getWeekRangeText(periodHistories.get(1).getStartDate(), periodHistories.get(1).getEndDate()));
        xValues.add(getString(R.string.last_week));
        xValues.add(getString(R.string.this_week));
        setHistoryData(dataSet, xValues);

    }

    private void setHistoryData(BarDataSet dataSet, List<String> xValues) {
        BarData data = new BarData(xValues, dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (value == 0) {
                return "";
            }
            return String.valueOf((int) value);
        });

        history.setData(data);
        history.invalidate();
        history.animateY(1400, Easing.EasingOption.EaseInOutQuart);
    }

    private int[] getColors() {
        int[] colors = new int[Constants.DEFAULT_BAR_COUNT];
        for (int i = 0; i < Constants.DEFAULT_BAR_COUNT; i++) {
            colors[i] = ContextCompat.getColor(this, Challenge.getCategory(challenge).color300);
        }
        return colors;
    }

    private String getWeekRangeText(long weekStart, long weekEnd) {
        return getWeekRangeText(DateUtils.fromMillis(weekStart), DateUtils.fromMillis(weekEnd));
    }

    private String getWeekRangeText(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart.getMonth().equals(weekEnd.getMonth())) {
            return weekStart.getDayOfMonth() + " - " + weekEnd.getDayOfMonth() + " " + getMonthShortName(weekEnd);
        } else {
            return weekStart.getDayOfMonth() + " " + getMonthShortName(weekStart) + " - " + weekEnd.getDayOfMonth() + " " + getMonthShortName(weekEnd);
        }
    }

    private void setBackgroundColors(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));

    }

    @OnClick(R.id.add_quests)
    public void onAddQuestsClick(View v) {
        Intent intent = new Intent(this, PickChallengeQuestsActivity.class);
        intent.putExtra(Constants.CHALLENGE_ID_EXTRA_KEY, challengeId);
        startActivity(intent);
    }
}