package io.ipoli.android.mcalendar.vo;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Calendar;

import io.ipoli.android.mcalendar.CellConfig;

/**
 * Created by Bigflower on 2015/12/8.
 */
public class MonthWeekData {
    private DateData pointDate;
    private Calendar calendar;

    private int realPosition;
    private int weekIndex, preNumber, afterNumber;

    private ArrayList<DayData> monthContent;
    private ArrayList<DayData> weekContent;

    /**
     * 绝对位置
     *
     * @param position
     */
    public MonthWeekData(int position) {
        realPosition = position;
        calendar = Calendar.getInstance();
        if (CellConfig.m2wPointDate == null) {
            CellConfig.m2wPointDate = getToday();
        }
        if (CellConfig.w2mPointDate == null) {
            CellConfig.w2mPointDate = getToday();
        }
        if (CellConfig.weekAnchorPointDate == null) {
            CellConfig.weekAnchorPointDate = getToday();
        }


        if (CellConfig.ifMonth) {
            getPointDate();
            initMonthArray();
        } else {
            initWeekArray();
        }
    }

    private void getPointDate() {
        calendar.set(CellConfig.w2mPointDate.getYear(), CellConfig.w2mPointDate.getMonth() - 1, CellConfig.w2mPointDate.getDay());
        int distance = CellConfig.Week2MonthPos - CellConfig.Month2WeekPos;
        calendar.add(Calendar.DATE, distance * 7);
        if (realPosition == CellConfig.middlePosition) {
            CellConfig.m2wPointDate = new DateData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            calendar.add(Calendar.MONTH, realPosition - CellConfig.Week2MonthPos);
        }
        calendar.set(Calendar.DATE, 1);
    }

    /**
     * 这个表是确定的6行，所以共有42个数字
     */
    private void initMonthParams() {
        weekIndex = calendar.get(Calendar.DAY_OF_WEEK);
        //我擦 败家的11月有问题，获得星期不对，拟合一下 TODO
//        if (calendar.get(Calendar.MONTH) == 11)
//            weekIndex--;
        preNumber = weekIndex - 1;
        afterNumber = 42 - calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - preNumber;
    }

    private void initMonthArray() {
        DayData addDate;
        monthContent = new ArrayList<DayData>();

        initMonthParams();

        // 本月前面的 上个月的灰色的日期
        calendar.add(Calendar.MONTH, -1);
        int lastMonthDayNumber = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int preDay = lastMonthDayNumber - preNumber + 1; preDay < lastMonthDayNumber + 1; preDay++) {
            addDate = new DayData(new DateData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, preDay));
            addDate.setTextColor(Color.LTGRAY);
            monthContent.add(addDate);
        }

        // 本月的 日期
        calendar.add(Calendar.MONTH, 1);
        int thisMonthDayNumber = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day < thisMonthDayNumber + 1; day++) {
            addDate = new DayData(new DateData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, day));
            addDate.setTextColor(Color.BLACK);
            monthContent.add(addDate);
        }

        // 本月的后面 下个月的灰色的日期
        afterNumber = afterNumber + 1;
        calendar.add(Calendar.MONTH, 1);
        for (int afterDay = 1; afterDay < afterNumber; afterDay++) {
            addDate = new DayData(new DateData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, afterDay));
            addDate.setTextColor(Color.LTGRAY);
            monthContent.add(addDate);
        }
        calendar.add(Calendar.MONTH, -1);
    }

    private void thisMonthArray() {

    }

    private void otherMonthArray() {

    }

    /**
     * Week2MonthPos 与 Month2WeekPos 是关键。
     */
    private void initWeekArray() {
        weekContent = new ArrayList<DayData>();

        calendar.set(CellConfig.m2wPointDate.getYear(), CellConfig.m2wPointDate.getMonth() - 1, CellConfig.m2wPointDate.getDay());
        if (CellConfig.Week2MonthPos != CellConfig.Month2WeekPos) {
            int distance = CellConfig.Month2WeekPos - CellConfig.Week2MonthPos;
            calendar.add(Calendar.MONTH, distance);
        }
        calendar.set(Calendar.DAY_OF_MONTH, getAnchorDayOfMonth(CellConfig.weekAnchorPointDate));
        if (realPosition == CellConfig.Month2WeekPos) {
            ;
        } else {
            calendar.add(Calendar.DATE, (realPosition - CellConfig.Month2WeekPos) * 7);
        }

        if (realPosition == CellConfig.middlePosition) {
            CellConfig.w2mPointDate = new DateData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        }

        DayData addDate;
        weekIndex = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DATE, -weekIndex + 1);
        for (int i = 0; i < 7; i++) {
            addDate = new DayData(new DateData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
            weekContent.add(addDate);
            calendar.add(Calendar.DATE, 1);
        }
    }

    private int getAnchorDayOfMonth(DateData date) {
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH);
        int month = date.getMonth() - 1;
        int selectedMonth = calendar.get(Calendar.MONTH);
        if (selectedMonth == month && calendar.get(Calendar.YEAR) == date.getYear()) {
            return date.getDay();
        }

        if (selectedMonth == thisMonth && month != thisMonth) {
            return getToday().getDay();
        }

        return 1;
    }

    private DateData getToday() {
        Calendar calendar = Calendar.getInstance();
        return new DateData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    public ArrayList getData() {
        if (CellConfig.ifMonth)
            return monthContent;
        else
            return weekContent;
    }

}
