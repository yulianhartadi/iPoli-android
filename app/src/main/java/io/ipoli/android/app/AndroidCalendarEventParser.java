package io.ipoli.android.app;

import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SourceMapping;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.android.calendar.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class AndroidCalendarEventParser {
    private static final int MINUTES_IN_DAY = 24 * 60;
    private static final int DEFAULT_REMINDER_MINUTES = -10;


    private final ExperienceRewardGenerator experienceRewardGenerator;
    private final CoinsRewardGenerator coinsRewardGenerator;
    private final SyncAndroidCalendarProvider syncAndroidCalendarProvider;

    public AndroidCalendarEventParser(ExperienceRewardGenerator experienceRewardGenerator, CoinsRewardGenerator coinsRewardGenerator, SyncAndroidCalendarProvider syncAndroidCalendarProvider) {
        this.experienceRewardGenerator = experienceRewardGenerator;
        this.coinsRewardGenerator = coinsRewardGenerator;
        this.syncAndroidCalendarProvider = syncAndroidCalendarProvider;
    }

    private boolean isRepeatingAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }

    public Result parse(List<Event> events) {
        return parse(events, Category.PERSONAL);
    }

    public Result parse(List<Event> events, Category category) {
        List<Quest> quests = new ArrayList<>();
        Map<Quest, Long> questToOriginalId = new HashMap<>();
        List<RepeatingQuest> repeatingQuests = new ArrayList<>();
        for (Event e : events) {
            if (e.deleted || !e.visible) {
                continue;
            }
            if (isRepeatingAndroidCalendarEvent(e)) {
                RepeatingQuest rq = parseRepeatingQuest(e, category);
                if (rq == null) {
                    continue;
                }
                repeatingQuests.add(rq);
            } else {
                Quest q = parseQuest(e, category);
                if (q == null) {
                    continue;
                }
                if (StringUtils.isEmpty(e.originalId)) {
                    quests.add(parseQuest(e, category));
                } else {
                    try {
                        questToOriginalId.put(q, Long.valueOf(e.originalId));
                    } catch (Exception ex) {
                        //log originalId not long
                    }
                }
            }
        }

        return new Result(quests, questToOriginalId, repeatingQuests);
    }

    private Quest parseQuest(Event event, Category category) {
        if (StringUtils.isEmpty(event.title) || String.valueOf(CalendarContract.Events.STATUS_CANCELED).equals(event.status)) {
            return null;
        }

        LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.dTStart), getZoneId(event));
        LocalDate startDate = DateUtils.toStartOfDayUTCLocalDate(startLocalDateTime.toLocalDate());
        if (startDate.isBefore(LocalDate.now())) {
            return null;
        }

        Quest q = new Quest(event.title);
        q.setSource(Constants.SOURCE_ANDROID_CALENDAR);
        q.setSourceMapping(SourceMapping.fromGoogleCalendar(event.calendarId, event.id));
        q.setCategoryType(category);

        LocalDate endDate = event.dTend > 0 ? DateUtils.toStartOfDayUTCLocalDate(DateUtils.fromMillis(event.dTend)) : startDate;

        q.setStartMinute(startLocalDateTime.getHour() * 60 + startLocalDateTime.getMinute());
        q.setStartDate(startDate);
        q.setEndDate(endDate);
        q.setScheduledDate(startDate);

        if (event.allDay) {
            q.setDuration(Constants.QUEST_MIN_DURATION);
        } else {
            int duration;
            if (StringUtils.isEmpty(event.duration) && event.dTend > 0 && event.dTStart > 0) {
                duration = (int) TimeUnit.MILLISECONDS.toMinutes(event.dTend - event.dTStart);
            } else if (!StringUtils.isEmpty(event.duration)) {
                Dur dur = new Dur(event.duration);
                duration = dur.getMinutes();
            } else {
                duration = Constants.QUEST_MIN_DURATION;
            }
            duration = Math.min(duration, Constants.MAX_QUEST_DURATION_HOURS * 60);
            duration = Math.max(duration, Constants.QUEST_MIN_DURATION);
            q.setDuration(duration);
        }
//        if (q.getScheduledDate().isBefore(LocalDate.now())) {
//            q.setCompletedAtDate(q.getScheduledDate());
//            int completedAtMinute = Math.min(q.getStartMinute() + q.getDuration(), MINUTES_IN_DAY);
//            q.setCompletedAtMinute(completedAtMinute);
//            q.increaseCompletedCount();
//            q.setExperience(experienceRewardGenerator.generate(q));
//            q.setCoins(coinsRewardGenerator.generate(q));
//        }

        if (event.hasAlarm) {
            List<Reminder> reminders = syncAndroidCalendarProvider.getEventReminders(event.id);
            for (Reminder r : reminders) {
                int minutes = r.minutes == -1 ? DEFAULT_REMINDER_MINUTES : -r.minutes;
                q.addReminder(new io.ipoli.android.reminder.data.Reminder(minutes));
            }
        }

        return q;
    }

    private ZoneId getZoneId(Event e) {
        String timeZone = e.eventTimeZone;
        if (StringUtils.isEmpty(timeZone)) {
            timeZone = e.eventEndTimeZone;
            if (StringUtils.isEmpty(timeZone)) {
                timeZone = e.calendarTimeZone;
            }
        }

        ZoneId zoneId;
        try {
            zoneId = StringUtils.isEmpty(timeZone) ? ZoneId.systemDefault() : ZoneId.of(timeZone);
        } catch (Exception ex) {
            zoneId = ZoneId.of(e.calendarTimeZone);
        }

        return zoneId;
    }

    private RepeatingQuest parseRepeatingQuest(Event event, Category category) {
        //rDate?
        if (StringUtils.isEmpty(event.rRule)) {
            return null;
        }

        RepeatingQuest rq = new RepeatingQuest(event.title);
        rq.setName(event.title);
        rq.setCategoryType(category);
        rq.setSource(Constants.SOURCE_ANDROID_CALENDAR);
        rq.setSourceMapping(SourceMapping.fromGoogleCalendar(event.calendarId, event.id));

        LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.dTStart), getZoneId(event));
        rq.setStartMinute(startLocalDateTime.getHour() * 60 + startLocalDateTime.getMinute());


        if (event.allDay) {
            rq.setDuration(Constants.QUEST_MIN_DURATION);
        } else {
            int duration;
            if (StringUtils.isEmpty(event.duration) && event.dTend > 0 && event.dTStart > 0) {
                duration = (int) TimeUnit.MILLISECONDS.toMinutes(event.dTend - event.dTStart);
            } else if (!StringUtils.isEmpty(event.duration)) {
                Dur dur = new Dur(event.duration);
                duration = dur.getMinutes();
            } else {
                duration = Constants.QUEST_MIN_DURATION;
            }
            duration = Math.min(duration, Constants.MAX_QUEST_DURATION_HOURS * 60);
            duration = Math.max(duration, Constants.QUEST_MIN_DURATION);
            rq.setDuration(duration);
        }

        String rRule = event.rRule;
        Recur recur;
        try {
            recur = new Recur(rRule);
        } catch (ParseException ex) {
            //log app error
            return null;
        }

        Recurrence recurrence = Recurrence.create();
        recurrence.setFlexibleCount(0);
        LocalDate startDate = DateUtils.toStartOfDayUTCLocalDate(startLocalDateTime.toLocalDate());
        recurrence.setDtstartDate(startDate);
        LocalDate endDate = null;
        if(event.dTend > 0) {
            endDate = DateUtils.toStartOfDayUTCLocalDate(DateUtils.fromMillis(event.dTend));
        } else if(recur.getUntil() != null) {
            LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(recur.getUntil().getTime()), getZoneId(event));
            endDate = DateUtils.toStartOfDayUTCLocalDate(endLocalDateTime.toLocalDate());
        }
        if(endDate != null && endDate.isBefore(LocalDate.now())) {
            return null;
        }
        recurrence.setDtendDate(endDate);

        String frequency = recur.getFrequency();
        switch (frequency) {
            case Recur.MONTHLY:
                recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
                if(recur.getMonthDayList().isEmpty() && recur.getDayList().isEmpty()) {
                    recur.getMonthDayList().add(startDate.getDayOfMonth());
                }
                recurrence.setRrule(recur.toString());
                break;
            case Recur.WEEKLY:
                recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
                if(recur.getDayList().isEmpty()) {
                    recur.getDayList().add(new WeekDay(startDate.getDayOfWeek().toString().substring(0,2)));
                }
                recurrence.setRrule(recur.toString());
                break;
            case Recur.DAILY:
                recurrence.setRecurrenceType(Recurrence.RepeatType.DAILY);
                recurrence.setRrule(createDailyRrule(recur));
                break;
            case Recur.YEARLY:
                recurrence.setRecurrenceType(Recurrence.RepeatType.YEARLY);
                if(recur.getYearDayList().isEmpty()) {
                    recur.getYearDayList().add(startDate.getDayOfYear());
                }
                recurrence.setRrule(recur.toString());
        }

        rq.setRecurrence(recurrence);

        if (event.hasAlarm) {
            List<Reminder> reminders = syncAndroidCalendarProvider.getEventReminders(event.id);
            for (Reminder r : reminders) {
                int minutes = r.minutes == -1 ? DEFAULT_REMINDER_MINUTES : -r.minutes;
                rq.addReminder(new io.ipoli.android.reminder.data.Reminder(minutes));
            }
        }

//        Log.d("AAA", event.title + " " + event.rRule);
//        Log.d("AAAB", event.title + " " + recurrence.getRrule());

        return rq;
    }

    @NonNull
    private String createDailyRrule(Recur recur) {
        recur.setFrequency(Recur.WEEKLY);
        recur.getDayList().clear();
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        recur.getDayList().add(WeekDay.SA);
        recur.getDayList().add(WeekDay.SU);
        return recur.toString();
    }

    public class Result {
        public List<Quest> quests;
        public Map<Quest, Long> questToOriginalId;
        public List<RepeatingQuest> repeatingQuests;

        public Result(List<Quest> quests, Map<Quest, Long> questToOriginalId, List<RepeatingQuest> repeatingQuests) {
            this.quests = quests;
            this.questToOriginalId = questToOriginalId;
            this.repeatingQuests = repeatingQuests;
        }
    }
}
