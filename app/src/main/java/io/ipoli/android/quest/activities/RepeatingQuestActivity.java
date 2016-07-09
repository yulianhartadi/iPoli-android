package io.ipoli.android.quest.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.persistence.RealmRepeatingQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/9/16.
 */
public class RepeatingQuestActivity extends BaseActivity {

    private RealmRepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @BindView(R.id.repeating_quest_progress_container)
    ViewGroup progressContainer;

    @BindView(R.id.repeating_quest_history)
    BarChart mChart;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getIntent() == null || TextUtils.isEmpty(getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY))) {
//            finish();
//            return;
//        }
        setContentView(R.layout.activity_repeating_quest);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, getRealm());

        String repeatingQuestId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        eventBus.post(new ScreenShownEvent(EventSource.REPEATING_QUEST));

        LayoutInflater inflater = LayoutInflater.from(this);
        int completed = 3;
        int incomplete = 2;
        for (int i = 1; i <= completed; i++) {
            View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_indicator_empty, progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();

            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1.5f, getResources()), Color.WHITE);
            progressViewEmptyBackground.setColor(Color.WHITE);
            progressContainer.addView(progressViewEmpty);
        }

        for (int i = 1; i <= incomplete; i++) {
            View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_indicator_empty, progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();
            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1.5f, getResources()), Color.WHITE);
            progressViewEmptyBackground.setColor(Color.TRANSPARENT);
            progressContainer.addView(progressViewEmpty);
        }


        mChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(true);

        mChart.setDrawValueAboveBar(false);
        mChart.setDrawGridBackground(false);

        // change the position of the y-labels
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaxValue(5);
        leftAxis.setEnabled(false);
        mChart.getAxisRight().setEnabled(false);

        XAxis xLabels = mChart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_54));
        xLabels.setTextSize(12f);
        xLabels.setDrawAxisLine(false);
        xLabels.setDrawGridLines(false);
//        mChart.getXAxis().setEnabled(false);
        mChart.getLegend().setEnabled(false);

        setData(4);

    }

    private void setData(int count) {

        ArrayList<String> xVals = new ArrayList<>();
//        for (int i = 0; i < count; i++) {
//            xVals.add(mMonths[i % 12]);
//        }
        xVals.add("8-15 Jun");
        xVals.add("16-23 Jun");
        xVals.add("last week");
        xVals.add("this week");

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            yVals1.add(new BarEntry(Math.max(3, new Random().nextInt(5) + 1), i));
        }

        BarDataSet set1;

        set1 = new BarDataSet(yVals1, "DataSet");
//        set1.setBarSpacePercent(35f);
        set1.setColors(getColors());
        set1.setBarShadowColor(ContextCompat.getColor(this, R.color.md_blue_100));

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) value);
            }
        });

        mChart.setData(data);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        mChart.animateY(500);
    }

    private int[] getColors() {

        int stacksize = 5;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        for (int i = 0; i < stacksize; i++) {
            colors[i] = ContextCompat.getColor(this, R.color.md_blue_300);
        }

        return colors;
    }

}