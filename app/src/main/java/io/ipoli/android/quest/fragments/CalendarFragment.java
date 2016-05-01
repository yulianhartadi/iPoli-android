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

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.ui.events.NewTitleEvent;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class CalendarFragment extends Fragment {

    public static final int MID_POSITION = 49;
    public static final int MAX_VISIBLE_DAYS = 100;

    @Bind(R.id.calendar_pager)
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

        adapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return DayViewFragment.newInstance(currentMidDate.plusDays(position - MID_POSITION));
            }

            @Override
            public int getCount() {
                return MAX_VISIBLE_DAYS;
            }
        };

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

    public void scrollToTodayQuest(Quest quest) {
        currentMidDate = new LocalDate();
        calendarPager.setCurrentItem(MID_POSITION);
        ((DayViewFragment) adapter.getItem(calendarPager.getCurrentItem())).scrollToQuest(quest);
    }

    @Subscribe
    public void onCurrentDayChanged(CurrentDayChangedEvent e) {

        if (e.source == CurrentDayChangedEvent.Source.SWIPE) {
            return;
        }

        currentMidDate = e.date;
        changeTitle(currentMidDate);
        calendarPager.setAdapter(adapter);
        calendarPager.setCurrentItem(MID_POSITION);
    }
}