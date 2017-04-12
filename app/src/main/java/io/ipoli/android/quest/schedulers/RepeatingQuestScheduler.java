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
import org.threeten.bp.temporal.TemporalAdjusters;

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
import io.ipoli.android.quest.data.Recurrence.RepeatType;
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
    private List<Quest> scheduledQuests;
    public static final int FOUR_WEEKS = DayOfWeek.values().length * 4;

    public RepeatingQuestScheduler() {
        this(System.currentTimeMillis());
    }

    public RepeatingQuestScheduler(long seed) {
        this.seed = seed;
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        return schedule(repeatingQuest, currentDate, new ArrayList<>());
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate) {
        return schedule(repeatingQuest, startDate, endDate, startDate);
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate) {
        return schedule(repeatingQuest, startDate, endDate, currentDate, new ArrayList<>());
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate, List<Quest> scheduledQuests) {
        if (repeatingQuest.isFlexible()) {
            return scheduleFlexibleRepeatingQuest(repeatingQuest, startDate, endDate, currentDate, scheduledQuests);
        }
        return scheduleFixedRepeatingQuest(repeatingQuest, startDate, endDate, currentDate, scheduledQuests);
    }

    @NonNull
    private List<Quest> scheduleFixedRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate, List<Quest> scheduledQuests) {

        List<Quest> result = createQuestsForFixedRepeatingQuest(repeatingQuest, startDate, endDate, currentDate);

        if (repeatingQuest.getRecurrence().getRecurrenceType() == RepeatType.MONTHLY) {
            // monthly repeat type can have only 1 quest per month, if not scheduled -> schedule for next month
            if (result.isEmpty()) {
                result.addAll(createQuestsForFixedRepeatingQuest(repeatingQuest, startDate.plusMonths(1), endDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()), currentDate));
            }
        }
        return result;
    }

    private List<Quest> scheduleFlexibleRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate, List<Quest> scheduledQuests) {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        if (recurrence.getRecurrenceType() == RepeatType.WEEKLY) {
            return scheduleWeeklyFlexibleQuest(repeatingQuest, startDate, endDate, currentDate, scheduledQuests);
        }
        List<Quest> result = scheduleMonthlyFlexibleQuest(repeatingQuest, startDate, endDate, currentDate, scheduledQuests);

        if (result.size() < recurrence.getFlexibleCount()) {
            result.addAll(scheduleMonthlyFlexibleQuest(repeatingQuest, startDate.plusMonths(1), endDate.plusMonths(1).with(lastDayOfMonth()), currentDate, scheduledQuests));
        }
        return result;
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, LocalDate currentDate, List<Quest> scheduledQuests) {
        // @ TODO handle placeholder quests
        this.scheduledQuests = scheduledQuests;
        Recurrence recurrence = repeatingQuest.getRecurrence();
        RepeatType repeatType = recurrence.getRecurrenceType();
        LocalDate startDate;
        LocalDate endDate;
//        if (repeatingQuest.isFlexible()) {
        boolean isFlexible = repeatingQuest.isFlexible();
        if (isFlexible && (repeatType == RepeatType.WEEKLY || repeatType == RepeatType.DAILY)) {
//            Recurrence recurrence = repeatingQuest.getRecurrence();
//            if (recurrence.getRecurrenceType() == Recurrence.RepeatType.WEEKLY) {
            List<Pair<LocalDate, LocalDate>> bounds = getBoundsFor4WeeksAhead(currentDate);
            List<Quest> quests = new ArrayList<>();
            for (int i = 0; i < bounds.size(); i++) {
                Pair<LocalDate, LocalDate> weekPair = bounds.get(i);
                quests.addAll(scheduleWeeklyFlexibleQuest(repeatingQuest, weekPair.first, weekPair.second, currentDate, scheduledQuests));
//            quests.addAll(saveQuestsInRange(rq, weekPair.first, weekPair.second, currentDate));
            }
            return quests;
//            }
        } else if (!isFlexible && (repeatType == RepeatType.WEEKLY || repeatType == RepeatType.DAILY)) {
            List<Pair<LocalDate, LocalDate>> bounds = getBoundsFor4WeeksAhead(currentDate);
            List<Quest> quests = new ArrayList<>();
            for (int i = 0; i < bounds.size(); i++) {
                Pair<LocalDate, LocalDate> weekPair = bounds.get(i);
                quests.addAll(scheduleFixedRepeatingQuest(repeatingQuest, weekPair.first, weekPair.second, currentDate, scheduledQuests));
//            quests.addAll(saveQuestsInRange(rq, weekPair.first, weekPair.second, currentDate));
            }
            return quests;
        } else if (isFlexible && repeatType == RepeatType.MONTHLY) {
            startDate = currentDate.with(firstDayOfMonth());
            endDate = currentDate.with(lastDayOfMonth());
            List<Quest> result = scheduleMonthlyFlexibleQuest(repeatingQuest, startDate, endDate, currentDate, scheduledQuests);

            if (result.size() < recurrence.getFlexibleCount()) {
                result.addAll(scheduleMonthlyFlexibleQuest(repeatingQuest, startDate.plusMonths(1), endDate.plusMonths(1).with(lastDayOfMonth()), currentDate, scheduledQuests));
            }
            return result;
        } else {
            startDate = currentDate.with(firstDayOfMonth());
            endDate = currentDate.with(lastDayOfMonth());
            List<Quest> result = createQuestsForFixedRepeatingQuest(repeatingQuest, startDate, endDate, currentDate);

            // monthly repeat type can have only 1 quest per month, if not scheduled -> schedule for next month
            if (result.isEmpty()) {
                result.addAll(createQuestsForFixedRepeatingQuest(repeatingQuest, startDate.plusMonths(1), endDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()), currentDate));
            }
            return result;
        }
//        if (repeatType == Recurrence.RepeatType.MONTHLY) {
////                return scheduleMonthlyFlexible(repeatingQuest, currentDate.with(firstDayOfMonth()), currentDate.with(lastDayOfMonth()), currentDate);
//            startDate = currentDate.with(firstDayOfMonth());
//            endDate = currentDate.with(lastDayOfMonth());
////                return scheduleMonthlyFlexible(repeatingQuest, currentDate.with(firstDayOfMonth()), currentDate.with(lastDayOfMonth()), currentDate);
//        } else {
//            startDate = currentDate.with(DayOfWeek.MONDAY);
//            endDate = currentDate.plusWeeks(3).with(DayOfWeek.SUNDAY);
////                return scheduleFlexibleFor4WeeksAhead(currentDate, repeatingQuest, scheduledQuests);
////                return schedule(repeatingQuest, currentDate.with(DayOfWeek.MONDAY), currentDate.with(DayOfWeek.SUNDAY), currentDate);
////                return scheduleFlexibleFor4WeeksAhead(currentDate, repeatingQuest);
//        }
////        } else {
////            if (repeatType == Recurrence.RepeatType.MONTHLY) {
//////                return scheduleFixedForMonth(repeatingQuest, currentDate);
////                return saveQuestsInRange(repeatingQuest, currentDate.with(firstDayOfMonth()), currentDate.with(lastDayOfMonth()), currentDate);
////            } else {
////                return saveQuestsInRange(repeatingQuest, currentDate.with(DayOfWeek.MONDAY), currentDate.with(DayOfWeek.SUNDAY), currentDate);
////            }
//            // @TODO handle yearly scheduling
//        }

//        return schedule(repeatingQuest, startDate, endDate, currentDate, scheduledQuests);
    }

//    private List<Quest> scheduleFlexibleFor4WeeksAhead(LocalDate currentDate, RepeatingQuest rq, List<Quest> scheduledQuests) {
//        List<Pair<LocalDate, LocalDate>> bounds = getBoundsFor4WeeksAhead(currentDate);
//        List<Quest> quests = new ArrayList<>();
//        for (int i = 0; i < bounds.size(); i++) {
//            Pair<LocalDate, LocalDate> weekPair = bounds.get(i);
//            quests.addAll(scheduleWeeklyFlexibleQuest(rq, weekPair.first, weekPair.second, currentDate, scheduledQuests));
////            quests.addAll(saveQuestsInRange(rq, weekPair.first, weekPair.second, currentDate));
//        }
//        return quests;
//    }
//
//    private List<Quest> scheduleMonthlyFlexible(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate) {
//        List<Quest> result = saveQuestsInRange(repeatingQuest, startDate, endDate, currentDate);
//        if (ChronoUnit.DAYS.between(currentDate, endDate) < FOUR_WEEKS) {
//            result.addAll(saveQuestsInRange(repeatingQuest, startDate.plusMonths(1), endDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()), currentDate));
//        }
//        return result;
//
//    }
//
//    private List<Quest> scheduleFor4WeeksAhead(RepeatingQuest repeatingQuest, LocalDate currentDate) {
//        List<Quest> quests = new ArrayList<>();
//        for (Pair<LocalDate, LocalDate> weekPair : getBoundsFor4WeeksAhead(currentDate)) {
//            quests.addAll(saveQuestsInRange(repeatingQuest, weekPair.first, weekPair.second, currentDate));
//        }
//        return quests;
//    }

//    private List<Quest> saveQuestsInRange(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate) {
//        if (!repeatingQuest.shouldBeScheduledForPeriod(endDate)) {
//            return new ArrayList<>();
//        }
//        List<Quest> questsToCreate = schedule(repeatingQuest, startDate, endDate, currentDate, scheduledQuests);
//        repeatingQuest.addScheduledPeriodEndDate(endDate);
//        return questsToCreate;
//    }

    @NonNull
    private List<Pair<LocalDate, LocalDate>> getBoundsFor4WeeksAhead(LocalDate currentDate) {
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = currentDate.with(DayOfWeek.SUNDAY);

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(startOfWeek, endOfWeek));
        for (int i = 0; i < 3; i++) {
            startOfWeek = startOfWeek.plusDays(7);
            endOfWeek = endOfWeek.plusDays(7);
            weekBounds.add(new Pair<>(startOfWeek, endOfWeek));
        }
        return weekBounds;
    }

    @NonNull
    private List<Quest> scheduleMonthlyFlexibleQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate, List<Quest> scheduledQuests) {

        if (repeatingQuest.isScheduledForDate(endDate)) {
            return new ArrayList<>();
        }

        repeatingQuest.addScheduledPeriodEndDate(endDate);

        Recurrence recurrence = repeatingQuest.getRecurrence();
        List<LocalDate> possibleDates = findMonthlyPossibleDates(recurrence, startDate, endDate, currentDate);
        List<Quest> result = new ArrayList<>();

        int countForMonth = recurrence.getFlexibleCount();

        for (Quest q : scheduledQuests) {
            if (q.isCompleted() && !q.getScheduledDate().isBefore(startDate) && !q.getScheduledDate().isAfter(endDate)) {
                countForMonth--;
            }
        }

        for (LocalDate date : possibleDates) {
            result.add(createQuestFromRepeating(repeatingQuest, date));
            if (result.size() == countForMonth) {
                break;
            }
        }
        return result;
    }

    @NonNull
    private List<LocalDate> findMonthlyPossibleDates(Recurrence recurrence, LocalDate startDate, LocalDate endDate, LocalDate currentDate) {
        Set<LocalDate> possibleDates = new HashSet<>();
        List<LocalDate> allMonthDays = new ArrayList<>();
        int lastDayOfMonth = endDate.getDayOfMonth();
        for (int i = startDate.getDayOfMonth(); i <= lastDayOfMonth; i++) {
            WeekDayList weekDayList = getWeekDayList(recurrence.getRrule());
            LocalDate date = startDate.withDayOfMonth(i);
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
            if (!possibleDate.isBefore(currentDate)) {
                result.add(possibleDate);
            }
        }
        return result;
    }

    @NonNull
    private List<Quest> scheduleWeeklyFlexibleQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate, List<Quest> scheduledQuests) {

        if (repeatingQuest.isScheduledForDate(endDate)) {
            return new ArrayList<>();
        }

        repeatingQuest.addScheduledPeriodEndDate(endDate);

        Recurrence recurrence = repeatingQuest.getRecurrence();
        int countForWeek = recurrence.getFlexibleCount();

        for (Quest q : scheduledQuests) {
            if (q.isCompleted() && !q.getScheduledDate().isBefore(startDate) && !q.getScheduledDate().isAfter(endDate)) {
                countForWeek--;
            }
        }

        List<LocalDate> possibleDates = findPossibleDates(recurrence.getRrule(), countForWeek, currentDate);

        Collections.shuffle(possibleDates, new Random(seed));

        List<Quest> result = new ArrayList<>();
        for (LocalDate scheduledDate : possibleDates) {
            result.add(createQuestFromRepeating(repeatingQuest, scheduledDate));
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
    private List<Quest> createQuestsForFixedRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate, LocalDate currentDate) {

        if (repeatingQuest.isScheduledForDate(endDate)) {
            return new ArrayList<>();
        }
        repeatingQuest.addScheduledPeriodEndDate(endDate);

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
            LocalDate scheduledDate = DateUtils.fromMillis(d.getTime());
            if (!scheduledDate.isBefore(currentDate)) {
                res.add(createQuestFromRepeating(repeatingQuest, scheduledDate));
            }
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