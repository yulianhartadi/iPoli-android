package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.ui.events.CloseToolbarCalendarEvent;
import io.ipoli.android.app.ui.events.NewTitleEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class CalendarFragment extends Fragment implements CompactCalendarView.CompactCalendarViewListener {

    public static final int MID_POSITION = 49;
    public static final int MAX_VISIBLE_DAYS = 100;

    @BindView(R.id.calendar_pager)
    ViewPager calendarPager;

    @Inject
    Bus eventBus;

    private FragmentStatePagerAdapter adapter;
    private LocalDate currentMidDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        currentMidDate = new LocalDate();

        adapter = createAdapter();

        calendarPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                LocalDate date = currentMidDate.plusDays(position - MID_POSITION);
                changeTitle(date);
                eventBus.post(new CurrentDayChangedEvent(date, CurrentDayChangedEvent.Source.SWIPE));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        calendarPager.setAdapter(adapter);
        calendarPager.setCurrentItem(MID_POSITION);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calendar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_today) {
            eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.MENU));
            eventBus.post(new CloseToolbarCalendarEvent());
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void changeTitle(LocalDate date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        eventBus.post(new NewTitleEvent(simpleDateFormat.format(date.toDate())));
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

    @Subscribe
    public void onCurrentDayChanged(CurrentDayChangedEvent e) {

        if (e.source == CurrentDayChangedEvent.Source.SWIPE) {
            return;
        }

        currentMidDate = e.date;
        changeTitle(currentMidDate);
        adapter.notifyDataSetChanged();
        calendarPager.setCurrentItem(MID_POSITION, false);
    }

    private FragmentStatePagerAdapter createAdapter() {
        return new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return DayViewFragment.newInstance(currentMidDate.plusDays(position - MID_POSITION));
            }

            @Override
            public int getCount() {
                return MAX_VISIBLE_DAYS;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };
    }

    @Override
    public void onDayClick(Date dateClicked) {
        eventBus.post(new CurrentDayChangedEvent(new LocalDate(dateClicked), CurrentDayChangedEvent.Source.CALENDAR));
        eventBus.post(new CloseToolbarCalendarEvent());
    }

    @Override
    public void onMonthScroll(Date firstDayOfNewMonth) {
        eventBus.post(new CurrentDayChangedEvent(new LocalDate(firstDayOfNewMonth), CurrentDayChangedEvent.Source.CALENDAR));
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        if (e.quest.getEndDate().after(new Date()) && (e.source == EventSource.CALENDAR_DAY_VIEW || e.source == EventSource.CALENDAR_UNSCHEDULED_SECTION)) {
            eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.CALENDAR));
        }
    }
}