package io.ipoli.android.quest.schedulers;

import android.support.annotation.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Date;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.DateList;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.DateTime;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDayList;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.parameter.Value;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    private final long seed;

    public RepeatingQuestScheduler() {
        this(System.currentTimeMillis());
    }

    public RepeatingQuestScheduler(long seed) {
        this.seed = seed;
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, java.util.Date startDate) {
        if (repeatingQuest.isFlexible()) {
            return scheduleFlexibleQuest(repeatingQuest, startDate);
        }
        return scheduleFixedQuest(repeatingQuest, startDate);
    }

    private List<Quest> scheduleFlexibleQuest(RepeatingQuest repeatingQuest, java.util.Date startDate) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.WEEKLY) {
            return scheduleWeeklyFlexibleQuest(repeatingQuest, startDate);
        }
        return scheduleMonthlyFlexibleQuest(repeatingQuest, startDate);
    }

    @NonNull
    private List<Quest> scheduleMonthlyFlexibleQuest(RepeatingQuest repeatingQuest, java.util.Date startDate) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        LocalDate start = new LocalDate(startDate, DateTimeZone.UTC);
        List<LocalDate> possibleDates = findMonthlyPossibleDates(recurrence, start);
        List<Quest> result = new ArrayList<>();
        for (LocalDate date : possibleDates) {
            for (int j = 0; j < recurrence.getTimesADay(); j++) {
                result.add(createQuestFromRepeating(repeatingQuest, toStartOfDayUTC(date)));
            }
            if (result.size() == recurrence.getFlexibleCount() * recurrence.getTimesADay()) {
                break;
            }
        }
        return result;
    }

    @NonNull
    private List<LocalDate> findMonthlyPossibleDates(Recurrence recurrence, LocalDate start) {
        Set<LocalDate> possibleDates = new HashSet<>();
        List<LocalDate> allMonthDays = new ArrayList<>();
        for (int i = start.dayOfMonth().withMinimumValue().getDayOfMonth(); i <= start.dayOfMonth().withMaximumValue().getDayOfMonth(); i++) {
            WeekDayList weekDayList = getWeekDayList(recurrence.getRrule());
            LocalDate date = start.withDayOfMonth(i);
            WeekDay weekDay = new WeekDay(date.dayOfWeek().getAsShortText().substring(0, 2).toUpperCase());
            if (weekDayList.contains(weekDay)) {
                possibleDates.add(date);
            }
            allMonthDays.add(date);
        }
        if (possibleDates.size() < recurrence.getFlexibleCount()) {
            Collections.shuffle(allMonthDays, new Random(seed));
            for (LocalDate monthDate : allMonthDays) {
                possibleDates.add(monthDate);
                if (possibleDates.size() == recurrence.getFlexibleCount()) {
                    break;
                }
            }
        }
        List<LocalDate> possibleDateList = new ArrayList<>(possibleDates);
        Collections.shuffle(possibleDateList, new Random(seed));
        possibleDateList = possibleDateList.subList(0, recurrence.getFlexibleCount());

        List<LocalDate> result = new ArrayList<>();
        for (LocalDate possibleDate : possibleDateList) {
            if (!possibleDate.isBefore(start)) {
                result.add(possibleDate);
            }
        }
        return result;
    }

    @NonNull
    private List<Quest> scheduleWeeklyFlexibleQuest(RepeatingQuest repeatingQuest, java.util.Date startDate) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        int countForWeek = recurrence.getFlexibleCount();
        LocalDate start = new LocalDate(startDate, DateTimeZone.UTC);
        List<LocalDate> possibleDates = findPossibleDates(recurrence.getRrule(), countForWeek, start);

        Collections.shuffle(possibleDates, new Random(seed));

        List<Quest> result = new ArrayList<>();
        for (LocalDate endDate : possibleDates) {
            for (int j = 0; j < recurrence.getTimesADay(); j++) {
                result.add(createQuestFromRepeating(repeatingQuest, toStartOfDayUTC(endDate)));
            }
        }
        return result;
    }

    @NonNull
    private List<LocalDate> findPossibleDates(String rrule, int countForWeek, LocalDate start) {
        Set<LocalDate> possibleDates = new HashSet<>();

        WeekDayList weekDayList = getWeekDayList(rrule);
        addPreferredDays(start, possibleDates, weekDayList);
        if (weekDayList.size() < countForWeek) {
            addAdditionalDays(countForWeek, start, possibleDates);
        }

        List<LocalDate> result = new ArrayList<>();
        for (LocalDate possibleDate : possibleDates) {
            if (!possibleDate.isBefore(start)) {
                result.add(possibleDate);
            }
        }
        if (result.size() > countForWeek) {
            return result.subList(0, countForWeek);
        }
        return result;
    }

    private WeekDayList getWeekDayList(String rrule) {
        try {
            return new Recur(rrule).getDayList();

        } catch (Exception e) {
            return new WeekDayList();
        }
    }

    private void addAdditionalDays(int countForWeek, LocalDate start, Set<LocalDate> possibleDates) {
        for (int i = start.getDayOfWeek(); i <= start.dayOfWeek().withMaximumValue().getDayOfWeek(); i++) {
            possibleDates.add(start.withDayOfWeek(i));
            if (possibleDates.size() == countForWeek) {
                return;
            }
        }
    }

    private void addPreferredDays(LocalDate start, Set<LocalDate> possibleDates, WeekDayList weekDayList) {
        for (Object weekDayObj : weekDayList) {
            WeekDay weekDay = (WeekDay) weekDayObj;
            int calendarDay = WeekDay.getCalendarDay(weekDay);
            if (weekDay.equals(WeekDay.SU)) {
                calendarDay = 7;
            } else {
                calendarDay--;
            }
            possibleDates.add(start.withDayOfWeek(calendarDay));
        }
    }

    @NonNull
    private List<Quest> scheduleFixedQuest(RepeatingQuest repeatingQuest, java.util.Date startDate) {
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
            for (int i = 0; i < recurrence.getTimesADay(); i++) {
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