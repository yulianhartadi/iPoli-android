package io.ipoli.android.quest.schedulers;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.Random;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/15/17.
 */

public class QuestScheduler {

    public Date schedule(Quest quest) {
        LocalDate start = new LocalDate(quest.getStart());
        LocalDate end = new LocalDate(quest.getEnd());
        int daysBetween = Days.daysBetween(start, end).getDays();
        int dayOffset = new Random().nextInt(daysBetween + 1);
        return DateUtils.toStartOfDayUTC(start.plusDays(dayOffset));
    }
}
