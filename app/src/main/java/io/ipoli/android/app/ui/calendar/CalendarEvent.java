package io.ipoli.android.app.ui.calendar;

import android.support.annotation.ColorRes;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public interface CalendarEvent {
    Date getStartTime();

    int getDuration();

    void setStartTime(Date startTime);

    @ColorRes
    int getBackgroundColor();

    String getName();

}
