package io.ipoli.android.app.ui.calendar;

import android.support.annotation.ColorRes;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public interface CalendarEvent {
    Integer getStartMinute();

    int getDuration();

    void setStartMinute(Integer startMinute);

    @ColorRes
    int getBackgroundColor();

    @ColorRes
    int getDragBackgroundColor();

    boolean isRepeating();

    String getName();

    boolean isMostImportant();

    boolean isForChallenge();
}
