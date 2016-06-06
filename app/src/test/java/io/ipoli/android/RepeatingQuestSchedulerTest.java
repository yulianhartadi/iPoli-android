package io.ipoli.android;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;

import java.util.List;

import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/6/16.
 */
public class RepeatingQuestSchedulerTest {

    private static RepeatingQuestScheduler repeatingQuestScheduler;

    @BeforeClass
    public static void setUp() {
        repeatingQuestScheduler = new RepeatingQuestScheduler();
    }

    @Test
    public void createRepeatingQuest() {
        RepeatingQuest repeatingQuest = new RepeatingQuest("Test");
        RepeatingQuest.setContext(repeatingQuest, QuestContext.CHORES);
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(LocalDate.now().toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate());
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, new LocalDate());
        for (Quest q : result) {
            System.out.println(q.getEndDate());
//            System.out.println(LocalDate.now().toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate());
        }

        assertThat(result.size(), is(3));
    }
}
