package io.ipoli.android.quest.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

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
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmRepeatingQuestPersistenceService;
import io.ipoli.android.quest.ui.formatters.DateFormatter;

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

    @BindView(R.id.quest_category_image)
    ImageView categoryImage;

    @BindView(R.id.quest_category_name)
    TextView categoryName;

    @BindView(R.id.quest_next_scheduled_date)
    TextView nextScheduledDate;

    private RepeatingQuest repeatingQuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || StringUtils.isEmpty(getIntent().getStringExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY))) {
            finish();
            return;
        }

        RealmRepeatingQuestPersistenceService repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, getRealm());

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

    private void displayRepeatingQuest() {
        name.setText(repeatingQuest.getName());

        LayoutInflater inflater = LayoutInflater.from(this);
        int completed = 3;
        int incomplete = 2;

        int progressColor = R.color.colorAccent;
        Category category = RepeatingQuest.getCategory(repeatingQuest);
        if (category == Category.WORK || category == Category.FUN || category == Category.CHORES) {
            progressColor = R.color.colorAccentAlternative;
        }

        for (int i = 1; i <= completed; i++) {
            View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_indicator_empty, progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();

//            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1.5f, getResources()), Color.WHITE);
            progressViewEmptyBackground.setColor(ContextCompat.getColor(this, progressColor));
            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1.5f, getResources()), ContextCompat.getColor(this, progressColor));
            progressContainer.addView(progressViewEmpty);
        }

        for (int i = 1; i <= incomplete; i++) {
            View progressViewEmpty = inflater.inflate(R.layout.repeating_quest_progress_indicator_empty, progressContainer, false);
            GradientDrawable progressViewEmptyBackground = (GradientDrawable) progressViewEmpty.getBackground();
            progressViewEmptyBackground.setStroke((int) ViewUtils.dpToPx(1.5f, getResources()), Color.WHITE);
            progressViewEmptyBackground.setColor(Color.WHITE);
            progressContainer.addView(progressViewEmpty);
        }

        categoryName.setText(StringUtils.capitalize(category.name()));
        categoryImage.setImageResource(category.whiteImage);

        RealmQuestPersistenceService questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());
        Date nextDate = questPersistenceService.findNextUncompletedQuestEndDate(repeatingQuest);
        nextScheduledDate.setText(DateFormatter.formatWithoutYear(nextDate));

        colorLayout(category);
        setupChart();
    }

    private void setupChart() {
        history.setDescription("");
        history.setTouchEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        history.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        history.setPinchZoom(false);

        history.setDrawGridBackground(false);
        history.setDrawBarShadow(true);

        history.setDrawValueAboveBar(false);
        history.setDrawGridBackground(false);

        // change the position of the y-labels
        YAxis leftAxis = history.getAxisLeft();
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaxValue(5);
        leftAxis.setEnabled(false);
        history.getAxisRight().setEnabled(false);

        XAxis xLabels = history.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_54));
        xLabels.setTextSize(12f);
        xLabels.setDrawAxisLine(false);
        xLabels.setDrawGridLines(false);
//        history.getXAxis().setEnabled(false);
        history.getLegend().setEnabled(false);

        setData(4);
    }

    private void colorLayout(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.resLightColor));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.resDarkColor));
    }

    private void setData(int count) {

        ArrayList<String> xVals = new ArrayList<>();
        xVals.add("8 - 15 Jun");
        xVals.add("16 - 23 Jun");
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
        set1.setBarShadowColor(ContextCompat.getColor(this, RepeatingQuest.getCategory(repeatingQuest).color100));

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.valueOf((int) value));

        history.setData(data);
        history.animateY(500);
    }

    private int[] getColors() {

        int stacksize = 5;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        Category category = RepeatingQuest.getCategory(repeatingQuest);
        for (int i = 0; i < stacksize; i++) {
            colors[i] = ContextCompat.getColor(this, category.color300);
        }

        return colors;
    }

}