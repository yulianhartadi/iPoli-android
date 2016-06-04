package io.ipoli.android.player.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.squareup.otto.Bus;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/16.
 */
public class GrowthFragment extends BaseFragment {

    @BindView(R.id.time_spent_chart)
    PieChart timeSpentChart;

    @BindView(R.id.experience_chart)
    BarChart experienceChart;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_growth, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.fragment_title_growth);

        return view;
    }

    private void setUpExperienceChart(List<Quest> quests, int dayCount) {
        experienceChart.setDrawBarShadow(false);
        experienceChart.setDrawValueAboveBar(true);

        experienceChart.setDescription("");
        experienceChart.setPinchZoom(false);

        experienceChart.setDrawGridBackground(false);
        experienceChart.setExtraOffsets(5, 10, 5, 5);
        experienceChart.setNoDataText("Not enough data to display");

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
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

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

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            xVals.add(String.valueOf(i + 1));
        }

        if (quests.isEmpty()) {
            return;
        }
        TreeMap<Date, List<Quest>> groupedByDate = new TreeMap<>();

        for (LocalDate date = new LocalDate().minusDays(dayCount - 1); date.isBefore(new LocalDate().plusDays(1)); date = date.plusDays(1)) {
            groupedByDate.put(date.toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate(), new ArrayList<>());
        }

        for (Quest q : quests) {
            groupedByDate.get(q.getEndDate()).add(q);
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        int index = 0;
        for (Map.Entry<Date, List<Quest>> pair : groupedByDate.entrySet()) {
            int total = 0;
            for (Quest q : pair.getValue()) {
                total += q.getExperience();
            }
            yVals1.add(new BarEntry(total, index));
            index++;
        }

        BarDataSet set1;

        set1 = new BarDataSet(yVals1, "Experience gain per day");
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
        timeSpentChart.setExtraOffsets(5, 0, 5, 5);

        timeSpentChart.setDragDecelerationFrictionCoef(0.95f);

        timeSpentChart.setDrawHoleEnabled(true);
        timeSpentChart.setDescription("Time spent per context");
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
        setData(quests);


        timeSpentChart.animateY(getResources().getInteger(android.R.integer.config_longAnimTime), Easing.EasingOption.EaseInOutQuad);
    }

    private void setData(List<Quest> quests) {

        if (quests.isEmpty()) {
            return;
        }

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        TreeMap<QuestContext, List<Quest>> groupedByContext = new TreeMap<>();

        Set<QuestContext> usedContexts = new TreeSet<>();
        for (Quest q : quests) {
            QuestContext ctx = Quest.getContext(q);
            if (!groupedByContext.containsKey(ctx)) {
                groupedByContext.put(ctx, new ArrayList<>());
            }
            groupedByContext.get(ctx).add(q);
            usedContexts.add(ctx);
        }

        ArrayList<String> xVals = new ArrayList<String>();
        List<Integer> colors = new ArrayList<>();
        for (QuestContext usedCtx : usedContexts) {
            xVals.add(StringUtils.capitalize(usedCtx.name()));
            colors.add(getColor(usedCtx.resLightColor));
        }

        int index = 0;
        int total = 0;
        long totalXP = 0;
        long totalCoins = 0;
        for (Map.Entry<QuestContext, List<Quest>> pair : groupedByContext.entrySet()) {
            int sum = 0;
            for (Quest q : pair.getValue()) {
                totalXP += q.getExperience();
                totalCoins += q.getCoins();
                if (q.getActualStart() != null) {
                    sum += TimeUnit.MILLISECONDS.toMinutes(q.getCompletedAt().getTime() - q.getActualStart().getTime());
                } else {
                    sum += Math.max(q.getDuration(), 5);
                }
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

        timeSpentChart.highlightValues(null);

        String centerText = String.format(Locale.getDefault(), "XP: %d\nCoins: %d", totalXP, totalCoins);
        timeSpentChart.setCenterText(centerText);
        timeSpentChart.setCenterTextColor(getColor(R.color.md_dark_text_87));
        timeSpentChart.setCenterTextSize(12f);
    }

    private int getColor(@ColorRes int color) {
        return ContextCompat.getColor(getActivity(), color);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        questPersistenceService.findAllCompletedNonAllDayBetween(new LocalDate().minusDays(6), new LocalDate().plusDays(1))
                .compose(bindToLifecycle()).subscribe(quests -> {
            setUpTimeSpentChart(quests);
            setUpExperienceChart(quests, 7);
        });
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
}
