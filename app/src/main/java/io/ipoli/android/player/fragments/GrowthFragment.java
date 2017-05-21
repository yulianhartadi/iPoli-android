package io.ipoli.android.player.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
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

    @BindView(R.id.awesomeness_vs_week)
    TextView awesomenessVsWeek;

    @BindView(R.id.awesomeness_vs_month)
    TextView awesomenessVsMonth;

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
//        List<Entry> totalEntries = new ArrayList<>();
//        totalEntries.add(new Entry(1, 12, R.color.colorAccentDark));
//        totalEntries.add(new Entry(2, 24, R.color.colorAccentDark));
//        totalEntries.add(new Entry(3, 19, R.color.colorAccentDark));
//        totalEntries.add(new Entry(4, 23, R.color.colorAccentDark));
//        totalEntries.add(new Entry(5, 12, R.color.colorAccentDark));
//        totalEntries.add(new Entry(6, 4, R.color.colorAccentDark));
//        totalEntries.add(new Entry(7, 5, R.color.colorAccentDark));
//        LineDataSet totalDataSet = new LineDataSet(totalEntries, "Total");
//        totalDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        totalDataSet.setCircleRadius(4);
//        totalDataSet.setDrawCircleHole(false);
//        totalDataSet.setDrawValues(false);
//        totalDataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//        totalDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorAccentDark));
//        totalDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.colorAccentDark));

        List<Entry> wellnessEntries = new ArrayList<>();
        wellnessEntries.add(new Entry(1, 4, R.color.md_green_700));
        wellnessEntries.add(new Entry(2, 9, R.color.md_green_700));
        wellnessEntries.add(new Entry(3, 12, R.color.md_green_700));
        wellnessEntries.add(new Entry(4, 4, R.color.md_green_700));
        wellnessEntries.add(new Entry(5, 8, R.color.md_green_700));
        wellnessEntries.add(new Entry(6, 1, R.color.md_green_700));
        wellnessEntries.add(new Entry(7, 3, R.color.md_green_700));
        LineDataSet wellnessDataSet = new LineDataSet(wellnessEntries, "Wellness");
        wellnessDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        wellnessDataSet.setCircleRadius(4);
        wellnessDataSet.setDrawCircleHole(false);
        wellnessDataSet.setDrawValues(false);
        wellnessDataSet.setLineWidth(1.5f);
        wellnessDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        wellnessDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_green_700));
        wellnessDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_green_700));
        wellnessDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_green_300));
        wellnessDataSet.setDrawFilled(true);

        List<Entry> learningEntries = new ArrayList<>();
        learningEntries.add(new Entry(1, 2, R.color.md_blue_700));
        learningEntries.add(new Entry(2, 4, R.color.md_blue_700));
        learningEntries.add(new Entry(3, 5, R.color.md_blue_700));
        learningEntries.add(new Entry(4, 10, R.color.md_blue_700));
        learningEntries.add(new Entry(5, 3, R.color.md_blue_700));
        learningEntries.add(new Entry(6, 1, R.color.md_blue_700));
        learningEntries.add(new Entry(7, 1, R.color.md_blue_700));
        LineDataSet learningDataSet = new LineDataSet(learningEntries, "Learning");
        learningDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        learningDataSet.setCircleRadius(4);
        learningDataSet.setDrawCircleHole(false);
//        learningDataSet.setDrawCircles(false);
        learningDataSet.setLineWidth(1.5f);
        learningDataSet.setDrawValues(false);
        learningDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_blue_500));
        learningDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_blue_700));
        learningDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_blue_700));
        learningDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_blue_300));
        learningDataSet.setDrawFilled(true);

        List<Entry> workEntries = new ArrayList<>();
        workEntries.add(new Entry(1, 1, R.color.md_red_700));
        workEntries.add(new Entry(2, 4, R.color.md_red_700));
        workEntries.add(new Entry(3, 7, R.color.md_red_700));
        workEntries.add(new Entry(4, 12, R.color.md_red_700));
        workEntries.add(new Entry(5, 4, R.color.md_red_700));
        workEntries.add(new Entry(6, 7, R.color.md_red_700));
        workEntries.add(new Entry(7, 9, R.color.md_red_700));
        LineDataSet workDataSet = new LineDataSet(workEntries, "Work");
        workDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        workDataSet.setCircleRadius(4);
        workDataSet.setDrawCircleHole(false);
//        workDataSet.setDrawCircles(false);
        workDataSet.setLineWidth(1.5f);
        workDataSet.setDrawValues(false);
        workDataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_red_500));
        workDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_red_700));
        workDataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_red_700));
        workDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_red_300));
        workDataSet.setDrawFilled(true);

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

        SpannableString lastWeekSpan = new SpannableString("+18%\nvs\nlast week");
        lastWeekSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.md_green_500)), 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        lastWeekSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        lastWeekSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.md_dark_text_54)), 5, lastWeekSpan.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        awesomenessVsWeek.setText(lastWeekSpan);

        SpannableString lastMonthSpan = new SpannableString("-35%\nvs\nlast month");
        lastMonthSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.md_red_500)), 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        lastMonthSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        lastMonthSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.md_dark_text_54)), 5, lastMonthSpan.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        awesomenessVsMonth.setText(lastMonthSpan);

        applyDefaultStyle(awesomenessChart);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 12, R.color.md_green_700));
        entries.add(new Entry(2, 24, R.color.md_green_700));
        entries.add(new Entry(3, 38, R.color.md_green_700));
        entries.add(new Entry(4, 55, R.color.md_green_700));
        entries.add(new Entry(5, 74, R.color.md_green_700));
        entries.add(new Entry(6, 80, R.color.md_green_700));
        entries.add(new Entry(7, 85, R.color.md_green_700));
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.md_green_700));
        dataSet.setDrawFilled(true);
        dataSet.setLineWidth(1.5f);
        dataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.md_green_700));
        dataSet.setDrawValues(false);

        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.md_green_500));
        dataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.md_green_300));

        LineData lineData = new LineData(dataSet);
        awesomenessChart.setDescription(null);
        awesomenessChart.getLegend().setEnabled(false);
        awesomenessChart.setDrawBorders(false);
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


        xAxis.setLabelCount(entries.size(), true);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(6, true);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext());
        awesomenessChart.setMarker(customMarkerView);
        awesomenessChart.setData(lineData);
        awesomenessChart.invalidate();
    }

    private void applyDefaultStyle(LineChart chart) {
        chart.setDrawBorders(false);
        chart.setExtraBottomOffset(8);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setTextSize(12);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xAxis.setYOffset(12);
        xAxis.setLabelRotationAngle(330);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(12);
        yAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        yAxis.setXOffset(12);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);
        chart.getAxisRight().setEnabled(false);
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