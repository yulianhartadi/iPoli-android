package io.ipoli.android.quest.schedulers;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.parameter.Value;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.TextStyle;

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

import static org.threeten.bp.temporal.TemporalAdjusters.firstDayOfMonth;
import static org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth;

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

    public List<Quest> scheduleAhead(RepeatingQuest repeatingQuest, LocalDate startDate) {
        List<Quest> quests = new ArrayList<>();
        Recurrence.RepeatType repeatType = repeatingQuest.getRecurrence().getRecurrenceType();
        if (repeatingQuest.isFlexible()) {
            if (repeatType == Recurrence.RepeatType.MONTHLY) {
                quests.addAll(scheduleFlexibleForMonth(repeatingQuest, startDate));
            } else if (repeatType == Recurrence.RepeatType.WEEKLY) {
                quests.addAll(scheduleFlexibleFor4WeeksAhead(startDate, repeatingQuest));
            }
        } else {
            if (repeatType == Recurrence.RepeatType.MONTHLY) {
                quests.addAll(scheduleFixedForMonth(repeatingQuest, startDate));
            } else {
                quests.addAll(scheduleFor4WeeksAhead(repeatingQuest, startDate));
            }
        }
        return quests;

    }

    private List<Quest> scheduleFixedForMonth(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        List<Quest> quests = new ArrayList<>();
        LocalDate fiveWeeksEnd = currentDate.with(DayOfWeek.SUNDAY).plusWeeks(4);
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
        return saveQuestsInRange(repeatingQuest, currentDate, currentDate.with(lastDayOfMonth()));
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
        List<Quest> questsToCreate = schedule(repeatingQuest, startDate, endOfPeriodDate);
        repeatingQuest.addScheduledPeriodEndDate(periodEnd);
        return questsToCreate;
    }

    @NonNull
    private List<Pair<LocalDate, LocalDate>> getBoundsFor4WeeksAhead(LocalDate currentDate) {
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = currentDate.with(DayOfWeek.SUNDAY);

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(currentDate, endOfWeek));
        for (int i = 0; i < 3; i++) {
            startOfWeek = startOfWeek.plusDays(7);
            endOfWeek = endOfWeek.plusDays(7);
            weekBounds.add(new Pair<>(startOfWeek, endOfWeek));
        }
        return weekBounds;
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate) {
        if (repeatingQuest.isFlexible()) {
            return scheduleFlexibleQuest(repeatingQuest, startDate);
        }
        return scheduleFixedQuest(repeatingQuest, startDate, endDate);
    }

    private List<Quest> scheduleFlexibleQuest(RepeatingQuest repeatingQuest, LocalDate startDate) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        if (recurrence.getRecurrenceType() == Recurrence.RepeatType.WEEKLY) {
            return scheduleWeeklyFlexibleQuest(repeatingQuest, startDate);
        }
        return scheduleMonthlyFlexibleQuest(repeatingQuest, startDate);
    }

    @NonNull
    private List<Quest> scheduleMonthlyFlexibleQuest(RepeatingQuest repeatingQuest, LocalDate startDate) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        List<LocalDate> possibleDates = findMonthlyPossibleDates(recurrence, startDate);
        List<Quest> result = new ArrayList<>();
        for (LocalDate date : possibleDates) {
            result.add(createQuestFromRepeating(repeatingQuest, date));
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
        int lastDayOfMonth = start.with(lastDayOfMonth()).getDayOfMonth();
        for (int i = start.with(firstDayOfMonth()).getDayOfMonth(); i <= lastDayOfMonth; i++) {
            WeekDayList weekDayList = getWeekDayList(recurrence.getRrule());
            LocalDate date = start.withDayOfMonth(i);
            String weekDayText = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).substring(0, 2).toUpperCase();
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
    private List<Quest> scheduleWeeklyFlexibleQuest(RepeatingQuest repeatingQuest, LocalDate startDate) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        int countForWeek = recurrence.getFlexibleCount();
        List<LocalDate> possibleDates = findPossibleDates(recurrence.getRrule(), countForWeek, startDate);

        Collections.shuffle(possibleDates, new Random(seed));

        List<Quest> result = new ArrayList<>();
        for (LocalDate endDate : possibleDates) {
            result.add(createQuestFromRepeating(repeatingQuest, endDate));
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
        for (int i = start.getDayOfWeek().getValue(); i <= DayOfWeek.SUNDAY.getValue(); i++) {
            possibleDates.add(start.with(DayOfWeek.of(i)));
            if (possibleDates.size() == countForWeek) {
                return;
            }
        }
    }

    private void addPreferredDays(LocalDate start, Set<LocalDate> possibleDates, WeekDayList weekDayList) {
        for (Object weekDayObj : weekDayList) {
            WeekDay weekDay = (WeekDay) weekDayObj;
            int weekDayValue = WeekDay.getCalendarDay(weekDay);
            if (weekDay.equals(WeekDay.SU)) {
                weekDayValue = 7;
            } else {
                weekDayValue--;
            }
            possibleDates.add(start.with(DayOfWeek.of(weekDayValue)));
        }
    }

    @NonNull
    private List<Quest> scheduleFixedQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate) {
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
            recur.setUntil(new Date(recurrence.getDtend()));
        }

        if (recur.getFrequency().equals(Recur.YEARLY)) {
            return res;
        }

        DateList dates = recur.getDates(new Date(DateUtils.toMillis(startDate)), new Date(recurrence.getDtstart()),
                getPeriodEnd(endDate), Value.DATE);

        for (Object obj : dates) {
            Date d = (Date) obj;
            res.add(createQuestFromRepeating(repeatingQuest, DateUtils.fromMillis(d.getTime())));
        }
        return res;
    }

    @NonNull
    private DateTime getPeriodEnd(LocalDate endDate) {
        return new DateTime(DateUtils.toStartOfDayUTC(endDate.plusDays(1)));
    }

    private Quest createQuestFromRepeating(RepeatingQuest repeatingQuest, LocalDate endDate) {
        Quest quest = new Quest();
        quest.setType(Quest.TYPE);
        quest.setName(repeatingQuest.getName());
        quest.setCategory(repeatingQuest.getCategory());
        quest.setDuration(repeatingQuest.getDuration());
        quest.setStartMinute(repeatingQuest.getStartMinute());
        quest.setEndDate(endDate);
        quest.setStartDate(endDate);
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
                Reminder questReminder = new Reminder(r.getMinutesFromStart(), String.valueOf(r.getNotificationNum()));
                questReminder.setMessage(r.getMessage());
                questReminders.add(questReminder);
            }
            quest.setReminders(questReminders);
        }
        return quest;
    }
}