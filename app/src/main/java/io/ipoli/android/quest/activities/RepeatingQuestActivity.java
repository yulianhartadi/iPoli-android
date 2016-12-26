package io.ipoli.android.quest.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
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

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.QuestData;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.ui.formatters.DateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.FrequencyTextFormatter;

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

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || StringUtils.isEmpty(getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY))) {
            finish();
            return;
        }

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
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
        String repeatingQuestId = getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY);
        repeatingQuestPersistenceService.findById(repeatingQuestId, repeatingQuest -> {
            if (repeatingQuest == null) {
                finish();
                return;
            }
            this.repeatingQuest = repeatingQuest;
            eventBus.post(new ScreenShownEvent(EventSource.REPEATING_QUEST));
            displayRepeatingQuest();
        });
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }


    private Pair<LocalDate, LocalDate> getCurrentInterval() {
        LocalDate today = LocalDate.now();
        if (repeatingQuest.getRecurrence().getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
            return new Pair<>(today.dayOfMonth().withMinimumValue(), today.dayOfMonth().withMaximumValue());
        } else {
            return new Pair<>(today.dayOfWeek().withMinimumValue(), today.dayOfWeek().withMaximumValue());
        }
    }

    private void displayRepeatingQuest() {
        name.setText(repeatingQuest.getName());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(repeatingQuest.getName());
        }

        Category category = RepeatingQuest.getCategory(repeatingQuest);
        Pair<LocalDate, LocalDate> interval = getCurrentInterval();
        questPersistenceService.countCompletedForRepeatingQuest(repeatingQuest.getId(), interval.first, interval.second, count -> {
            showFrequencyProgress(category, count);
            displaySummaryStats(category);
            colorLayout(category);
            setupChart();
        });
    }

    private void displaySummaryStats(Category category) {
        categoryName.setText(StringUtils.capitalize(category.name()));
        categoryImage.setImageResource(category.whiteImage);

        int timeSpent = 0;
        for (QuestData questData : repeatingQuest.getQuestsData().values()) {
            if (questData.isComplete()) {
                timeSpent += questData.getDuration();
            }
        }

        totalTimeSpent.setText(timeSpent > 0 ? DurationFormatter.formatShort((int) timeSpent, "") : "0");

        frequencyInterval.setText(FrequencyTextFormatter.formatInterval(getFrequency(), repeatingQuest.getRecurrence()));

        String nextScheduledDateText = DateFormatter.formatWithoutYear(
                repeatingQuest.getNextScheduledDate() == null ? null : new Date(repeatingQuest.getNextScheduledDate()),
                getString(R.string.unscheduled)
        );
        nextScheduledDate.setText(nextScheduledDateText);

        setCurrentStreak();
    }

    private void showFrequencyProgress(Category category, long completed) {
        LayoutInflater inflater = LayoutInflater.from(this);
        progressContainer.removeAllViews();

        int frequency = getFrequency();
        if (frequency > 7) {
            TextView progressText = (TextView) inflater.inflate(R.layout.repeating_quest_progress_text, progressContainer, false);
            progressText.setText(completed + " completed this month");
            progressContainer.addView(progressText);
            return;
        }

        long incomplete = frequency - completed;

        int progressColor = R.color.colorAccent;

        if (category == Category.WORK || category == Category.FUN || category == Category.CHORES) {
            progressColor = R.color.colorAccentAlternative;
        }

        for (int i = 1; i <= completed; i++) {
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
        leftAxis.setAxisMaxValue(getFrequency());
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

        setHistoryData();
    }

    private void colorLayout(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
    }

    private void setHistoryData() {
        if (repeatingQuest.getRecurrence().getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
            setMonthlyHistoryData();
        } else {
            setWeeklyHistoryData();
        }
    }

    private void setMonthlyHistoryData() {
        List<BarEntry> yValues = new ArrayList<>();
        List<Pair<LocalDate, LocalDate>> monthPairs = getBoundsFor4MonthsInThePast(LocalDate.now());
        for (int i = 0; i < Constants.DEFAULT_BAR_COUNT; i++) {
            Pair<LocalDate, LocalDate> monthPair = monthPairs.get(i);
            int finalI = i;
            questPersistenceService.countCompletedForRepeatingQuest(repeatingQuest.getId(), monthPair.first, monthPair.second, count -> {
                yValues.add(new BarEntry(count, finalI));
                if (yValues.size() == Constants.DEFAULT_BAR_COUNT) {
                    BarDataSet dataSet = new BarDataSet(yValues, "");
                    dataSet.setColors(getColors());
                    dataSet.setBarShadowColor(ContextCompat.getColor(this, RepeatingQuest.getCategory(repeatingQuest).color100));

                    List<String> xValues = new ArrayList<>();
                    xValues.add(getMonthText(monthPairs.get(0).first));
                    xValues.add(getMonthText(monthPairs.get(1).first));
                    xValues.add(getMonthText(monthPairs.get(2).first));
                    xValues.add("this month");
                    setHistoryData(dataSet, xValues);
                }
            });
        }
    }

    private String getMonthText(LocalDate date) {
        return date.monthOfYear().getAsShortText();
    }

    private void setWeeklyHistoryData() {
        List<BarEntry> yValues = new ArrayList<>();
        List<Pair<LocalDate, LocalDate>> weekPairs = getBoundsFor4WeeksInThePast(LocalDate.now());
        for (int i = 0; i < Constants.DEFAULT_BAR_COUNT; i++) {
            Pair<LocalDate, LocalDate> weekPair = weekPairs.get(i);
            int finalI = i;
            questPersistenceService.countCompletedForRepeatingQuest(repeatingQuest.getId(), weekPair.first, weekPair.second, count -> {
                yValues.add(new BarEntry(count, finalI));
                if (yValues.size() == Constants.DEFAULT_BAR_COUNT) {
                    BarDataSet dataSet = new BarDataSet(yValues, "");
                    dataSet.setColors(getColors());
                    dataSet.setBarShadowColor(ContextCompat.getColor(this, RepeatingQuest.getCategory(repeatingQuest).color100));

                    List<String> xValues = new ArrayList<>();
                    xValues.add(getWeekRangeText(weekPairs.get(0).first, weekPairs.get(0).second));
                    xValues.add(getWeekRangeText(weekPairs.get(1).first, weekPairs.get(1).second));
                    xValues.add("last week");
                    xValues.add("this week");
                    setHistoryData(dataSet, xValues);
                }
            });
        }

    }

    @NonNull
    private List<Pair<LocalDate, LocalDate>> getBoundsFor4MonthsInThePast(LocalDate currentDate) {
        LocalDate monthStart = currentDate.minusMonths(3).dayOfMonth().withMinimumValue();
        LocalDate monthEnd = monthStart.dayOfMonth().withMaximumValue();

        List<Pair<LocalDate, LocalDate>> monthBounds = new ArrayList<>();
        monthBounds.add(new Pair<>(monthStart, monthEnd));
        for (int i = 0; i < 3; i++) {
            monthStart = monthStart.plusMonths(1);
            monthEnd = monthStart.dayOfMonth().withMaximumValue();
            monthBounds.add(new Pair<>(monthStart, monthEnd));
        }
        return monthBounds;
    }

    @NonNull
    private List<Pair<LocalDate, LocalDate>> getBoundsFor4WeeksInThePast(LocalDate currentDate) {
        LocalDate weekStart = currentDate.minusWeeks(3).dayOfWeek().withMinimumValue();
        LocalDate weekEnd = weekStart.dayOfWeek().withMaximumValue();

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(weekStart, weekEnd));
        for (int i = 0; i < 3; i++) {
            weekStart = weekStart.plusWeeks(1);
            weekEnd = weekStart.dayOfWeek().withMaximumValue();
            weekBounds.add(new Pair<>(weekStart, weekEnd));
        }
        return weekBounds;
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

    private String getWeekRangeText(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart.getMonthOfYear() == weekEnd.getMonthOfYear()) {
            return weekStart.getDayOfMonth() + " - " + weekEnd.getDayOfMonth() + " " + weekEnd.monthOfYear().getAsShortText();
        } else {
            return weekStart.getDayOfMonth() + " " + weekStart.monthOfYear().getAsShortText() + " - " + weekEnd.getDayOfMonth() + " " + weekEnd.monthOfYear().getAsShortText();
        }
    }

    private int[] getColors() {
        int[] colors = new int[Constants.DEFAULT_BAR_COUNT];
        Category category = RepeatingQuest.getCategory(repeatingQuest);
        for (int i = 0; i < Constants.DEFAULT_BAR_COUNT; i++) {
            colors[i] = ContextCompat.getColor(this, category.color300);
        }
        return colors;
    }

    private int getFrequency() {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        if (recurrence.isFlexible()) {
            return recurrence.getFlexibleCount();
        }
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.DAILY) {
            return 7;
        }
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
            return 1;
        }
        try {
            Recur recur = new Recur(recurrence.getRrule());
            return recur.getDayList().size();
        } catch (ParseException e) {
            return 0;
        }
    }

    private void setCurrentStreak() {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
            getMonthlyStreak(streak -> streakText.setText(String.valueOf(streak)));
        } else {
            getWeeklyStreak(streak -> streakText.setText(String.valueOf(streak)));
        }

    }

    private void getMonthlyStreak(StreakListener streakListener) {
        LocalDate monthStart = LocalDate.now().dayOfMonth().withMinimumValue();
        LocalDate monthEnd = monthStart.dayOfMonth().withMaximumValue();
        countMonthlyStreak(monthStart, monthEnd, 0, streakListener);
    }

    private void getWeeklyStreak(StreakListener streakListener) {
        LocalDate weekStart = LocalDate.now().dayOfWeek().withMinimumValue();
        LocalDate weekEnd = weekStart.dayOfWeek().withMaximumValue();
        countWeeklyStreak(weekStart, weekEnd, 0, streakListener);
    }

    private void countMonthlyStreak(LocalDate start, LocalDate end, long streak, StreakListener listener) {
        questPersistenceService.countCompletedForRepeatingQuest(repeatingQuest.getId(), start, end, count -> {
            if (count < getFrequency()) {
                long streakResult = streak == 0 ? streak + count : streak;
                listener.onStreakCounted(streakResult);
            } else {
                LocalDate monthStart = start.minusMonths(1);
                LocalDate monthEnd = monthStart.dayOfMonth().withMaximumValue();
                countMonthlyStreak(monthStart, monthEnd, streak + count, listener);
            }
        });
    }

    private void countWeeklyStreak(LocalDate start, LocalDate end, long streak, StreakListener listener) {
        questPersistenceService.countCompletedForRepeatingQuest(repeatingQuest.getId(), start, end, count -> {
            if (count < getFrequency()) {
                long streakResult = streak == 0 ? streak + count : streak;
                listener.onStreakCounted(streakResult);
            } else {
                LocalDate weekStart = start.minusWeeks(1);
                LocalDate weekEnd = weekStart.dayOfWeek().withMaximumValue();
                countWeeklyStreak(weekStart, weekEnd, streak + count, listener);
            }
        });
    }

    interface StreakListener {
        void onStreakCounted(long streak);
    }
}