package io.ipoli.android.quest.schedulers;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Date;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.DateList;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.DateTime;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.parameter.Value;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/6/16.
 */
public class RepeatingQuestScheduler {
    public List<Quest> schedule(RepeatingQuest repeatingQuest, LocalDate date) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        String rruleStr = recurrence.getRrule();
        if (rruleStr != null && !rruleStr.isEmpty()) {
            Recur recur;
            try {
                recur = new Recur(rruleStr);
            } catch (ParseException e) {
                return new ArrayList<>();
            }
            java.util.Date endOfWeek = LocalDate.now().dayOfWeek().withMaximumValue().toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
            DateList dates = recur.getDates(new Date(recurrence.getDtstart()), new Date(recurrence.getDtstart()), new DateTime(endOfWeek), Value.DATE);
            List<Quest> res = new ArrayList<>();
            for (Object obj : dates) {
                res.add(createQuest(repeatingQuest, (Date) obj));
            }
            return res;
        }
        return new ArrayList<>();
    }

    private Quest createQuest(RepeatingQuest repeatingQuest, Date endDate) {
        Quest quest = new Quest();
        quest.setName(repeatingQuest.getName());
        quest.setContext(repeatingQuest.getContext());
        quest.setStartMinute(repeatingQuest.getStartMinute());
        quest.setEndDate(endDate);
        quest.setStartDate(endDate);

        quest.setId(IDGenerator.generate());
        quest.setCreatedAt(DateUtils.nowUTC());
        quest.setUpdatedAt(DateUtils.nowUTC());
        quest.setFlexibleStartTime(false);
        quest.setNeedsSync();
        quest.setSource(Constants.API_RESOURCE_SOURCE);
        quest.setExperience(new ExperienceRewardGenerator().generate(quest));
        quest.setCoins(new CoinsRewardGenerator().generate(quest));
        return quest;
    }
}