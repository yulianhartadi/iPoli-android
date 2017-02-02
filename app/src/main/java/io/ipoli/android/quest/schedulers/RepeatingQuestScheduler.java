package io.ipoli.android.quest.schedulers;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Date;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.DateList;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.DateTime;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDayList;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.parameter.Value;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.reminder.data.Reminder;

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

    public List<Quest> scheduleAhead(RepeatingQuest repeatingQuest, java.util.Date startDate) {
        LocalDate currentDate = new LocalDate(startDate, DateTimeZone.UTC);
        List<Quest> quests = new ArrayList<>();
        Recurrence.RecurrenceType recurrenceType = repeatingQuest.getRecurrence().getRecurrenceType();
        if (repeatingQuest.isFlexible()) {
            if (recurrenceType == Recurrence.RecurrenceType.MONTHLY) {
                quests.addAll(scheduleFlexibleForMonth(repeatingQuest, currentDate));
            } else if (recurrenceType == Recurrence.RecurrenceType.WEEKLY) {
                quests.addAll(scheduleFlexibleFor4WeeksAhead(currentDate, repeatingQuest));
            }
        } else {
            if (recurrenceType == Recurrence.RecurrenceType.MONTHLY) {
                quests.addAll(scheduleFixedForMonth(repeatingQuest, currentDate));
            } else if (recurrenceType == Recurrence.RecurrenceType.WEEKLY) {
                quests.addAll(scheduleFor4WeeksAhead(repeatingQuest, currentDate));
            }
        }
        return quests;

    }

    private List<Quest> scheduleFixedForMonth(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        List<Quest> quests = new ArrayList<>();
        LocalDate fiveWeeksEnd = currentDate.dayOfWeek().withMaximumValue().plusWeeks(4);
        quests.addAll(saveQuestsInRange(repeatingQuest, currentDate, fiveWeeksEnd));
        return quests;
    }

    private List<Quest> scheduleFlexibleFor4WeeksAhead(LocalDate currentDate, RepeatingQuest rq) {
        List<Pair<LocalDate, LocalDate>> bounds = getBoundsFor4WeeksAhead(currentDate);
        List<Quest> quests = new ArrayList<>();
        for (int i = 0; i < bounds.size(); i++) {
            Pair<LocalDate, LocalDate> weekPair = bounds.get(i);
            quests.addAll(saveQuestsInRange(rq, weekPair.first, weekPair.second));
        }
        return quests;
    }

    private List<Quest> scheduleFlexibleForMonth(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        LocalDate endOfMonth = currentDate.dayOfMonth().withMaximumValue();
        return saveQuestsInRange(repeatingQuest, currentDate, endOfMonth);
    }

    private List<Quest> scheduleFor4WeeksAhead(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        List<Quest> quests = new ArrayList<>();
        for (Pair<LocalDate, LocalDate> weekPair : getBoundsFor4WeeksAhead(currentDate)) {
            quests.addAll(saveQuestsInRange(repeatingQuest, weekPair.first, weekPair.second));
        }
        return quests;
    }

    private List<Quest> saveQuestsInRange(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endOfPeriodDate) {
        java.util.Date periodEnd = DateUtils.toStartOfDayUTC(endOfPeriodDate);
        if (!repeatingQuest.shouldBeScheduledForPeriod(periodEnd)) {
            return new ArrayList<>();
        }
        List<Quest> questsToCreate = schedule(repeatingQuest, DateUtils.toStartOfDayUTC(startDate), DateUtils.toStartOfDayUTC(endOfPeriodDate));
        repeatingQuest.addScheduledPeriodEndDate(periodEnd);
        return questsToCreate;
    }

    @NonNull
    private List<Pair<LocalDate, LocalDate>> getBoundsFor4WeeksAhead(LocalDate currentDate) {
        LocalDate startOfWeek = currentDate.dayOfWeek().withMinimumValue();
        LocalDate endOfWeek = currentDate.dayOfWeek().withMaximumValue();

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(currentDate, endOfWeek));
        for (int i = 0; i < 3; i++) {
            startOfWeek = startOfWeek.plusDays(7);
            endOfWeek = endOfWeek.plusDays(7);
            weekBounds.add(new Pair<>(startOfWeek, endOfWeek));
        }
        return weekBounds;
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, java.util.Date startDate, java.util.Date endDate) {
        if (repeatingQuest.isFlexible()) {
            return scheduleFlexibleQuest(repeatingQuest, startDate);
        }
        return scheduleFixedQuest(repeatingQuest, startDate, endDate);
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
            result.add(createQuestFromRepeating(repeatingQuest, toStartOfDayUTC(date)));
            if (result.size() == recurrence.getFlexibleCount()) {
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
            String weekDayText = date.dayOfWeek().getAsShortText(Locale.ENGLISH).substring(0, 2).toUpperCase();
            WeekDay weekDay = new WeekDay(weekDayText);
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
            result.add(createQuestFromRepeating(repeatingQuest, toStartOfDayUTC(endDate)));
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
    private List<Quest> scheduleFixedQuest(RepeatingQuest repeatingQuest, java.util.Date startDate, java.util.Date endDate) {
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
        if (recurrence.getDtendDate() != null) {
            recur.setUntil(new Date(recurrence.getDtendDate()));
        }

        if (recur.getFrequency().equals(Recur.YEARLY)) {
            return res;
        }

        DateList dates = recur.getDates(new Date(startDate), new Date(recurrence.getDtstartDate()),
                getPeriodEnd(endDate), Value.DATE);

        for (Object obj : dates) {
            java.util.Date scheduledDate = toJavaDate((Date) obj);
            res.add(createQuestFromRepeating(repeatingQuest, scheduledDate));
        }
        return res;
    }

    private java.util.Date toJavaDate(Date date) {
        return new java.util.Date(date.getTime());
    }

    @NonNull
    private DateTime getPeriodEnd(java.util.Date endDate) {
        LocalDate nextDay = new LocalDate(endDate.getTime(), DateTimeZone.UTC).plusDays(1);
        return new DateTime(toStartOfDayUTC(nextDay));
    }

    private Quest createQuestFromRepeating(RepeatingQuest repeatingQuest, java.util.Date endDate) {
        Quest quest = new Quest();
        quest.setName(repeatingQuest.getName());
        quest.setCategory(repeatingQuest.getCategory());
        quest.setDuration(repeatingQuest.getDuration());
        quest.setStartMinute(repeatingQuest.getStartMinute());
        quest.setEndDate(endDate);
        quest.setStartDate(endDate);
        quest.setOriginalScheduledDate(endDate);
        quest.setScheduledDate(endDate);
        quest.setCreatedAt(DateUtils.nowUTC().getTime());
        quest.setUpdatedAt(DateUtils.nowUTC().getTime());
        quest.setSource(Constants.API_RESOURCE_SOURCE);
        quest.setChallengeId(repeatingQuest.getChallengeId());
        quest.setSubQuests(repeatingQuest.getSubQuests());
        quest.setNotes(repeatingQuest.getNotes());
        quest.setRepeatingQuestId(repeatingQuest.getId());
        quest.setCompletedCount(0);
        quest.setAllDay(false);
        quest.setTimesADay(repeatingQuest.getTimesADay());
        List<Reminder> questReminders = new ArrayList<>();
        if (repeatingQuest.getReminders() != null) {
            for (Reminder r : repeatingQuest.getReminders()) {
                Reminder questReminder = new Reminder(r.getMinutesFromStart(), r.getNotificationId());
                questReminder.setMessage(r.getMessage());
                questReminders.add(questReminder);
            }
            quest.setReminders(questReminders);
        }
        return quest;
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
        DateList dates = recur.getDates(new Date(recurrence.getDtstartDate()),
                new Date(from),
                getPeriodEnd(to), Value.DATE);


        for (Object obj : dates) {
            res.add(createQuestFromRepeating(repeatingQuest, toJavaDate((Date) obj)));
        }
        return res;
    }
}