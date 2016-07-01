package io.ipoli.android.quest.schedulers;

import android.support.annotation.NonNull;

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
import io.ipoli.android.quest.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.realm.RealmList;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/6/16.
 */
public class RepeatingQuestScheduler {

    public List<Quest> schedule(RepeatingQuest repeatingQuest, java.util.Date startDate) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        String rruleStr = recurrence.getRrule();
        List<Quest> res = new ArrayList<>();
        if (rruleStr == null || rruleStr.isEmpty()) {
            return res;
        }
        Recur recur;
        try {
            recur = new Recur(rruleStr);
        } catch (ParseException e) {
            return res;
        }
        if (recurrence.getDtend() != null) {
            recur.setUntil(new Date(recurrence.getDtend()));
        }

        if (recur.getFrequency().equals(Recur.YEARLY)) {
            return res;
        }

        java.util.Date endDate = getEndDate(recur, startDate);
        DateList dates = recur.getDates(new Date(startDate), new Date(recurrence.getDtstart()),
                getPeriodEnd(endDate), Value.DATE);

        for (Object obj : dates) {
            for (int i = 0; i < recurrence.getTimesPerDay(); i++) {
                res.add(createQuestFromRepeating(repeatingQuest, (Date) obj));
            }
        }
        return res;
    }

    @NonNull
    private DateTime getPeriodEnd(java.util.Date endDate) {
        return new DateTime(toStartOfDayUTC(new LocalDate(endDate.getTime(), DateTimeZone.UTC).plusDays(1)));
    }

    public Quest createQuestFromRepeating(RepeatingQuest repeatingQuest, java.util.Date endDate) {
        Quest quest = new Quest();
        quest.setName(repeatingQuest.getName());
        quest.setCategory(repeatingQuest.getCategory());
        quest.setDuration(repeatingQuest.getDuration());
        quest.setStartMinute(repeatingQuest.getStartMinute());
        quest.setEndDate(endDate);
        quest.setStartDate(endDate);
        quest.setOriginalStartDate(endDate);
        quest.setId(IDGenerator.generate());
        quest.setCreatedAt(DateUtils.nowUTC());
        quest.setUpdatedAt(DateUtils.nowUTC());
        quest.setFlexibleStartTime(false);
        quest.setNeedsSync();
        quest.setSource(Constants.API_RESOURCE_SOURCE);
        quest.setExperience(new ExperienceRewardGenerator().generate(quest));
        quest.setCoins(new CoinsRewardGenerator().generate(quest));
        quest.setChallenge(repeatingQuest.getChallenge());
        quest.setRepeatingQuest(repeatingQuest);
        RealmList<Reminder> questReminders = new RealmList<>();
        for (Reminder r : repeatingQuest.getReminders()) {
            Reminder questReminder = new Reminder(r.getMinutesFromStart(), r.getNotificationId());
            questReminder.setMessage(r.getMessage());
            questReminders.add(questReminder);
        }
        quest.setReminders(questReminders);
        quest.updateRemindersStartTime();
        return quest;
    }

    private java.util.Date getEndDate(Recur recur, java.util.Date startDate) {
        String frequency = recur.getFrequency();
        LocalDate localStartDate = new LocalDate(startDate.getTime(), DateTimeZone.UTC);
        if (frequency.equals(Recur.WEEKLY)) {
            return toStartOfDayUTC(localStartDate.dayOfWeek().withMaximumValue());
        }
        if (frequency.equals(Recur.MONTHLY)) {
            return toStartOfDayUTC(localStartDate.dayOfMonth().withMaximumValue());
        }
        return toStartOfDayUTC(localStartDate.dayOfYear().withMaximumValue());
    }

    public List<Quest> scheduleForDateRange(RepeatingQuest repeatingQuest, java.util.Date from, java.util.Date to) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        String rruleStr = recurrence.getRrule();
        List<Quest> res = new ArrayList<>();
        if (rruleStr == null || rruleStr.isEmpty()) {
            return res;
        }
        Recur recur;
        try {
            recur = new Recur(rruleStr);
        } catch (ParseException e) {
            return res;
        }
        if (recur.getFrequency().equals(Recur.YEARLY)) {
            return res;
        }
        DateList dates = recur.getDates(new Date(recurrence.getDtstart()),
                new Date(from),
                getPeriodEnd(to), Value.DATE);


        for (Object obj : dates) {
            res.add(createQuestFromRepeating(repeatingQuest, (Date) obj));
        }
        return res;
    }
}