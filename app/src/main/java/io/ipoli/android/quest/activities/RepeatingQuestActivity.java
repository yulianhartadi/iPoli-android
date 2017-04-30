package io.ipoli.android.quest.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.ui.formatters.FrequencyTextFormatter;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.PeriodHistory;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

import static io.ipoli.android.app.utils.DateUtils.getMonthShortName;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/9/16.
 */
public class RepeatingQuestActivity extends BaseActivity {

    @BindView(R.id.repeating_quest_progress_container)
    ViewGroup progressContainer;

    @BindView(R.id.repeating_quest_history)
    BarChart history;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.quest_name)
    TextView name;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.quest_category_image)
    ImageView categoryImage;

    @BindView(R.id.quest_category_name)
    TextView categoryName;

    @BindView(R.id.quest_next_scheduled_date)
    TextView nextScheduledDate;

    @BindView(R.id.quest_frequency_interval)
    TextView frequencyInterval;

    @BindView(R.id.quest_total_time_spent)
    TextView totalTimeSpent;

    @BindView(R.id.quest_streak)
    TextView streakText;

    private RepeatingQuest repeatingQuest;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    private String repeatingQuestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || StringUtils.isEmpty(getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY))) {
            finish();
            return;
        }

        repeatingQuestId = getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY);

        setContentView(R.layout.activity_repeating_quest);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.repeating_quest_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent i = new Intent(this, EditQuestActivity.class);
                i.putExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY, repeatingQuest.getId());
                startActivity(i);
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_repeating_quest, R.string.help_dialog_repeating_quest_title, "repeating_quest").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        repeatingQuestPersistenceService.listenById(repeatingQuestId, this::onRepeatingQuestFound);
    }

    @Override
    protected void onStop() {
        repeatingQuestPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    private void onRepeatingQuestFound(RepeatingQuest repeatingQuest) {
        if (repeatingQuest == null) {
            finish();
            return;
        }
        this.repeatingQuest = repeatingQuest;
        eventBus.post(new ScreenShownEvent(EventSource.REPEATING_QUEST));
        displayRepeatingQuest();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    private void displayRepeatingQuest() {
        name.setText(repeatingQuest.getName());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(repeatingQuest.getName());
        }

        Category category = repeatingQuest.getCategoryType();
        List<PeriodHistory> periodHistories = repeatingQuest.getPeriodHistories(LocalDate.now());
        showFrequencyProgress(category, periodHistories.get(periodHistories.size() - 1));
        displaySummaryStats(category);
        colorLayout(category);
        setupChart(periodHistories);
    }

    private void displaySummaryStats(Category category) {
        categoryName.setText(StringUtils.capitalize(category.name()));
        categoryImage.setImageResource(category.whiteImage);

        int timeSpent = repeatingQuest.getTotalTimeSpent();

        totalTimeSpent.setText(timeSpent > 0 ? DurationFormatter.formatShort(timeSpent, "") : "0");

        frequencyInterval.setText(FrequencyTextFormatter.formatInterval(repeatingQuest.getFrequency(), repeatingQuest.getRecurrence()));

        LocalDate nextScheduledDate = repeatingQuest.getNextScheduledDate(LocalDate.now());

        String nextScheduledDateText = DateFormatter.formatWithoutYear(
                nextScheduledDate,
                getString(R.string.unscheduled)
        );
        this.nextScheduledDate.setText(nextScheduledDateText);
        streakText.setText(String.valueOf(repeatingQuest.getStreak()));
    }

    private void showFrequencyProgress(Category category, PeriodHistory currentPeriodHistory) {
        LayoutInflater inflater = LayoutInflater.from(this);
        progressContainer.removeAllViews();

        int totalCount = currentPeriodHistory.getTotalCount();
        int completedCount = currentPeriodHistory.getCompletedCount();
        if (totalCount > 7) {
            TextView progressText = (TextView) inflater.inflate(R.layout.repeating_quest_progress_text, progressContainer, false);
            progressText.setText(getString(R.string.repeating_quest_completed_this_month, completedCount));
            progressContainer.addView(progressText);
            return;
        }

        long incomplete = currentPeriodHistory.getRemainingCount();

        int progressColor = R.color.colorAccent;

        if (category == Category.WORK || category == Category.FUN || category == Category.CHORES) {
            progressColor = R.color.colorAccentAlternative;
        }

        for (int i = 1; i <= completedCount; i++) {
            View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_indicator_empty, progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();

            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1.5f, getResources()), ContextCompat.getColor(this, progressColor));
            progressViewEmptyBackground.setColor(ContextCompat.getColor(this, progressColor));
            progressContainer.addView(progressViewEmpty);
        }

        for (int i = 1; i <= incomplete; i++) {
            View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_indicator_empty, progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();
            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1.5f, getResources()), Color.WHITE);
            progressViewEmptyBackground.setColor(Color.WHITE);
            progressContainer.addView(progressViewEmpty);
        }
    }

    private void setupChart(List<PeriodHistory> periodHistories) {
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
        leftAxis.setAxisMaxValue(repeatingQuest.getFrequency());
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

        List<String> xValues = getXValues(periodHistories);
        setHistoryData(periodHistories, xValues);
    }

    private void colorLayout(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
    }

    private List<String> getXValues(List<PeriodHistory> periodHistories) {
        List<String> xValues = new ArrayList<>();
        if (repeatingQuest.getRecurrence().getRecurrenceType() == Recurrence.RepeatType.MONTHLY) {
            xValues.add(getMonthText(periodHistories.get(0).getStartDate()));
            xValues.add(getMonthText(periodHistories.get(1).getStartDate()));
            xValues.add(getMonthText(periodHistories.get(2).getStartDate()));
            xValues.add(getString(R.string.this_month));
        } else {
            xValues.add(getWeekRangeText(periodHistories.get(0).getStartDate(), periodHistories.get(0).getEndDate()));
            xValues.add(getWeekRangeText(periodHistories.get(1).getStartDate(), periodHistories.get(1).getEndDate()));
            xValues.add(getString(R.string.last_week));
            xValues.add(getString(R.string.this_week));
        }
        return xValues;

    }

    private void setHistoryData(List<PeriodHistory> periodHistories, List<String> xValues) {
        BarData data = new BarData(xValues, createBarDataSet(periodHistories));
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

    @NonNull
    private BarDataSet createBarDataSet(List<PeriodHistory> periodHistories) {
        List<BarEntry> yValues = new ArrayList<>();
        for (int i = 0; i < periodHistories.size(); i++) {
            PeriodHistory p = periodHistories.get(i);
            yValues.add(new BarEntry(p.getCompletedCount(), i));
        }

        BarDataSet dataSet = new BarDataSet(yValues, "");
        dataSet.setColors(getColors());
        dataSet.setBarShadowColor(ContextCompat.getColor(this, repeatingQuest.getCategoryType().color100));
        return dataSet;
    }

    private String getMonthText(LocalDate date) {
        return getMonthShortName(date);
    }

    private String getWeekRangeText(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart.getMonth().equals(weekEnd.getMonth())) {
            return weekStart.getDayOfMonth() + " - " + weekEnd.getDayOfMonth() + " " + getMonthShortName(weekEnd);
        } else {
            return weekStart.getDayOfMonth() + " " + getMonthShortName(weekStart) + " - " + weekEnd.getDayOfMonth() + " " + getMonthShortName(weekEnd);
        }
    }

    private int[] getColors() {
        int[] colors = new int[Constants.DEFAULT_BAR_COUNT];
        Category category = repeatingQuest.getCategoryType();
        for (int i = 0; i < Constants.DEFAULT_BAR_COUNT; i++) {
            colors[i] = ContextCompat.getColor(this, category.color300);
        }
        return colors;
    }
}