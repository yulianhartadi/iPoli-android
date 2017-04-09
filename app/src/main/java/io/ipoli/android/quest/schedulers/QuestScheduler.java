package io.ipoli.android.quest.schedulers;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

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
        LocalDate start = quest.getStartDate();
        LocalDate end =  quest.getEndDate();
        int daysBetween = (int) ChronoUnit.DAYS.between(start, end);
        int dayOffset = new Random().nextInt(daysBetween + 1);
        return DateUtils.toStartOfDayUTC(start.plusDays(dayOffset));
    }
}
