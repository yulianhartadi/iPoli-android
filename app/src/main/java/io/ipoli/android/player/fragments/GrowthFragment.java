package io.ipoli.android.player.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.squareup.otto.Bus;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.quest.QuestContext;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/16.
 */
public class GrowthFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    @BindView(R.id.time_spent_chart)
    PieChart timeSpentChart;

    @BindView(R.id.experience_chart)
    BarChart experienceChart;

    private Unbinder unbinder;

    protected String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
    };

    protected String[] mParties = new String[]{
            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
            "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
            "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
            "Party Y", "Party Z"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_growth, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        setUpTimeSpentChart();

//        LineData data = getData(7, 5);
////        data.setValueTextColor(getColor(R.color.md_blue_500));
//
//        // add some transparency to the color with "& 0x90FFFFFF"
//        setupChart(experienceChart, data);

        experienceChart.setDrawBarShadow(false);
        experienceChart.setDrawValueAboveBar(true);

        experienceChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        experienceChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        experienceChart.setPinchZoom(false);

        experienceChart.setDrawGridBackground(false);
        experienceChart.setExtraOffsets(5, 10, 5, 5);
        // experienceChart.setDrawYLabels(false);

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

        setDataBar(7, 200);


        return view;
    }

    private void setDataBar(int count, float range) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(String.valueOf(i + 1));
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            int val = (int) (Math.random() * mult);
            yVals1.add(new BarEntry(val, i));
        }

        BarDataSet set1;

        set1 = new BarDataSet(yVals1, "Experience gain per day");
        set1.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf(Math.round(value));
            }
        });
        set1.setBarSpacePercent(35f);
        set1.setColors(new int[]{getColor(R.color.md_blue_300)});

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(12f);
        data.setValueTextColor(getColor(R.color.md_dark_text_87));

        experienceChart.setData(data);
        experienceChart.animateY(getResources().getInteger(android.R.integer.config_longAnimTime), Easing.EasingOption.EaseInOutQuad);
    }

    private void setUpTimeSpentChart() {
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

        String centerText = "XP: 2565\nCoins: 450";
        timeSpentChart.setCenterText(centerText);
        timeSpentChart.setCenterTextColor(getColor(R.color.md_dark_text_87));
        timeSpentChart.setCenterTextSize(12f);

        timeSpentChart.setRotationAngle(0);
        timeSpentChart.setRotationEnabled(true);
        timeSpentChart.setHighlightPerTapEnabled(true);
        timeSpentChart.getLegend().setEnabled(false);
        setData(5, 30);

        timeSpentChart.animateY(getResources().getInteger(android.R.integer.config_longAnimTime), Easing.EasingOption.EaseInOutQuad);
    }

    private void setData(int count, float range) {

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < count + 1; i++) {
            yVals1.add(new Entry((float) (Math.random() * range) + range / 5, i));
        }

        ArrayList<String> xVals = new ArrayList<String>();

        xVals.add("Learning");
        xVals.add("Wellness");
        xVals.add("Personal");
        xVals.add("Work");
        xVals.add("Fun");
        xVals.add("Chores");

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return Math.round(value) + " h";
            }
        });

        dataSet.setSelectionShift(5f);

        dataSet.setColors(new int[]{getColor(QuestContext.LEARNING.resLightColor),
                getColor(QuestContext.WELLNESS.resLightColor),
                getColor(QuestContext.PERSONAL.resLightColor),
                getColor(QuestContext.WORK.resLightColor),
                getColor(QuestContext.FUN.resLightColor),
                getColor(QuestContext.CHORES.resLightColor)});

        PieData data = new PieData(xVals, dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(getColor(R.color.md_white));
        timeSpentChart.setData(data);

        timeSpentChart.highlightValues(null);

        timeSpentChart.invalidate();
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
