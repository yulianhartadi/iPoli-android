package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.adapters.AgendaAdapter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class AgendaActivity extends BaseActivity implements CompactCalendarView.CompactCalendarViewListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.agenda_calendar)
    CompactCalendarView calendar;

    @BindView(R.id.agenda_list)
    RecyclerView questList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_agenda);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        LocalDate date = LocalDate.now();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        getSupportActionBar().setTitle(simpleDateFormat.format(date.toDate()));

        Calendar tDate = Calendar.getInstance();
        tDate.add(Calendar.DAY_OF_YEAR, -3);
        calendar.addEvent(new Event(ContextCompat.getColor(this, R.color.md_light_green_500), tDate.getTimeInMillis()), true);

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        calendar.addEvent(new Event(ContextCompat.getColor(this, R.color.md_orange_500), yesterday.getTimeInMillis()), true);
        calendar.addEvent(new Event(ContextCompat.getColor(this, R.color.md_orange_500), yesterday.getTimeInMillis()), true);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        calendar.addEvent(new Event(ContextCompat.getColor(this, R.color.md_red_500), tomorrow.getTimeInMillis()), true);
        calendar.addEvent(new Event(ContextCompat.getColor(this, R.color.md_red_500), tomorrow.getTimeInMillis()), true);
        calendar.addEvent(new Event(ContextCompat.getColor(this, R.color.md_red_500), tomorrow.getTimeInMillis()), true);

        calendar.setListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        questList.setLayoutManager(layoutManager);
        questList.setHasFixedSize(true);


        List<Quest> quests = new ArrayList<>();
        Quest q1 = new Quest("Clean up the fridge");
        q1.setStartTime(Time.at(9, 0));
        q1.setDuration(30);
        q1.setCategoryType(Category.CHORES);
        quests.add(q1);
        Quest q2 = new Quest("Study Deep Learning book");
        q2.setStartTime(Time.at(11, 30));
        q2.setDuration(120);
        q2.setCategoryType(Category.LEARNING);
        quests.add(q2);
        Quest q3 = new Quest("Hit the gym");
        q3.setCategoryType(Category.WELLNESS);
        q3.setStartTime(Time.at(20, 0));
        q3.setDuration(90);
        quests.add(q3);
        questList.setAdapter(new AgendaAdapter(quests));
    }

    private int getToolbarText(LocalDate date) {
        if (date.isEqual(new LocalDate().minusDays(1))) {
            return R.string.yesterday_calendar_format;
        }
        if (date.isEqual(new LocalDate())) {
            return R.string.today_calendar_format;
        }
        if (date.isEqual(new LocalDate().plusDays(1))) {
            return R.string.tomorrow_calendar_format;
        }
        return R.string.calendar_format;
    }

    @Override
    public void onDayClick(Date dateClicked) {
        LocalDate date = new LocalDate(dateClicked);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        getSupportActionBar().setTitle(simpleDateFormat.format(date.toDate()));
    }

    @Override
    public void onMonthScroll(Date firstDayOfNewMonth) {
        LocalDate date = new LocalDate(firstDayOfNewMonth);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        getSupportActionBar().setTitle(simpleDateFormat.format(date.toDate()));
    }
}
