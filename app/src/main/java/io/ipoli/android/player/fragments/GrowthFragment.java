package io.ipoli.android.player.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.help.HelpDialog;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/16.
 */
public class GrowthFragment extends BaseFragment {

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.awesomeness_chart)
    LineChart awesomenessChart;

    @BindView(R.id.completed_quests_chart)
    LineChart completedQuestsChart;

    @Inject
    Bus eventBus;

    private Unbinder unbinder;

    public class CustomMarkerView extends MarkerView {

        private TextView popupContent;

        public CustomMarkerView(Context context) {
            super(context, R.layout.chart_popup);
            popupContent = (TextView) findViewById(R.id.popup_content);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            @ColorRes int backgroundColor = (int) e.getData();
            GradientDrawable drawable = (GradientDrawable) popupContent.getBackground();
            drawable.setColor(ContextCompat.getColor(getContext(), backgroundColor));
            popupContent.setText(String.valueOf((int) e.getY())); // set the entry-value as the display text
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2f), -(getHeight() * 1.5f));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_growth, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.growth);
        collapsingToolbarLayout.setTitleEnabled(false);
        setupAwesomenessChart();
        setupCompletedQuestsChart();
        return view;
    }

    private void setupCompletedQuestsChart() {
        applyDefaultStyle(completedQuestsChart);

        List<Entry> wellnessEntries = new ArrayList<>();
        wellnessEntries.add(new Entry(1, 4, R.color.md_green_500));
        wellnessEntries.add(new Entry(2, 9, R.color.md_green_500));
        wellnessEntries.add(new Entry(3, 12, R.color.md_green_500));
        wellnessEntries.add(new Entry(4, 4, R.color.md_green_500));
        wellnessEntries.add(new Entry(5, 8, R.color.md_green_500));
        wellnessEntries.add(new Entry(6, 1, R.color.md_green_500));
        wellnessEntries.add(new Entry(7, 3, R.color.md_green_500));
        LineDataSet wellnessDataSet = new LineDataSet(wellnessEntries, "Wellness");

        applyLineDataSetStyle(wellnessDataSet, R.color.md_green_300, R.color.md_green_500);

        List<Entry> learningEntries = new ArrayList<>();
        learningEntries.add(new Entry(1, 2, R.color.md_blue_500));
        learningEntries.add(new Entry(2, 4, R.color.md_blue_500));
        learningEntries.add(new Entry(3, 5, R.color.md_blue_500));
        learningEntries.add(new Entry(4, 10, R.color.md_blue_500));
        learningEntries.add(new Entry(5, 3, R.color.md_blue_500));
        learningEntries.add(new Entry(6, 1, R.color.md_blue_500));
        learningEntries.add(new Entry(7, 1, R.color.md_blue_500));
        LineDataSet learningDataSet = new LineDataSet(learningEntries, "Learning");

        applyLineDataSetStyle(learningDataSet, R.color.md_blue_300, R.color.md_blue_500);

        List<Entry> workEntries = new ArrayList<>();
        workEntries.add(new Entry(1, 1, R.color.md_red_500));
        workEntries.add(new Entry(2, 4, R.color.md_red_500));
        workEntries.add(new Entry(3, 7, R.color.md_red_500));
        workEntries.add(new Entry(4, 12, R.color.md_red_500));
        workEntries.add(new Entry(5, 4, R.color.md_red_500));
        workEntries.add(new Entry(6, 7, R.color.md_red_500));
        workEntries.add(new Entry(7, 9, R.color.md_red_500));
        LineDataSet workDataSet = new LineDataSet(workEntries, "Work");

        applyLineDataSetStyle(workDataSet, R.color.md_red_300, R.color.md_red_500);
//        workDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_red_300));
//        workDataSet.setDrawFilled(true);

        LineData lineData = new LineData(wellnessDataSet, learningDataSet, workDataSet);

        XAxis xAxis = completedQuestsChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        completedQuestsChart.setMarker(customMarkerView);

        completedQuestsChart.setDescription(null);
        completedQuestsChart.setData(lineData);
        completedQuestsChart.invalidate();
    }

    private void setupAwesomenessChart() {
        applyDefaultStyle(awesomenessChart);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 42, R.color.md_green_500));
        entries.add(new Entry(2, 32, R.color.md_green_500));
        entries.add(new Entry(3, 20, R.color.md_green_500));
        entries.add(new Entry(4, 55, R.color.md_green_500));
        entries.add(new Entry(5, 67, R.color.md_green_500));
//        entries.add(new Entry(6, 80, R.color.md_green_500));
//        entries.add(new Entry(7, 17, R.color.md_green_500));
        LineDataSet thisWeekDataSet = new LineDataSet(entries, "This week");

        applyLineDataSetStyle(thisWeekDataSet, R.color.md_green_300, R.color.md_green_500);


        List<Entry> lastWeekEntries = new ArrayList<>();
        lastWeekEntries.add(new Entry(1, 12, R.color.md_orange_500));
        lastWeekEntries.add(new Entry(2, 21, R.color.md_orange_500));
        lastWeekEntries.add(new Entry(3, 38, R.color.md_orange_500));
        lastWeekEntries.add(new Entry(4, 93, R.color.md_orange_500));
        lastWeekEntries.add(new Entry(5, 64, R.color.md_orange_500));
        lastWeekEntries.add(new Entry(6, 22, R.color.md_orange_500));
        lastWeekEntries.add(new Entry(7, 12, R.color.md_orange_500));
        LineDataSet lastWeekDataSet = new LineDataSet(lastWeekEntries, "Last week");
        applyLineDataSetStyle(lastWeekDataSet, R.color.md_orange_300, R.color.md_orange_500);

        XAxis xAxis = awesomenessChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return "12 Feb";
            }
        });

        YAxis yAxis = awesomenessChart.getAxisLeft();
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return String.valueOf((int) v) + "%";
            }
        });


        xAxis.setLabelCount(lastWeekEntries.size(), true);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(6, true);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        awesomenessChart.setMarker(customMarkerView);
        awesomenessChart.setDescription(null);
        awesomenessChart.setDrawBorders(false);

        LineData lineData = new LineData(lastWeekDataSet, thisWeekDataSet);

        awesomenessChart.setData(lineData);
        awesomenessChart.invalidate();
    }

    private void applyLineDataSetStyle(LineDataSet lastWeekDataSet, @ColorRes int color, @ColorRes int highlightColor) {
        lastWeekDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lastWeekDataSet.setDrawCircleHole(true);
        lastWeekDataSet.setCircleHoleRadius(3f);
        lastWeekDataSet.setCircleColorHole(Color.WHITE);
        lastWeekDataSet.setCircleColor(ContextCompat.getColor(getContext(), color));
        lastWeekDataSet.setCircleRadius(6f);
        lastWeekDataSet.setLineWidth(3f);
        lastWeekDataSet.setHighLightColor(ContextCompat.getColor(getContext(), highlightColor));
        lastWeekDataSet.setDrawValues(false);
        lastWeekDataSet.setColor(ContextCompat.getColor(getContext(), color));
    }

    private void applyDefaultStyle(LineChart chart) {
        chart.setDrawBorders(false);
        chart.setExtraBottomOffset(8);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
//        xAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setYOffset(12f);
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(12f);
        yAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setXOffset(12f);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setYOffset(8f);
        legend.setTextSize(12f);
    }

    @OnClick(R.id.filter_completed_quests)
    public void onFilterCompletedQuestsClick(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.category_filter, (ViewGroup) getActivity().findViewById(R.id.filters_container));
        PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        // display the popup in the center
        pw.showAsDropDown(view);
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
}