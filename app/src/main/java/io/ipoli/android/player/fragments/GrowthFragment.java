package io.ipoli.android.player.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.squareup.otto.Bus;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.scheduling.PriorityEstimator;
import io.ipoli.android.app.ui.charts.ChartMarkerView;
import io.ipoli.android.app.ui.charts.XAxisValueFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.events.GrowthCategoryFilteredEvent;
import io.ipoli.android.player.events.GrowthIntervalSelectedEvent;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/16.
 */
public class GrowthFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {

    public static final int CHART_ANIMATION_DURATION = 500;
    public static final Easing.EasingOption DEFAULT_EASING_OPTION = Easing.EasingOption.EaseInQuad;

    public static final int THIS_WEEK = 0;
    public static final int THIS_MONTH = 1;
    public static final int LAST_7_DAYS = 2;
    public static final int LAST_4_WEEKS = 3;
    public static final int LAST_3_MONTHS = 4;

    public static final String X_AXIS_DAY_FORMAT = "d MMM";
    public static final String X_AXIS_MONTH = "MMM";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_spinner)
    Spinner spinner;

    @BindView(R.id.summary_done)
    TextView summaryDone;

    @BindView(R.id.summary_overdue)
    TextView summaryOverdue;

    @BindView(R.id.summary_time_tracked)
    TextView summaryTimeTracked;

    @BindView(R.id.awesomeness_range_chart)
    LineChart awesomenessLineChart;

    @BindView(R.id.awesomeness_vs_last_chart)
    BarChart awesomenessBarChart;

    @BindView(R.id.completed_quests_range_chart)
    LineChart completedQuestsLineChart;

    @BindView(R.id.completed_quests_vs_last_chart)
    BarChart completedQuestsBarChart;

    @BindView(R.id.time_spent_range_chart)
    LineChart timeSpentLineChart;

    @BindView(R.id.time_spent_vs_last_chart)
    BarChart timeSpentBarChart;

    @BindView(R.id.coins_earned_range_chart)
    LineChart coinsEarnedLineChart;

    @BindView(R.id.coins_earned_vs_last_chart)
    BarChart coinsEarnedBarChart;

    @BindView(R.id.xp_earned_range_chart)
    LineChart xpEarnedLineChart;

    @BindView(R.id.xp_earned_vs_last_chart)
    BarChart xpEarnedBarChart;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    private Unbinder unbinder;
    private List<Chart<?>> lineCharts;
    private List<Chart<?>> barCharts;
    private PriorityEstimator priorityEstimator;

    private Set<Category> selectedCompleted = new HashSet<>();
    private Set<Category> selectedTimeSpent = new HashSet<>();
    private Map<Category, Integer> categoryToColor = new HashMap<>();

    private IValueFormatter barDataValueFormatter = (v, entry, i, viewPortHandler) -> String.valueOf((int) v);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_growth, container, false);
        unbinder = ButterKnife.bind(this, view);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        priorityEstimator = new PriorityEstimator();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(actionBar.getThemedContext(),
                    R.layout.growth_spinner_item,
                    R.id.growth_interval,
                    getResources().getStringArray(R.array.growth_intervals));
            adapter.setDropDownViewResource(R.layout.growth_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.md_white), PorterDuff.Mode.SRC_ATOP);
            spinner.setSelection(0, false);
            spinner.setOnItemSelectedListener(this);
            ((MainActivity) getActivity()).actionBarDrawerToggle.syncState();
        }

        lineCharts = new ArrayList<>();
        lineCharts.add(awesomenessLineChart);
        lineCharts.add(completedQuestsLineChart);
        lineCharts.add(timeSpentLineChart);
        lineCharts.add(coinsEarnedLineChart);
        lineCharts.add(xpEarnedLineChart);

        barCharts = new ArrayList<>();
        barCharts.add(awesomenessBarChart);
        barCharts.add(completedQuestsBarChart);
        barCharts.add(timeSpentBarChart);
        barCharts.add(coinsEarnedBarChart);
        barCharts.add(xpEarnedBarChart);

        selectedCompleted.add(Category.WELLNESS);
        selectedCompleted.add(Category.LEARNING);
        selectedCompleted.add(Category.WORK);

        selectedTimeSpent.addAll(selectedCompleted);

        categoryToColor.put(Category.WELLNESS, R.color.md_green_500);
        categoryToColor.put(Category.LEARNING, R.color.md_blue_A200);
        categoryToColor.put(Category.PERSONAL, R.color.md_orange_A200);
        categoryToColor.put(Category.WORK, R.color.md_red_A200);
        categoryToColor.put(Category.FUN, R.color.md_purple_300);
        categoryToColor.put(Category.CHORES, R.color.md_brown_300);

        setupAwesomenessLineChart();
        setupAwesomenessBarChart();
        setupCompletedQuestsLineChart();
        setupCompletedQuestsBarChart();
        setupTimeSpentLineChart();
        setupTimeSpentBarChart();
        setupCoinsEarnedLineChart();
        setupCoinsEarnedBarChart();
        setupXpEarnedLineChart();
        setupXpEarnedBarChart();

        showCharts(THIS_WEEK);
        eventBus.post(new ScreenShownEvent(EventSource.GROWTH));
        return view;
    }

    private void setupAwesomenessLineChart() {
        applyDefaultStyle(awesomenessLineChart);
        ChartMarkerView chartMarkerView = new ChartMarkerView(getContext());
        awesomenessLineChart.setMarker(chartMarkerView);
    }

    private void setupAwesomenessBarChart() {
        applyDefaultStyle(awesomenessBarChart);
    }

    private void setupCompletedQuestsLineChart() {
        applyDefaultStyle(completedQuestsLineChart);
        ChartMarkerView chartMarkerView = new ChartMarkerView(getContext());
        completedQuestsLineChart.setMarker(chartMarkerView);
        completedQuestsLineChart.setNoDataText(getString(R.string.growth_no_categories_selected));
        completedQuestsLineChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
    }

    private void setupCompletedQuestsBarChart() {
        applyDefaultStyle(completedQuestsBarChart, true);
        completedQuestsBarChart.setNoDataText(getString(R.string.growth_no_categories_selected));
        completedQuestsBarChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
    }

    private void setupTimeSpentLineChart() {
        applyDefaultStyle(timeSpentLineChart);
        ChartMarkerView chartMarkerView = new ChartMarkerView(getContext());
        timeSpentLineChart.setMarker(chartMarkerView);
        timeSpentLineChart.setNoDataText(getString(R.string.growth_no_categories_selected));
        timeSpentLineChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
    }

    private void setupTimeSpentBarChart() {
        applyDefaultStyle(timeSpentBarChart, true);
        timeSpentBarChart.setNoDataText(getString(R.string.growth_no_categories_selected));
        timeSpentBarChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
    }

    private void setupCoinsEarnedLineChart() {
        applyDefaultStyle(coinsEarnedLineChart);
        ChartMarkerView chartMarkerView = new ChartMarkerView(getContext());
        coinsEarnedLineChart.setMarker(chartMarkerView);
    }

    private void setupCoinsEarnedBarChart() {
        applyDefaultStyle(coinsEarnedBarChart);
    }

    private void setupXpEarnedLineChart() {
        applyDefaultStyle(xpEarnedLineChart);
        ChartMarkerView chartMarkerView = new ChartMarkerView(getContext());
        xpEarnedLineChart.setMarker(chartMarkerView);
    }

    private void setupXpEarnedBarChart() {
        applyDefaultStyle(xpEarnedBarChart);
    }

    private void showLineCharts() {
        for (Chart<?> chart : lineCharts) {
            chart.setVisibility(View.VISIBLE);
        }
        for (Chart<?> chart : barCharts) {
            chart.setVisibility(View.GONE);
        }
    }

    private void showBarCharts() {
        for (Chart<?> chart : barCharts) {
            chart.setVisibility(View.VISIBLE);
        }
        for (Chart<?> chart : lineCharts) {
            chart.setVisibility(View.GONE);
        }
    }

    private void showCharts(int position) {
        LocalDate today = LocalDate.now();
        switch (position) {
            case THIS_WEEK:
                showLineCharts();
                showThisWeekCharts(today);
                break;
            case THIS_MONTH:
                showLineCharts();
                showThisMonthCharts(today);
                break;
            case LAST_7_DAYS:
                showBarCharts();
                showLast7DaysCharts(today);
                break;
            case LAST_4_WEEKS:
                showBarCharts();
                showLast4WeeksCharts(today);
                break;
            case LAST_3_MONTHS:
                showBarCharts();
                showLast3MonthsCharts(today);
                break;
        }
    }

    private void showThisWeekCharts(final LocalDate today) {
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate startOfPrevWeek = today.minusWeeks(1).with(DayOfWeek.MONDAY);

        questPersistenceService.findAllScheduledBetween(startOfPrevWeek, today, quests -> {
            int[] awesomenessData = new int[7 + today.getDayOfWeek().getValue()];
            int[][] completedData = new int[Category.values().length][7];
            int[][] timeSpentData = new int[Category.values().length][7];
            int[] xpData = new int[7 + today.getDayOfWeek().getValue()];
            int[] coinsData = new int[7 + today.getDayOfWeek().getValue()];
            int[] completed = new int[2];
            int[] overdue = new int[2];
            int[] minutesTracked = new int[2];
            for (Quest q : quests) {
                if (q.isCompleted()) {
                    LocalDate completedAtDate = q.getCompletedAtDate();
                    int startOfPrevWeekIdx = (int) DAYS.between(startOfPrevWeek, completedAtDate);
                    awesomenessData[startOfPrevWeekIdx] += getAwesomenessForQuest(q);
                    coinsData[startOfPrevWeekIdx] += q.getCoins();
                    xpData[startOfPrevWeekIdx] += q.getExperience();
                    if (completedAtDate.isBefore(startOfWeek)) {
                        completed[0]++;
                        if (q.getActualStart() != null) {
                            minutesTracked[0] += q.getActualDuration();
                        }
                    } else {
                        completed[1]++;
                        if (q.getActualStart() != null) {
                            minutesTracked[1] += q.getActualDuration();
                        }
                        int thisWeekIdx = (int) DAYS.between(startOfWeek, completedAtDate);
                        completedData[q.getCategoryType().ordinal()][thisWeekIdx]++;
                        timeSpentData[q.getCategoryType().ordinal()][thisWeekIdx] += q.getActualDuration();
                    }
                }

                boolean isCompletedAfterEndDate = q.isCompleted() && q.getEndDate().isBefore(q.getCompletedAtDate());
                boolean isOverdue = !q.isCompleted() && q.getEndDate().isBefore(today);
                if (isCompletedAfterEndDate || isOverdue) {
                    if (q.getEndDate().isBefore(startOfWeek)) {
                        overdue[0]++;
                    } else {
                        overdue[1]++;
                    }
                }
            }
            showSummary(completed, overdue, minutesTracked);
            String[] xLabels = new String[7];
            for (int i = 0; i < 7; i++) {
                xLabels[i] = startOfWeek.plusDays(i).format(DateTimeFormatter.ofPattern(X_AXIS_DAY_FORMAT));
            }

            boolean drawHandles = true;
            showAwesomenessLineChart(awesomenessData, 7, StringUtils.capitalize(getString(R.string.this_week)), StringUtils.capitalize(getString(R.string.last_week)), xLabels, drawHandles);
            showCompletedQuestsLineChart(completedData, xLabels, drawHandles);
            showTimeSpentLineChart(timeSpentData, xLabels, drawHandles);
            showCoinsEarnedLineChart(coinsData, 7, StringUtils.capitalize(getString(R.string.this_week)), StringUtils.capitalize(getString(R.string.last_week)), xLabels, drawHandles);
            showXpEarnedLineChart(xpData, 7, StringUtils.capitalize(getString(R.string.this_week)), StringUtils.capitalize(getString(R.string.last_week)), xLabels, drawHandles);
        });
    }

    private void showThisMonthCharts(LocalDate today) {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfPrevMonth = today.minusMonths(1).withDayOfMonth(1);
        int daysInPrevMonth = startOfPrevMonth.lengthOfMonth();
        int daysInCurrentMonth = startOfMonth.lengthOfMonth();

        questPersistenceService.findAllScheduledBetween(startOfPrevMonth, today, quests -> {
            int[] awesomenessData = new int[daysInPrevMonth + today.getDayOfMonth()];
            int[][] completedData = new int[Category.values().length][daysInCurrentMonth];
            int[][] timeSpentData = new int[Category.values().length][daysInCurrentMonth];
            int[] xpData = new int[daysInPrevMonth + today.getDayOfMonth()];
            int[] coinsData = new int[daysInPrevMonth + today.getDayOfMonth()];
            int[] completed = new int[2];
            int[] overdue = new int[2];
            int[] minutesTracked = new int[2];
            for (Quest q : quests) {
                if (q.isCompleted()) {
                    LocalDate completedAtDate = q.getCompletedAtDate();
                    int prevMonthIdx = (int) DAYS.between(startOfPrevMonth, completedAtDate);
                    awesomenessData[prevMonthIdx] += getAwesomenessForQuest(q);
                    coinsData[prevMonthIdx] += q.getCoins();
                    xpData[prevMonthIdx] += q.getExperience();
                    if (completedAtDate.isBefore(startOfMonth)) {
                        completed[0]++;
                        if (q.getActualStart() != null) {
                            minutesTracked[0] += q.getActualDuration();
                        }
                    } else {
                        completed[1]++;
                        if (q.getActualStart() != null) {
                            minutesTracked[1] += q.getActualDuration();
                        }
                        int thisMonthIdx = (int) DAYS.between(startOfMonth, completedAtDate);
                        completedData[q.getCategoryType().ordinal()][thisMonthIdx]++;
                        timeSpentData[q.getCategoryType().ordinal()][thisMonthIdx] += q.getActualDuration();
                    }
                }
                boolean isCompletedAfterEndDate = q.isCompleted() && q.getEndDate().isBefore(q.getCompletedAtDate());
                boolean isOverdue = !q.isCompleted() && q.getEndDate().isBefore(today);
                if (isCompletedAfterEndDate || isOverdue) {
                    if (q.getEndDate().isBefore(startOfMonth)) {
                        overdue[0]++;
                    } else {
                        overdue[1]++;
                    }
                }
            }
            String[] xLabels = new String[daysInCurrentMonth];
            for (int i = 0; i < daysInCurrentMonth; i++) {
                xLabels[i] = startOfMonth.plusDays(i).format(DateTimeFormatter.ofPattern(X_AXIS_DAY_FORMAT));
            }

            boolean drawHandles = false;
            showSummary(completed, overdue, minutesTracked);
            showAwesomenessLineChart(awesomenessData, daysInPrevMonth, StringUtils.capitalize(getString(R.string.this_month)), getString(R.string.last_month), xLabels, drawHandles);
            showCompletedQuestsLineChart(completedData, xLabels, drawHandles);
            showTimeSpentLineChart(timeSpentData, xLabels, drawHandles);
            showCoinsEarnedLineChart(coinsData, daysInPrevMonth, StringUtils.capitalize(getString(R.string.this_month)), getString(R.string.last_month), xLabels, drawHandles);
            showXpEarnedLineChart(xpData, daysInPrevMonth, StringUtils.capitalize(getString(R.string.this_month)), getString(R.string.last_month), xLabels, drawHandles);
        });
    }

    private void showLast3MonthsCharts(LocalDate today) {
        LocalDate startDay = today.minusMonths(2).withDayOfMonth(1);
        List<Pair<Long, Long>> monthRanges = new ArrayList<>();
        final int periodLength = 3;
        for (int i = 0; i < periodLength; i++) {
            LocalDate curStart = startDay.plusMonths(i);
            monthRanges.add(new Pair<>(DateUtils.toMillis(curStart), DateUtils.toMillis(curStart.with(TemporalAdjusters.lastDayOfMonth()))));
        }
        questPersistenceService.findAllScheduledBetween(startDay, today, quests -> {
            int[] awesomenessData = new int[periodLength];
            int[][] completedData = new int[Category.values().length][periodLength];
            int[][] timeSpentData = new int[Category.values().length][periodLength];
            int[] xpData = new int[periodLength];
            int[] coinsData = new int[periodLength];
            int completed = 0;
            int overdue = 0;
            int minutesTracked = 0;
            for (Quest q : quests) {
                if (q.isCompleted()) {
                    Long completedAt = q.getCompletedAt();

                    int idx = -1;
                    for (int i = 0; i < periodLength; i++) {
                        Pair<Long, Long> range = monthRanges.get(i);
                        if (completedAt >= range.first && completedAt <= range.second) {
                            idx = i;
                        }
                    }
                    awesomenessData[idx] += getAwesomenessForQuest(q);
                    coinsData[idx] += q.getCoins();
                    xpData[idx] += q.getExperience();
                    completedData[q.getCategoryType().ordinal()][idx]++;
                    timeSpentData[q.getCategoryType().ordinal()][idx] += q.getActualDuration();
                    completed++;
                    if (q.getActualStart() != null) {
                        minutesTracked += q.getActualDuration();
                    }
                }
                if (isOverdue(q, today)) {
                    overdue++;
                }
            }

            showSummary(completed, overdue, minutesTracked);
            String[] xLabels = new String[periodLength];
            for (int i = 0; i < periodLength; i++) {
                xLabels[i] = startDay.plusMonths(i).format(DateTimeFormatter.ofPattern(X_AXIS_MONTH));
            }

            showAwesomenessBarChart(awesomenessData, xLabels);
            showCompletedQuestsBarChart(completedData, xLabels);
            showTimeSpentBarChart(timeSpentData, xLabels);
            showCoinsEarnedBarChart(coinsData, xLabels);
            showXpEarnedBarChart(xpData, xLabels);
        });
    }

    private void showLast4WeeksCharts(LocalDate today) {
        LocalDate startDay = today.minusWeeks(3).with(DayOfWeek.MONDAY);
        List<Pair<Long, Long>> weekRanges = new ArrayList<>();
        final int periodLength = 4;
        for (int i = 0; i < periodLength; i++) {
            LocalDate curStart = startDay.plusWeeks(i);
            weekRanges.add(new Pair<>(DateUtils.toMillis(curStart), DateUtils.toMillis(curStart.with(DayOfWeek.SUNDAY))));
        }
        questPersistenceService.findAllScheduledBetween(startDay, today, quests -> {
            int[] awesomenessData = new int[periodLength];
            int[][] completedData = new int[Category.values().length][periodLength];
            int[][] timeSpentData = new int[Category.values().length][periodLength];
            int[] xpData = new int[periodLength];
            int[] coinsData = new int[periodLength];
            int completed = 0;
            int overdue = 0;
            int minutesTracked = 0;
            for (Quest q : quests) {
                if (q.isCompleted()) {
                    Long completedAt = q.getCompletedAt();

                    int idx = -1;
                    for (int i = 0; i < periodLength; i++) {
                        Pair<Long, Long> range = weekRanges.get(i);
                        if (completedAt >= range.first && completedAt <= range.second) {
                            idx = i;
                        }
                    }
                    awesomenessData[idx] += getAwesomenessForQuest(q);
                    coinsData[idx] += q.getCoins();
                    xpData[idx] += q.getExperience();
                    completedData[q.getCategoryType().ordinal()][idx]++;
                    timeSpentData[q.getCategoryType().ordinal()][idx] += q.getActualDuration();
                    completed++;
                    if (q.getActualStart() != null) {
                        minutesTracked += q.getActualDuration();
                    }
                }
                if (isOverdue(q, today)) {
                    overdue++;
                }
            }

            showSummary(completed, overdue, minutesTracked);
            String[] xLabels = new String[periodLength];
            for (int i = 0; i < periodLength; i++) {
                LocalDate weekStart = startDay.plusWeeks(i);
                LocalDate weekEnd = weekStart.plusDays(6);
                String weekStartText = weekStart.format(DateTimeFormatter.ofPattern(X_AXIS_DAY_FORMAT));
                String weekEndText = weekEnd.format(DateTimeFormatter.ofPattern(X_AXIS_DAY_FORMAT));
                String label = weekStartText + " - " + weekEndText;
                xLabels[i] = label;
            }

            showAwesomenessBarChart(awesomenessData, xLabels);
            showCompletedQuestsBarChart(completedData, xLabels);
            showTimeSpentBarChart(timeSpentData, xLabels);
            showCoinsEarnedBarChart(coinsData, xLabels);
            showXpEarnedBarChart(xpData, xLabels);
        });
    }

    private void showLast7DaysCharts(LocalDate today) {
        LocalDate startDay = today.minusDays(6);

        questPersistenceService.findAllScheduledBetween(startDay, today, quests -> {
            int[] awesomenessData = new int[7];
            int[][] completedData = new int[Category.values().length][7];
            int[][] timeSpentData = new int[Category.values().length][7];
            int[] xpData = new int[7];
            int[] coinsData = new int[7];
            int completed = 0;
            int overdue = 0;
            int minutesTracked = 0;
            for (Quest q : quests) {
                if (q.isCompleted()) {
                    int idx = (int) DAYS.between(startDay, q.getCompletedAtDate());
                    awesomenessData[idx] += getAwesomenessForQuest(q);
                    coinsData[idx] += q.getCoins();
                    xpData[idx] += q.getExperience();
                    completedData[q.getCategoryType().ordinal()][idx]++;
                    timeSpentData[q.getCategoryType().ordinal()][idx] += q.getActualDuration();
                    completed++;
                    if (q.getActualStart() != null) {
                        minutesTracked += q.getActualDuration();
                    }
                }
                if (isOverdue(q, today)) {
                    overdue++;
                }
            }

            showSummary(completed, overdue, minutesTracked);

            String[] xLabels = new String[7];
            for (int i = 0; i < 7; i++) {
                xLabels[i] = startDay.plusDays(i).format(DateTimeFormatter.ofPattern(X_AXIS_DAY_FORMAT));
            }

            showAwesomenessBarChart(awesomenessData, xLabels);
            showCompletedQuestsBarChart(completedData, xLabels);
            showTimeSpentBarChart(timeSpentData, xLabels);
            showCoinsEarnedBarChart(coinsData, xLabels);
            showXpEarnedBarChart(xpData, xLabels);
        });
    }

    private boolean isOverdue(Quest quest, LocalDate today) {
        boolean isCompletedAfterEndDate = quest.isCompleted() && quest.getEndDate().isBefore(quest.getCompletedAtDate());
        if (isCompletedAfterEndDate) {
            return true;
        }
        return !quest.isCompleted() && quest.getEndDate().isBefore(today);
    }

    private void showSummary(int completed, int overdue, int minutesTracked) {
        summaryDone.setText(completed + "\n" + getString(R.string.done));
        summaryOverdue.setText(overdue + "\n" + getString(R.string.overdue));
        if (minutesTracked < 60) {
            summaryTimeTracked.setText(minutesTracked + " min\n" + getString(R.string.tracked));
        } else {
            summaryTimeTracked.setText(minutesTracked / 60 + "h\n" + getString(R.string.tracked));
        }
    }

    private void showSummary(int done[], int[] overdue, int[] minutesTracked) {
        int lastPeriodIdx = 0;
        int thisPeriodIdx = 1;

        showDoneSummary(done[thisPeriodIdx], calculateChange(done[thisPeriodIdx], done[lastPeriodIdx]));

        showOverdueSummary(overdue[thisPeriodIdx], calculateChange(overdue[thisPeriodIdx], overdue[lastPeriodIdx]));

        showTimeTrackedSummary(minutesTracked[thisPeriodIdx], calculateChange(minutesTracked[thisPeriodIdx], minutesTracked[lastPeriodIdx]));
    }

    private int calculateChange(int thisPeriod, int lastPeriod) {
        if (thisPeriod == 0 && lastPeriod == 0) {
            return 0;
        }
        if (thisPeriod == 0) {
            return -(lastPeriod * 100);
        }
        if (lastPeriod == 0) {
            return thisPeriod * 100;
        }
        float diff = thisPeriod - lastPeriod;
        return (int) (diff / (float) lastPeriod) * 100;
    }

    private void showDoneSummary(int completedCount, int doneChange) {
        String completedText = completedCount + " ";
        int spanStart = completedText.length();
        completedText += doneChange >= 0 ? "+" : "-";
        completedText += Math.abs(doneChange) + "%";
        int spanEnd = completedText.length();
        completedText += "\n" + getString(R.string.done);
        SpannableString finalText = new SpannableString(completedText);
        finalText.setSpan(new ForegroundColorSpan(doneChange >= 0 ? ContextCompat.getColor(getContext(), R.color.md_light_green_300) : ContextCompat.getColor(getContext(), R.color.md_red_A400)), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        finalText.setSpan(new StyleSpan(Typeface.BOLD), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        finalText.setSpan(new RelativeSizeSpan(0.8f), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        summaryDone.setText(finalText);
    }

    private void showOverdueSummary(int overdueCount, int overdueChange) {
        String overdueText = overdueCount + " ";
        int spanStart = overdueText.length();
        overdueText += overdueChange >= 0 ? "+" : "-";
        overdueText += Math.abs(overdueChange) + "%";
        int spanEnd = overdueText.length();
        overdueText += "\n" + getString(R.string.overdue);
        SpannableString finalText = new SpannableString(overdueText);
        finalText.setSpan(new ForegroundColorSpan(overdueChange > 0 ? ContextCompat.getColor(getContext(), R.color.md_red_A400) : ContextCompat.getColor(getContext(), R.color.md_light_green_300)), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        finalText.setSpan(new StyleSpan(Typeface.BOLD), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        finalText.setSpan(new RelativeSizeSpan(0.8f), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        summaryOverdue.setText(finalText);
    }

    private void showTimeTrackedSummary(int minutesTracked, int minutesTrackedChange) {
        String minutesTrackedText = "";
        if (minutesTracked < 60) {
            minutesTrackedText += minutesTracked + " min ";
        } else {
            minutesTrackedText += minutesTracked / 60 + "h ";
        }
        int spanStart = minutesTrackedText.length();
        minutesTrackedText += minutesTrackedChange >= 0 ? "+" : "-";
        minutesTrackedText += Math.abs(minutesTrackedChange) + "%";
        int spanEnd = minutesTrackedText.length();
        minutesTrackedText += "\n" + getString(R.string.tracked);
        SpannableString finalText = new SpannableString(minutesTrackedText);
        finalText.setSpan(new ForegroundColorSpan(minutesTrackedChange >= 0 ? ContextCompat.getColor(getContext(), R.color.md_light_green_300) : ContextCompat.getColor(getContext(), R.color.md_red_A400)), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        finalText.setSpan(new StyleSpan(Typeface.BOLD), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        finalText.setSpan(new RelativeSizeSpan(0.8f), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        summaryTimeTracked.setText(finalText);
    }

    private int getAwesomenessForQuest(Quest quest) {
        return Math.max(0, priorityEstimator.estimate(quest));
    }

    private void showAwesomenessBarChart(int[] awesomenessPerDay, String[] xLabels) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < awesomenessPerDay.length; i++) {
            entries.add(new BarEntry(i, awesomenessPerDay[i]));
        }
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_orange_500));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(barDataValueFormatter);
        barData.setValueTextSize(14f);
        awesomenessBarChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));
        awesomenessBarChart.getXAxis().setLabelCount(xLabels.length);
        awesomenessBarChart.setData(barData);
        invalidateAndAnimate(awesomenessBarChart);
    }

    private void showXpEarnedBarChart(int[] data, String[] xLabels) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            entries.add(new BarEntry(i, data[i]));
        }
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_orange_500));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(barDataValueFormatter);
        barData.setValueTextSize(14f);
        xpEarnedBarChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));
        xpEarnedBarChart.getXAxis().setLabelCount(xLabels.length);
        xpEarnedBarChart.setData(barData);
        invalidateAndAnimate(xpEarnedBarChart);
    }

    private void showCoinsEarnedBarChart(int[] data, String[] xLabels) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            entries.add(new BarEntry(i, data[i]));
        }
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_orange_500));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(barDataValueFormatter);
        barData.setValueTextSize(14f);
        coinsEarnedBarChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));
        coinsEarnedBarChart.getXAxis().setLabelCount(xLabels.length);
        coinsEarnedBarChart.setData(barData);
        invalidateAndAnimate(coinsEarnedBarChart);
    }

    private void showTimeSpentBarChart(int[][] data, String[] xLabels) {
        timeSpentBarChart.setTag(data);
        if (selectedTimeSpent.isEmpty()) {
            timeSpentBarChart.setData(null);
        } else {
            BarData barData = createCategoryBarData(data, selectedTimeSpent);
            timeSpentBarChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));
            timeSpentBarChart.getXAxis().setLabelCount(xLabels.length);
            timeSpentBarChart.setData(barData);
        }
        invalidateAndAnimate(timeSpentBarChart);
    }

    @NonNull
    private BarData createCategoryBarData(int[][] data, Set<Category> selectedCategories) {
        List<Category> availableCategories = new ArrayList<>();
        for (Category category : Category.values()) {
            if (selectedCategories.contains(category)) {
                availableCategories.add(category);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data[0].length; i++) {
            float[] vals = new float[availableCategories.size()];
            for (int j = 0; j < vals.length; j++) {
                Category category = availableCategories.get(j);
                vals[j] = data[category.ordinal()][i];
            }
            entries.add(new BarEntry(i, vals));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        List<String> stackLabels = new ArrayList<>();
        for (Category category : availableCategories) {
            colors.add(ContextCompat.getColor(getContext(), categoryToColor.get(category)));
            stackLabels.add(getString(Category.getNameRes(category)));
        }
        dataSet.setColors(colors);
        if (stackLabels.size() > 1) {
            dataSet.setStackLabels(stackLabels.toArray(new String[stackLabels.size()]));
        } else {
            dataSet.setLabel(stackLabels.get(0));
        }
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(barDataValueFormatter);
        barData.setValueTextSize(14f);
        return barData;
    }


    private void showCompletedQuestsBarChart(int[][] data, String[] xLabels) {
        completedQuestsBarChart.setTag(data);
        if (selectedCompleted.isEmpty()) {
            completedQuestsBarChart.setData(null);
        } else {
            BarData barData = createCategoryBarData(data, selectedCompleted);
            completedQuestsBarChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));
            completedQuestsBarChart.getXAxis().setLabelCount(xLabels.length);
            completedQuestsBarChart.setData(barData);
        }
        invalidateAndAnimate(completedQuestsBarChart);
    }

    private void showXpEarnedLineChart(int[] data, int range, String currentRangeLabel, String prevRangeLabel, String[] xLabels, boolean drawHandles) {
        LineData lineData = createThisVsLastPeriodLineData(data, range, currentRangeLabel, prevRangeLabel, drawHandles);

        xpEarnedLineChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));

        xpEarnedLineChart.setData(lineData);
        invalidateAndAnimate(xpEarnedLineChart);
    }

    private void showCoinsEarnedLineChart(int[] data, int range, String currentRangeLabel, String prevRangeLabel, String[] xLabels, boolean drawHandles) {
        LineData lineData = createThisVsLastPeriodLineData(data, range, currentRangeLabel, prevRangeLabel, drawHandles);

        coinsEarnedLineChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));

        coinsEarnedLineChart.setData(lineData);
        invalidateAndAnimate(coinsEarnedLineChart);
    }

    @NonNull
    private LineData createThisVsLastPeriodLineData(int[] data, int prevRangeLength, String currentRangeLabel, String prevRangeLabel, boolean drawHandles) {
        List<Entry> thisPeriodEntries = new ArrayList<>();
        for (int i = prevRangeLength; i < data.length; i++) {
            thisPeriodEntries.add(new Entry(i - prevRangeLength, data[i]));
        }
        LineDataSet thisPeriodDataSet = new LineDataSet(thisPeriodEntries, currentRangeLabel);

        applyLineDataSetStyle(thisPeriodDataSet, R.color.md_red_A200, R.color.md_red_A400, drawHandles);

        List<Entry> lastPeriodEntries = new ArrayList<>();
        for (int i = 0; i < prevRangeLength; i++) {
            lastPeriodEntries.add(new Entry(i, data[i]));
        }
        LineDataSet lastPeriodDataSet = new LineDataSet(lastPeriodEntries, prevRangeLabel);
        applyLineDataSetStyle(lastPeriodDataSet, R.color.md_blue_A200, R.color.md_blue_A400, drawHandles);

        return new LineData(lastPeriodDataSet, thisPeriodDataSet);
    }

    private void showTimeSpentLineChart(int[][] data, String[] xLabels, boolean drawHandles) {
        timeSpentLineChart.setTag(data);
        if (selectedTimeSpent.isEmpty()) {
            timeSpentLineChart.setData(null);
        } else {
            LineData lineData = createCategoryLineData(data, drawHandles, selectedTimeSpent);
            timeSpentLineChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));
            timeSpentLineChart.setData(lineData);
        }
        invalidateAndAnimate(timeSpentLineChart);
    }

    private void showCompletedQuestsLineChart(int[][] data, String[] xLabels, boolean drawHandles) {
        completedQuestsLineChart.setTag(data);
        if (selectedCompleted.isEmpty()) {
            completedQuestsLineChart.setData(null);
        } else {
            LineData lineData = createCategoryLineData(data, drawHandles, selectedCompleted);
            completedQuestsLineChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));
            completedQuestsLineChart.setData(lineData);
        }
        invalidateAndAnimate(completedQuestsLineChart);
    }


    private void showAwesomenessLineChart(int[] data, int prevRangeLength, String currentRangeLabel, String prevRangeLabel, String[] xLabels, boolean drawHandles) {
        LineData lineData = createThisVsLastPeriodLineData(data, prevRangeLength, currentRangeLabel, prevRangeLabel, drawHandles);

        awesomenessLineChart.getXAxis().setValueFormatter(new XAxisValueFormatter(xLabels));

        awesomenessLineChart.setData(lineData);
        invalidateAndAnimate(awesomenessLineChart);
    }

    @NonNull
    private LineData createCategoryLineData(int[][] data, boolean drawHandles, Set<Category> visibleCategories) {

        LineData lineData = new LineData();

        if (visibleCategories.contains(Category.WELLNESS)) {
            LineDataSet wellnessDataSet = createLineDataSetForCategory(data, Category.WELLNESS, drawHandles, categoryToColor.get(Category.WELLNESS), R.color.md_green_700);
            lineData.addDataSet(wellnessDataSet);
        }

        if (visibleCategories.contains(Category.LEARNING)) {
            LineDataSet learningDataSet = createLineDataSetForCategory(data, Category.LEARNING, drawHandles, categoryToColor.get(Category.LEARNING), R.color.md_blue_A400);
            lineData.addDataSet(learningDataSet);
        }

        if (visibleCategories.contains(Category.WORK)) {
            LineDataSet workDataSet = createLineDataSetForCategory(data, Category.WORK, drawHandles, categoryToColor.get(Category.WORK), R.color.md_red_A400);
            lineData.addDataSet(workDataSet);
        }

        if (visibleCategories.contains(Category.PERSONAL)) {
            LineDataSet personalDataSet = createLineDataSetForCategory(data, Category.PERSONAL, drawHandles, categoryToColor.get(Category.PERSONAL), R.color.md_orange_A400);
            lineData.addDataSet(personalDataSet);
        }

        if (visibleCategories.contains(Category.FUN)) {
            LineDataSet funDataSet = createLineDataSetForCategory(data, Category.FUN, drawHandles, categoryToColor.get(Category.FUN), R.color.md_purple_500);
            lineData.addDataSet(funDataSet);
        }

        if (visibleCategories.contains(Category.CHORES)) {
            LineDataSet choresDataSet = createLineDataSetForCategory(data, Category.CHORES, drawHandles, categoryToColor.get(Category.CHORES), R.color.md_brown_500);
            lineData.addDataSet(choresDataSet);
        }

        return lineData;
    }

    @NonNull
    private LineDataSet createLineDataSetForCategory(int[][] data, Category category, boolean drawHandles, @ColorRes int color, @ColorRes int highlightColor) {
        int[] categoryData = data[category.ordinal()];
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < categoryData.length; i++) {
            entries.add(new Entry(i, categoryData[i]));
        }
        LineDataSet dataSet = new LineDataSet(entries, getString(Category.getNameRes(category)));

        applyLineDataSetStyle(dataSet, color, highlightColor, drawHandles);
        return dataSet;
    }

    private void applyLineDataSetStyle(LineDataSet dataSet, @ColorRes int color, @ColorRes int highlightColor, boolean drawHandles) {
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        if (drawHandles) {
            dataSet.setDrawCircleHole(true);
        } else {
            dataSet.setDrawCircleHole(false);
            dataSet.setDrawCircles(false);
        }
        dataSet.setCircleHoleRadius(3f);
        dataSet.setCircleColorHole(Color.WHITE);
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), color));
        dataSet.setCircleRadius(6f);
        dataSet.setLineWidth(2f);
        dataSet.setHighLightColor(ContextCompat.getColor(getContext(), highlightColor));
        dataSet.setDrawValues(false);
        dataSet.setColor(ContextCompat.getColor(getContext(), color));
    }

    private void applyDefaultStyle(LineChart chart) {
        chart.setDescription(null);
        chart.setDrawBorders(false);
        chart.setExtraBottomOffset(8);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setYOffset(12f);
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(12f);
        yAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_26));
        yAxis.setXOffset(12f);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);
        yAxis.setGranularity(1f);

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setYOffset(8f);
        legend.setTextSize(12f);
    }

    private void applyDefaultStyle(BarChart chart) {
        applyDefaultStyle(chart, false);
    }

    private void applyDefaultStyle(BarChart chart, boolean showLegend) {
        chart.setTouchEnabled(false);
        chart.setDescription(null);
        chart.setDrawBorders(false);
        chart.setExtraBottomOffset(8);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setYOffset(12f);
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(12f);
        yAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_26));
        yAxis.setXOffset(12f);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);
        yAxis.setGranularity(1f);

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        if (showLegend) {
            legend.setYOffset(8f);
            legend.setTextSize(12f);
        } else {
            legend.setEnabled(false);
        }
    }

    @OnClick(R.id.filter_completed_quests)
    public void onFilterCompletedQuestsClick(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.category_filter, (ViewGroup) getActivity().findViewById(R.id.filters_container));
        ViewGroup filterContainer = (ViewGroup) layout.findViewById(R.id.filters_container);
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            Switch child = (Switch) filterContainer.getChildAt(i);
            Category category = Category.values()[i];
            if (selectedCompleted.contains(category)) {
                child.setChecked(true);
            }
            child.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int buttonId = buttonView.getId();
                Category selectedCategory = getSelectedCategory(buttonId);
                if (isChecked) {
                    selectedCompleted.add(selectedCategory);
                } else {
                    selectedCompleted.remove(selectedCategory);
                }
                eventBus.post(new GrowthCategoryFilteredEvent(selectedCategory, isChecked, spinner.getSelectedItemPosition(), "completedQuests"));
                showFilteredCompletedChart(spinner.getSelectedItemPosition());
            });
        }
        showPopupWindow(view, layout);
    }

    @OnClick(R.id.filter_time_spent)
    public void onFilterTimeSpentClick(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.category_filter, (ViewGroup) getActivity().findViewById(R.id.filters_container));
        ViewGroup filterContainer = (ViewGroup) layout.findViewById(R.id.filters_container);
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            Switch child = (Switch) filterContainer.getChildAt(i);
            Category category = Category.values()[i];
            if (selectedTimeSpent.contains(category)) {
                child.setChecked(true);
            }
            child.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int buttonId = buttonView.getId();
                Category selectedCategory = getSelectedCategory(buttonId);
                if (isChecked) {
                    selectedTimeSpent.add(selectedCategory);
                } else {
                    selectedTimeSpent.remove(selectedCategory);
                }
                eventBus.post(new GrowthCategoryFilteredEvent(selectedCategory, isChecked, spinner.getSelectedItemPosition(), "timeSpent"));
                showFilteredTimeSpentChart(spinner.getSelectedItemPosition());
            });
        }
        showPopupWindow(view, layout);
    }

    @NonNull
    private Category getSelectedCategory(int buttonId) {
        Category selectedCategory = Category.CHORES;
        switch (buttonId) {
            case R.id.filter_wellness:
                selectedCategory = Category.WELLNESS;
                break;
            case R.id.filter_learning:
                selectedCategory = Category.LEARNING;
                break;
            case R.id.filter_work:
                selectedCategory = Category.WORK;
                break;
            case R.id.filter_personal:
                selectedCategory = Category.PERSONAL;
                break;
            case R.id.filter_fun:
                selectedCategory = Category.FUN;
                break;
        }
        return selectedCategory;
    }

    private void showFilteredTimeSpentChart(int itemPosition) {
        if (itemPosition == THIS_WEEK || itemPosition == THIS_MONTH) {
            if (selectedTimeSpent.isEmpty()) {
                timeSpentLineChart.setData(null);
            } else {
                LineData lineData = createCategoryLineData((int[][]) timeSpentLineChart.getTag(), itemPosition == THIS_WEEK, selectedTimeSpent);
                timeSpentLineChart.setData(lineData);
            }
            invalidateAndAnimate(timeSpentLineChart);
        } else {
            if (selectedTimeSpent.isEmpty()) {
                timeSpentBarChart.setData(null);
            } else {

                BarData barData = createCategoryBarData((int[][]) timeSpentBarChart.getTag(), selectedTimeSpent);
                timeSpentBarChart.setData(barData);
            }
            invalidateAndAnimate(timeSpentBarChart);
        }
    }

    private void invalidateAndAnimate(Chart<?> chart) {
        chart.invalidate();
        chart.animateY(CHART_ANIMATION_DURATION, DEFAULT_EASING_OPTION);
    }

    private void showFilteredCompletedChart(int itemPosition) {
        if (itemPosition == THIS_WEEK || itemPosition == THIS_MONTH) {
            if (selectedCompleted.isEmpty()) {
                completedQuestsLineChart.setData(null);
            } else {
                LineData lineData = createCategoryLineData((int[][]) completedQuestsLineChart.getTag(), itemPosition == THIS_WEEK, selectedCompleted);
                completedQuestsLineChart.setData(lineData);
            }
            invalidateAndAnimate(completedQuestsLineChart);
        } else {
            if (selectedCompleted.isEmpty()) {
                completedQuestsBarChart.setData(null);
            } else {
                BarData barData = createCategoryBarData((int[][]) completedQuestsBarChart.getTag(), selectedCompleted);
                completedQuestsBarChart.setData(barData);
            }
            invalidateAndAnimate(completedQuestsBarChart);
        }
    }

    private void showPopupWindow(View view, View layout) {
        PopupWindow popupWindow = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(view);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_growth, R.string.help_dialog_growth_title, "growth").show(getActivity().getSupportFragmentManager());
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        eventBus.post(new GrowthIntervalSelectedEvent(position));
        showCharts(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}