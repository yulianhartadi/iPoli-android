package com.curiousily.ipoli.schedule.ui.dayview;

import java.util.Calendar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/30/15.
 */
public interface DateTimeInterpreter {
    String interpretDate(Calendar date);
    String interpretTime(int hour, int minutes);
}