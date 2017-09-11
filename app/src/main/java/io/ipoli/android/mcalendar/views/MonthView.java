package io.ipoli.android.mcalendar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import io.ipoli.android.mcalendar.adapters.CalendarAdapter;
import io.ipoli.android.mcalendar.vo.MonthData;

/**
 * Created by bob.sun on 15/8/27.
 */
public class MonthView extends GridView {
    private MonthData monthData;
    private CalendarAdapter adapter;

    public MonthView(Context context) {
        super(context);
        this.setNumColumns(7);
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setNumColumns(7);
    }

    /**
     * @deprecated
     * @param monthData
     * @return
     */
    public MonthView displayMonth(MonthData monthData){
        adapter = new CalendarAdapter(getContext(), 1, monthData.getData());
        return this;
    }

}
