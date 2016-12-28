package io.ipoli.android.app.ui.calendar;

import android.support.annotation.ColorRes;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public interface CalendarEvent {
    int getStartMinute();

    int getDuration();

    void setStartMinute(int startMinute);

    @ColorRes
    int getBackgroundColor();

    boolean isRepeating();

    String getName();

    boolean isMostImportant();

    boolean isForChallenge();
}
