package io.ipoli.android.player.fragments;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.exceptions.GrowthException;
import io.ipoli.android.player.events.GrowthIntervalSelectedEvent;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/16.
 */
public class GrowthFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.chart_container)
    View chartContainer;

    @BindView(R.id.empty_view_container)
    View emptyViewContainer;

    @BindView(R.id.summary_stats_quests_done)
    TextView questsDone;

    @BindView(R.id.summary_stats_hours_spent)
    TextView hoursSpent;

    @BindView(R.id.time_spent_chart)
    PieChart timeSpentChart;

    @BindView(R.id.experience_chart)
    BarChart experienceChart;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_spinner)
    Spinner spinner;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    private Unbinder unbinder;

    private int currentDayCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_growth, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(actionBar.getThemedContext(),
                R.layout.growth_spinner_item,
                R.id.growth_interval,
                getResources().getStringArray(R.array.growth_intervals));
        adapter.setDropDownViewResource(R.layout.growth_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.md_white), PorterDuff.Mode.SRC_ATOP);
        spinner.setSelection(1, false);
        spinner.setOnItemSelectedListener(this);
        ((MainActivity) getActivity()).actionBarDrawerToggle.syncState();

        currentDayCount = 7;
        return view;
    }

    private void showCharts(int dayCount) {
        questPersistenceService.findAllCompletedNonAllDayBetween(LocalDate.now().minusDays(dayCount - 1), LocalDate.now(), quests -> {
            if (quests.isEmpty()) {
                chartContainer.setVisibility(View.GONE);
                emptyViewContainer.setVisibility(View.VISIBLE);
            } else {
                chartContainer.setVisibility(View.VISIBLE);
                emptyViewContainer.setVisibility(View.GONE);
                setUpSummaryStats(quests);
                setUpTimeSpentChart(quests);
                setUpExperienceChart(quests, dayCount);
            }
        });
    }

    private void setUpSummaryStats(List<Quest> quests) {
        questsDone.setText(getString(R.string.summary_stats_quests_done, quests.size()));
        int totalMin = 0;
        for (Quest q : quests) {
            totalMin += getDurationForCompletedQuest(q);
        }
        hoursSpent.setText(getString(R.string.summary_stats_hours_spent, totalMin / 60.0f));
    }

    private void setUpExperienceChart(List<Quest> quests, int dayCount) {
        experienceChart.setDrawBarShadow(false);
        experienceChart.setDrawValueAboveBar(true);

        experienceChart.setDescription("");
        experienceChart.setTouchEnabled(false);
        experienceChart.setPinchZoom(false);

        experienceChart.setDrawGridBackground(false);
        experienceChart.setExtraOffsets(5, 10, 5, 5);
        Paint textPaint = experienceChart.getPaint(BarChart.PAINT_INFO);
        textPaint.setTextSize(Utils.convertDpToPixel(14f));
        textPaint.setColor(getColor(R.color.md_dark_text_87));
        experienceChart.setNoDataText(getString(R.string.chart_no_data_to_display));

        XAxis xAxis = experienceChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getColor(R.color.md_dark_text_54));
        xAxis.setTextSize(10f);
        xAxis.setSpaceBetweenLabels(2);

        YAxis leftAxis = experienceChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setTextColor(getColor(R.color.md_dark_text_54));
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinValue(0f);

        YAxis rightAxis = experienceChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setTextColor(getColor(R.color.md_dark_text_54));
        rightAxis.setSpaceTop(15f);
        rightAxis.setTextSize(10f);
        rightAxis.setAxisMinValue(0f);

        Legend l = experienceChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setTextSize(10f);
        l.setTextColor(getColor(R.color.md_dark_text_87));

        setExperienceChartData(quests, dayCount);
    }

    private void setExperienceChartData(List<Quest> quests, int dayCount) {

        if (quests.isEmpty()) {
            experienceChart.clear();
            return;
        }

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            xVals.add(String.valueOf(i + 1));
        }
        TreeMap<LocalDate, List<Quest>> groupedByDate = new TreeMap<>();

        for (LocalDate date = LocalDate.now().minusDays(dayCount - 1); date.isBefore(LocalDate.now().plusDays(1)); date = date.plusDays(1)) {
            groupedByDate.put(date, new ArrayList<>());
        }

        for (Quest q : quests) {
            LocalDate dateKey = q.getCompletedAtDate();
            try {
                groupedByDate.get(dateKey).add(q);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("CompletedAt date with time ").append(DateUtils.toMillis(q.getCompletedAtDate()))
                        .append(" of quest with id ").append(q.getId())
                        .append(" was not present in generated dates for ").append(dayCount).append(" days: ");
                for(LocalDate d : groupedByDate.keySet()) {
                    sb.append(DateUtils.toMillis(d)).append(" ");
                }
                eventBus.post(new GrowthException(sb.toString(), e));
            }
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        int index = 0;
        for (Map.Entry<LocalDate, List<Quest>> pair : groupedByDate.entrySet()) {
            int total = 0;
            for (Quest q : pair.getValue()) {
                total += q.getExperience();
            }
            yVals1.add(new BarEntry(total, index));
            index++;
        }

        BarDataSet set1;

        set1 = new BarDataSet(yVals1, getString(R.string.chart_experience_description));
        set1.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.valueOf(Math.round(value)));
        set1.setBarSpacePercent(35f);
        set1.setColors(new int[]{getColor(R.color.md_blue_300)});

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(12f);
        data.setValueTextColor(getColor(R.color.md_dark_text_87));

        experienceChart.setData(data);
        experienceChart.animateY(getResources().getInteger(android.R.integer.config_longAnimTime), Easing.EasingOption.EaseInOutQuad);
    }

    private void setUpTimeSpentChart(List<Quest> quests) {
        timeSpentChart.setTouchEnabled(false);
        timeSpentChart.setExtraOffsets(5, 0, 5, 5);

        timeSpentChart.setDragDecelerationFrictionCoef(0.95f);

        timeSpentChart.setDrawHoleEnabled(true);
        timeSpentChart.setDescription(getString(R.string.chart_time_spent_description));
        timeSpentChart.setDescriptionColor(getColor(R.color.md_dark_text_87));
        timeSpentChart.setDescriptionTextSize(10f);
        timeSpentChart.setHoleColor(Color.WHITE);

        timeSpentChart.setTransparentCircleColor(Color.WHITE);
        timeSpentChart.setTransparentCircleAlpha(110);

        timeSpentChart.setHoleRadius(44f);
        timeSpentChart.setTransparentCircleRadius(47f);

        timeSpentChart.setDrawCenterText(true);

        timeSpentChart.setRotationAngle(0);
        timeSpentChart.setRotationEnabled(true);
        timeSpentChart.setHighlightPerTapEnabled(true);
        timeSpentChart.getLegend().setEnabled(false);

        Paint textPaint = timeSpentChart.getPaint(PieChart.PAINT_INFO);
        textPaint.setTextSize(Utils.convertDpToPixel(14f));
        textPaint.setColor(getColor(R.color.md_dark_text_87));
        timeSpentChart.setNoDataText(getString(R.string.chart_no_data_to_display));

        setData(quests);

        timeSpentChart.animateY(getResources().getInteger(android.R.integer.config_longAnimTime), Easing.EasingOption.EaseInOutQuad);
    }

    private void setData(List<Quest> quests) {

        if (quests.isEmpty()) {
            timeSpentChart.clear();
            return;
        }

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        TreeMap<Category, List<Quest>> groupedByCategory = new TreeMap<>();

        Set<Category> usedCategories = new TreeSet<>();
        for (Quest q : quests) {
            Category category = q.getCategoryType();
            if (!groupedByCategory.containsKey(category)) {
                groupedByCategory.put(category, new ArrayList<>());
            }
            groupedByCategory.get(category).add(q);
            usedCategories.add(category);
        }

        ArrayList<String> xVals = new ArrayList<String>();
        List<Integer> colors = new ArrayList<>();
        for (Category usedCategory : usedCategories) {
            xVals.add(StringUtils.capitalize(usedCategory.name()));
            colors.add(getColor(usedCategory.color500));
        }

        int index = 0;
        int total = 0;
        long totalXP = 0;
        long totalCoins = 0;
        for (Map.Entry<Category, List<Quest>> pair : groupedByCategory.entrySet()) {
            int sum = 0;
            for (Quest q : pair.getValue()) {
                totalXP += q.getExperience();
                totalCoins += q.getCoins();
                sum += getDurationForCompletedQuest(q);
            }
            total += sum;
            yVals1.add(new Entry(sum, index));
            index++;
        }

        final int totalTimeSpent = total;

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) ->
                String.format("%.1fh (%d%%)", value / 60.0f, (int) ((value / totalTimeSpent) * 100))
        );

        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(getColor(R.color.md_white));
        timeSpentChart.setData(data);

        timeSpentChart.setCenterText(getString(R.string.chart_time_spent_center_text, totalXP, totalCoins));
        timeSpentChart.setCenterTextColor(getColor(R.color.md_dark_text_87));
        timeSpentChart.setCenterTextSize(12f);
    }

    private int getDurationForCompletedQuest(Quest q) {
        return Math.max(q.getActualDuration(), Constants.QUEST_MIN_DURATION);
    }

    private int getColor(@ColorRes int color) {
        return ContextCompat.getColor(getActivity(), color);
    }

    @Override
    public void onDestroyView() {
        questPersistenceService.removeAllListeners();
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        showCharts(currentDayCount);
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
        currentDayCount = 1;
        if (position == 1) {
            currentDayCount = 7;
        } else if (position == 2) {
            currentDayCount = 30;
        }
        eventBus.post(new GrowthIntervalSelectedEvent(currentDayCount));
        showCharts(currentDayCount);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}