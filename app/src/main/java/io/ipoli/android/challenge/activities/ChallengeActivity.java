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
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.ui.formatters.DateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;

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

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    ChallengePersistenceService challengePersistenceService;

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

        appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (collapsingToolbarLayout.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbarLayout)) {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            } else {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        });

        challengeId = getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        challengePersistenceService.listenById(challengeId, challenge -> {
            this.challenge = challenge;
            displayChallenge();
            setBackgroundColors(Challenge.getCategory(challenge));
        });
    }

    private void setBackgroundColors(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));

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
                finish();
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

    private long totalCount = 0;

    private void showProgress() {
        totalCount = 0;
        Date challengeEnd = challenge.getEndDate();
        LocalDate today = LocalDate.now();
        LocalDate scheduledQuestsEndDate = today.plusWeeks(3).dayOfWeek().withMaximumValue();

        if (notAllQuestsAreScheduledForChallenge(challengeEnd, scheduledQuestsEndDate)) {
            repeatingQuestPersistenceService.findByChallenge(challenge, repeatingQuests -> {
                Date challengeStart = DateUtils.toStartOfDayUTC(new LocalDate(challenge.getCreatedAt(), DateTimeZone.UTC));
                for (RepeatingQuest rq : repeatingQuests) {
                    Recurrence recurrence = rq.getRecurrence();
                    Date rqStart = recurrence.getDtstartDate();
                    Date rqEnd = recurrence.getDtendDate();

                    Date progressStart = challengeStart.after(rqStart) ? challengeStart : rqStart;
                    Date progressEnd = rqEnd == null || challengeEnd.before(rqEnd) ? challengeEnd : rqEnd;

                    float questsPerDayCoefficient = calculateQuestsPerDayCoefficient(recurrence) * recurrence.getTimesADay();

                    int dayCount = (int) TimeUnit.MILLISECONDS.toDays(progressEnd.getTime() - progressStart.getTime()) + 1;
                    totalCount += Math.ceil(dayCount * questsPerDayCoefficient);
                }
                questPersistenceService.countNotRepeating(challenge.getId(), count -> {
                    totalCount += count;
                    populateProgress();
                });
            });
        } else {
            questPersistenceService.countNotDeleted(challenge.getId(), count -> {
                totalCount = count;
                populateProgress();
            });
        }

    }

    private void populateProgress() {
        questPersistenceService.countCompletedForChallenge(challenge.getId(), completed -> {
            progressFraction.setText(completed + " / " + totalCount);

            int percentDone = Math.round((completed / (float) totalCount) * 100);

            progressPercent.setText(String.valueOf(percentDone) + "% done");

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
        });

    }

    private float calculateQuestsPerDayCoefficient(Recurrence recurrence) {
        Recurrence.RecurrenceType recurrenceType = recurrence.getRecurrenceType();
        if (recurrenceType == Recurrence.RecurrenceType.MONTHLY) {
            if (recurrence.isFlexible()) {
                return recurrence.getFlexibleCount() / 31f;
            } else {
                return 1f / 31f;
            }
        } else if (recurrenceType == Recurrence.RecurrenceType.WEEKLY) {
            if (recurrence.isFlexible()) {
                return recurrence.getFlexibleCount() / 7f;
            } else {
                try {
                    return new Recur(recurrence.getRrule()).getDayList().size() / 7f;
                } catch (ParseException e) {
                    return 0.0f;
                }
            }
        } else {
            return 1f;
        }
    }

    private boolean notAllQuestsAreScheduledForChallenge(Date challengeEnd, LocalDate scheduledQuestsEndDate) {
        return new LocalDate(challengeEnd, DateTimeZone.UTC).isAfter(scheduledQuestsEndDate);
    }

    private void showSummaryStats() {
        Category category = Challenge.getCategory(challenge);

        summaryStatsContainer.setBackgroundResource(category.color500);

        categoryName.setText(StringUtils.capitalize(category.name()));
        categoryImage.setImageResource(category.whiteImage);

        questPersistenceService.findNextUncompletedQuestEndDate(challenge.getId(), nextDate ->
                nextScheduledDate.setText(DateFormatter.formatWithoutYear(nextDate, getString(R.string.unscheduled))));


        dueDate.setText(DateFormatter.formatWithoutYear(challenge.getEndDate()));

        questPersistenceService.findAllCompleted(challenge.getId(), quests -> {
            int timeSpent = (int) getTotalTimeSpent(quests);
            totalTimeSpent.setText(timeSpent > 0 ? DurationFormatter.formatShort(timeSpent, "") : "0");
        });
    }

    private long getTotalTimeSpent(List<Quest> completedQuests) {
        long totalTime = 0;
        for (Quest completedQuest : completedQuests) {
            totalTime += completedQuest.getActualDuration();
        }
        return totalTime;
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

        questPersistenceService.countCompletedByWeek(challenge.getId(), Constants.DEFAULT_BAR_COUNT, counts -> {
            List<BarEntry> yValues = new ArrayList<>();
            List<Pair<LocalDate, LocalDate>> weekPairs = DateUtils.getBoundsForWeeksInThePast(LocalDate.now(), Constants.DEFAULT_BAR_COUNT);
            for (int i = 0; i < counts.size(); i++) {
                yValues.add(new BarEntry(counts.get(i), i));
            }

            BarDataSet dataSet = new BarDataSet(yValues, "");
            dataSet.setColors(getColors());
            dataSet.setBarShadowColor(ContextCompat.getColor(this, Challenge.getCategory(challenge).color100));

            List<String> xValues = new ArrayList<>();
            xValues.add(getWeekRangeText(weekPairs.get(0).first, weekPairs.get(0).second));
            xValues.add(getWeekRangeText(weekPairs.get(1).first, weekPairs.get(1).second));
            xValues.add("last week");
            xValues.add("this week");
            setHistoryData(dataSet, xValues);
        });

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

    private String getWeekRangeText(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart.getMonthOfYear() == weekEnd.getMonthOfYear()) {
            return weekStart.getDayOfMonth() + " - " + weekEnd.getDayOfMonth() + " " + weekEnd.monthOfYear().getAsShortText();
        } else {
            return weekStart.getDayOfMonth() + " " + weekStart.monthOfYear().getAsShortText() + " - " + weekEnd.getDayOfMonth() + " " + weekEnd.monthOfYear().getAsShortText();
        }
    }
}