package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class CalendarFragment extends Fragment {

    public static final int TODAY_POSITION = 49;
    public static final int MAX_VISIBLE_DAYS = 100;

    @Bind(R.id.calendar_pager)
    ViewPager calendarPager;

    private FragmentStatePagerAdapter adapter;
    private LocalDate currentDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        currentDate = new LocalDate();

        adapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return DayViewFragment.newInstance(currentDate.plusDays(position - TODAY_POSITION));
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
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    LocalDate date = currentDate.plusDays(position - TODAY_POSITION);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
                    actionBar.setTitle(simpleDateFormat.format(date.toDate()));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.today_calendar_format), Locale.getDefault());
            actionBar.setTitle(simpleDateFormat.format(currentDate.toDate()));
        }

        calendarPager.setAdapter(adapter);
        calendarPager.setCurrentItem(TODAY_POSITION);

        return view;
    }

    private int getToolbarText(LocalDate date) {
        int text = R.string.calendar_format;
        if (date.isEqual(new LocalDate())) {
            text = R.string.today_calendar_format;
        }
        if (date.isEqual(new LocalDate().plusDays(1))) {
            text = R.string.tomorrow_calendar_format;
        }
        return text;
    }

    public void scrollToTodayQuest(Quest quest) {
        currentDate = new LocalDate();
        calendarPager.setCurrentItem(TODAY_POSITION);
        ((DayViewFragment) adapter.getItem(calendarPager.getCurrentItem())).scrollToQuest(quest);
    }
}