package io.ipoli.android.mcalendar.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import io.ipoli.android.mcalendar.MCalendarView;
import io.ipoli.android.mcalendar.fragments.MonthFragment;
import io.ipoli.android.mcalendar.utils.CalendarUtil;
import io.ipoli.android.mcalendar.vo.DateData;
import io.ipoli.android.mcalendar.vo.MonthData;

/**
 * Created by bob.sun on 15/8/27.
 */
public class CalendarViewAdapter extends FragmentStatePagerAdapter {

    private DateData date;

    private int dateCellId;
    private int markCellId;
    private boolean hasTitle = true;

    private Context context;
    private int mCurrentPosition = -1;

    public CalendarViewAdapter(FragmentManager fm) {
        super(fm);
    }

    public CalendarViewAdapter setDate(DateData date){
        this.date = date;
        return this;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public CalendarViewAdapter setDateCellId(int dateCellRes){
        this.dateCellId =  dateCellRes;
        return this;
    }


    public CalendarViewAdapter setMarkCellId(int markCellId){
        this.markCellId = markCellId;
        return this;
    }


    @Override
    public Fragment getItem(int position) {
        int year = CalendarUtil.position2Year(position);
        int month = CalendarUtil.position2Month(position);

        MonthFragment fragment = new MonthFragment();
        fragment.setTitle(hasTitle);
        MonthData monthData = new MonthData(new DateData(year, month, month / 2), hasTitle);
        fragment.setData(monthData, dateCellId, markCellId);
        return fragment;
    }
    @Override
    public int getCount() {
        return 1000;
    }

    public CalendarViewAdapter setTitle(boolean hasTitle){
        this.hasTitle = hasTitle;
        return this;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        ((MCalendarView) container).measureCurrentView(position);
    }
}
