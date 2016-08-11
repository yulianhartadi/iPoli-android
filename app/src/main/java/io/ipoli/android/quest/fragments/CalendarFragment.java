package io.ipoli.android.quest.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import butterknife.OnClick;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.events.ToolbarCalendarTapEvent;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.events.AddQuestButtonTappedEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class CalendarFragment extends BaseFragment implements CompactCalendarView.CompactCalendarViewListener, View.OnClickListener {

    public static final int MID_POSITION = 49;
    public static final int MAX_VISIBLE_DAYS = 100;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.toolbar_expand_container)
    View toolbarExpandContainer;

    @BindView(R.id.toolbar_calendar)
    CompactCalendarView toolbarCalendar;


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_calendar_indicator)
    ImageView calendarIndicator;

    @BindView(R.id.calendar_pager)
    ViewPager calendarPager;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @Inject
    Bus eventBus;

    private FragmentStatePagerAdapter adapter;
    private LocalDate currentMidDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ((MainActivity) getActivity()).actionBarDrawerToggle.syncState();

        toolbarCalendar.setCurrentDate(new Date());

        toolbarExpandContainer.setOnClickListener(this);

        toolbarCalendar.setListener(this);

        appBar.setExpanded(false, false);
        appBar.setTag(false);

        currentMidDate = new LocalDate();

        changeTitle(currentMidDate);

        adapter = createAdapter();

        calendarPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                LocalDate date = currentMidDate.plusDays(position - MID_POSITION);
                changeTitle(date);
                toolbarCalendar.setCurrentDate(date.toDate());
                eventBus.post(new CurrentDayChangedEvent(date, CurrentDayChangedEvent.Source.SWIPE));
            }
        });

        calendarPager.setAdapter(adapter);
        calendarPager.setCurrentItem(MID_POSITION);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.MENU));
                closeToolbarCalendar();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calendar_menu, menu);
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    public void closeToolbarCalendar() {
        boolean isExpanded = (boolean) appBar.getTag();
        if (isExpanded) {
            calendarIndicator.animate().rotation(0).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            appBar.setExpanded(false, true);
        }
        appBar.setTag(false);
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

    @OnClick(R.id.add_quest)
    public void onAddQuest(View view) {
        eventBus.post(new AddQuestButtonTappedEvent(EventSource.CALENDAR));
        startActivity(new Intent(getActivity(), EditQuestActivity.class));
    }

    private void changeTitle(LocalDate date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        toolbarTitle.setText(simpleDateFormat.format(date.toDate()));
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
        toolbarCalendar.setCurrentDate(currentMidDate.toDate());

        calendarPager.setCurrentItem(MID_POSITION, false);
    }

    private FragmentStatePagerAdapter createAdapter() {
        return new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                int plusDays = position - MID_POSITION;
                return DayViewFragment.newInstance(currentMidDate.plusDays(plusDays));
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
        closeToolbarCalendar();
    }

    @Override
    public void onMonthScroll(Date firstDayOfNewMonth) {
        eventBus.post(new CurrentDayChangedEvent(new LocalDate(firstDayOfNewMonth), CurrentDayChangedEvent.Source.CALENDAR));
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        if (new LocalDate(e.quest.getEndDate()).isAfter(new LocalDate()) && (e.source == EventSource.CALENDAR_DAY_VIEW || e.source == EventSource.CALENDAR_UNSCHEDULED_SECTION)) {
            eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.CALENDAR));
        }
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_calendar, R.string.help_dialog_calendar_title, "calendar").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onClick(View v) {
        boolean isExpanded = (boolean) appBar.getTag();
        calendarIndicator.animate().rotation(isExpanded ? 0 : 180).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        appBar.setExpanded(!isExpanded, true);
        appBar.setTag(!isExpanded);
        eventBus.post(new ToolbarCalendarTapEvent(!isExpanded));
    }
}