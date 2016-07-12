package io.ipoli.android.quest.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmRepeatingQuestPersistenceService;
import io.ipoli.android.quest.ui.formatters.DateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.FrequencyTextFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/9/16.
 */
public class RepeatingQuestActivity extends BaseActivity {

    public static final int BAR_COUNT = 4;

    @BindView(R.id.repeating_quest_progress_container)
    ViewGroup progressContainer;

    @BindView(R.id.repeating_quest_history)
    BarChart history;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.quest_name)
    TextView name;

    @BindView(R.id.quest_category_image)
    ImageView categoryImage;

    @BindView(R.id.quest_category_name)
    TextView categoryName;

    @BindView(R.id.quest_next_scheduled_date)
    TextView nextScheduledDate;

    @BindView(R.id.quest_frequency_interval)
    TextView frequencyInterval;

    @BindView(R.id.quest_interval_duration)
    TextView intervalDuration;

    private RepeatingQuest repeatingQuest;
    private RealmRepeatingQuestPersistenceService repeatingQuestPersistenceService;
    private RealmQuestPersistenceService questPersistenceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || StringUtils.isEmpty(getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY))) {
            finish();
            return;
        }

        repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, getRealm());
        questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());

        String repeatingQuestId = getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY);
        repeatingQuest = repeatingQuestPersistenceService.findById(repeatingQuestId);

        if (repeatingQuest == null) {
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
        }

        eventBus.post(new ScreenShownEvent(EventSource.REPEATING_QUEST));
        displayRepeatingQuest();
    }

    private Pair<LocalDate, LocalDate> getCurrentInterval() {
        LocalDate today = LocalDate.now();
        if (repeatingQuest.getRecurrence().getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
            return new Pair<>(today.dayOfMonth().withMinimumValue(), today.dayOfMonth().withMaximumValue());
        } else {
            return new Pair<>(today.dayOfWeek().withMinimumValue(), today.dayOfWeek().withMaximumValue());
        }
    }

    private long findCompletedForCurrentInterval() {
        Pair<LocalDate, LocalDate> interval = getCurrentInterval();
        return questPersistenceService.countCompletedQuests(repeatingQuest, interval.first, interval.second);
    }

    private void displayRepeatingQuest() {
        name.setText(repeatingQuest.getName());

        Category category = RepeatingQuest.getCategory(repeatingQuest);
        long completed = findCompletedForCurrentInterval();
        showFrequencyProgress(category, completed);

        int timeSpent = (int) getTotalTimeSpent(completed);
        intervalDuration.setText(timeSpent > 0 ? DurationFormatter.formatShort(timeSpent, "") : "0");

        frequencyInterval.setText(FrequencyTextFormatter.formatInterval(getFrequency(), repeatingQuest.getRecurrence()));

        categoryName.setText(StringUtils.capitalize(category.name()));
        categoryImage.setImageResource(category.whiteImage);

        Date nextDate = questPersistenceService.findNextUncompletedQuestEndDate(repeatingQuest);
        nextScheduledDate.setText(DateFormatter.formatWithoutYear(nextDate));

        colorLayout(category);
        setupChart();
    }

    private long getTotalTimeSpent(long completed) {
        Pair<LocalDate, LocalDate> interval = getCurrentInterval();
        List<Quest> completedWithStartTime = questPersistenceService.findAllCompletedWithStartTime(repeatingQuest, interval.first, interval.second);

        long totalTime = (completed - completedWithStartTime.size()) * repeatingQuest.getDuration();
        for (Quest completedQuest : completedWithStartTime) {
            totalTime += completedQuest.getActualDuration();
        }
        return totalTime;
    }

    private void showFrequencyProgress(Category category, long completed) {
        LayoutInflater inflater = LayoutInflater.from(this);

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
        xLabels.setTextSize(12f);
        xLabels.setDrawAxisLine(false);
        xLabels.setDrawGridLines(false);
        history.getLegend().setEnabled(false);

        setHistoryData();
    }

    private void colorLayout(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.resLightColor));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.resLightColor));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.resDarkColor));
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
        for (int i = 0; i < BAR_COUNT; i++) {
            Pair<LocalDate, LocalDate> monthPair = monthPairs.get(i);
            yValues.add(new BarEntry(questPersistenceService.countCompletedQuests(repeatingQuest, monthPair.first, monthPair.second), i));
        }

        BarDataSet dataSet = new BarDataSet(yValues, "DataSet");
        dataSet.setColors(getColors());
        dataSet.setBarShadowColor(ContextCompat.getColor(this, RepeatingQuest.getCategory(repeatingQuest).color100));

        List<String> xValues = new ArrayList<>();
        xValues.add(getMonthText(monthPairs.get(0).first));
        xValues.add(getMonthText(monthPairs.get(1).first));
        xValues.add(getMonthText(monthPairs.get(2).first));
        xValues.add("this month");
        setHistoryData(dataSet, xValues);
    }

    private String getMonthText(LocalDate date) {
        return date.monthOfYear().getAsShortText();
    }

    private void setWeeklyHistoryData() {

        List<BarEntry> yValues = new ArrayList<>();
        List<Pair<LocalDate, LocalDate>> weekPairs = getBoundsFor4WeeksInThePast(LocalDate.now());
        for (int i = 0; i < BAR_COUNT; i++) {
            Pair<LocalDate, LocalDate> weekPair = weekPairs.get(i);
            yValues.add(new BarEntry(questPersistenceService.countCompletedQuests(repeatingQuest, weekPair.first, weekPair.second.plusDays(1)), i));
        }

        BarDataSet dataSet = new BarDataSet(yValues, "DataSet");
        dataSet.setColors(getColors());
        dataSet.setBarShadowColor(ContextCompat.getColor(this, RepeatingQuest.getCategory(repeatingQuest).color100));

        List<String> xValues = new ArrayList<>();
        xValues.add(getWeekRangeText(weekPairs.get(0).first, weekPairs.get(0).second));
        xValues.add(getWeekRangeText(weekPairs.get(1).first, weekPairs.get(1).second));
        xValues.add("last week");
        xValues.add("this week");
        setHistoryData(dataSet, xValues);
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
        LocalDate weekStart = currentDate.dayOfWeek().withMinimumValue().minusDays(21);
        LocalDate weekEnd = weekStart.dayOfWeek().withMaximumValue();

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(weekStart, weekEnd));
        for (int i = 0; i < 3; i++) {
            weekStart = weekStart.plusDays(7);
            weekEnd = weekEnd.plusDays(7);
            weekBounds.add(new Pair<>(weekStart, weekEnd));
        }
        return weekBounds;
    }

    private void setHistoryData(BarDataSet dataSet, List<String> xValues) {
        BarData data = new BarData(xValues, dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.valueOf((int) value));

        history.setData(data);
        history.animateY(500);
    }

    private String getWeekRangeText(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart.getMonthOfYear() == weekEnd.getMonthOfYear()) {
            return weekStart.getDayOfMonth() + " - " + weekEnd.getDayOfMonth() + " " + weekEnd.monthOfYear().getAsShortText();
        } else {
            return weekStart.getDayOfMonth() + " " + weekStart.monthOfYear().getAsShortText() + " - " + weekEnd.getDayOfMonth() + " " + weekEnd.monthOfYear().getAsShortText();
        }
    }

    private int[] getColors() {
        int[] colors = new int[BAR_COUNT];
        Category category = RepeatingQuest.getCategory(repeatingQuest);
        for (int i = 0; i < BAR_COUNT; i++) {
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

}