package io.ipoli.android.app;

import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.squareup.otto.Bus;

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SourceMapping;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.android.calendar.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class AndroidCalendarEventParser {
    private static final int DEFAULT_REMINDER_MINUTES = -10;

    private final SyncAndroidCalendarProvider syncAndroidCalendarProvider;
    private final Bus eventBus;

    public AndroidCalendarEventParser(SyncAndroidCalendarProvider syncAndroidCalendarProvider, Bus eventBus) {
        this.syncAndroidCalendarProvider = syncAndroidCalendarProvider;
        this.eventBus = eventBus;
    }

    private boolean isRepeatingAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
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
                        postError(ex);
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

        ZoneId zoneId = getZoneId(event);
        LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.dTStart), getZoneId(event));
        LocalDate startDate = DateUtils.fromMillis(event.dTStart, zoneId);
        if (isForThePast(startDate)) {
            return null;
        }

        Quest q = new Quest(event.title);
        q.setSource(Constants.SOURCE_ANDROID_CALENDAR);
        q.setSourceMapping(SourceMapping.fromGoogleCalendar(event.calendarId, event.id));
        q.setCategoryType(category);

        LocalDate endDate = event.dTend > 0 ? DateUtils.fromMillis(event.dTend, zoneId) : startDate;

        q.setStartMinute(startLocalDateTime.getHour() * 60 + startLocalDateTime.getMinute());
        q.setStartDate(startDate);
        q.setEndDate(endDate);
        q.setScheduledDate(startDate);

        if (event.allDay) {
            q.setDuration(Constants.QUEST_MIN_DURATION);
            q.setStartMinute(null);
            if (!event.hasAlarm) {
                q.addReminder(new io.ipoli.android.reminder.data.Reminder(0));
            }
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

        if (event.hasAlarm) {
            List<Reminder> reminders = syncAndroidCalendarProvider.getEventReminders(event.id);
            for (Reminder r : reminders) {
                int minutes = r.minutes == -1 ? DEFAULT_REMINDER_MINUTES : -r.minutes;
                q.addReminder(new io.ipoli.android.reminder.data.Reminder(minutes));
            }
        }

        return q;
    }

    private boolean isForThePast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
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

        ZoneId zoneId = getZoneId(event);
        LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.dTStart), zoneId);
        rq.setStartMinute(startLocalDateTime.getHour() * 60 + startLocalDateTime.getMinute());

        if (event.allDay) {
            rq.setDuration(Constants.QUEST_MIN_DURATION);
            rq.setStartMinute(null);
            if (!event.hasAlarm) {
                rq.addReminder(new io.ipoli.android.reminder.data.Reminder(0));
            }
        } else {
            int duration;
            if (StringUtils.isEmpty(event.duration) && event.dTend > 0 && event.dTStart > 0) {
                duration = (int) TimeUnit.MILLISECONDS.toMinutes(event.dTend - event.dTStart);
            } else if (!StringUtils.isEmpty(event.duration)) {
                Dur dur = new Dur(event.duration);
                duration = (int) TimeUnit.MILLISECONDS.toMinutes(dur.getTime(new Date(0)).getTime());
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
            postError(ex);
            return null;
        }

        Recurrence recurrence = Recurrence.create();
        recurrence.setFlexibleCount(0);
        LocalDate startDate = DateUtils.fromMillis(event.dTStart, zoneId);
        recurrence.setDtstartDate(startDate);
        LocalDate endDate = null;
        if (event.dTend > 0) {
            endDate = DateUtils.fromMillis(event.dTend, zoneId);
        } else if (recur.getUntil() != null) {
            endDate = DateUtils.fromMillis(recur.getUntil().getTime(), zoneId);
        }
        if (isForThePast(endDate)) {
            return null;
        }
        recurrence.setDtendDate(endDate);

        String frequency = recur.getFrequency();
        switch (frequency) {
            case Recur.MONTHLY:
                recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
                if (recur.getMonthDayList().isEmpty() && recur.getDayList().isEmpty()) {
                    recur.getMonthDayList().add(startDate.getDayOfMonth());
                }
                recurrence.setRrule(recur.toString());
                break;
            case Recur.WEEKLY:
                recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
                if (recur.getDayList().isEmpty()) {
                    recur.getDayList().add(new WeekDay(startDate.getDayOfWeek().toString().substring(0, 2)));
                }
                recurrence.setRrule(recur.toString());
                break;
            case Recur.DAILY:
                recurrence.setRecurrenceType(Recurrence.RepeatType.DAILY);
                recurrence.setRrule(createDailyRrule(recur));
                break;
            case Recur.YEARLY:
                recurrence.setRecurrenceType(Recurrence.RepeatType.YEARLY);
                recurrence.setRrule(recur.toString());
                break;
        }

        rq.setRecurrence(recurrence);

        if (event.hasAlarm) {
            List<Reminder> reminders = syncAndroidCalendarProvider.getEventReminders(event.id);
            for (Reminder r : reminders) {
                int minutes = r.minutes == -1 ? DEFAULT_REMINDER_MINUTES : -r.minutes;
                rq.addReminder(new io.ipoli.android.reminder.data.Reminder(minutes));
            }
        }

        return rq;
    }

    private ZoneId getZoneId(Event event) {
        String timeZone = event.eventTimeZone;
        if (StringUtils.isEmpty(timeZone)) {
            timeZone = event.eventEndTimeZone;
            if (StringUtils.isEmpty(timeZone)) {
                timeZone = event.calendarTimeZone;
            }
        }

        if (StringUtils.isEmpty(timeZone)) {
            return ZoneId.systemDefault();
        }

        try {
            return ZoneId.of(timeZone);
        } catch (DateTimeException ex) {
            postError(ex);
            try {
                return ZoneId.of(event.calendarTimeZone);
            } catch (DateTimeException e) {
                postError(e);
                return ZoneId.systemDefault();
            }
        }
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

    protected void postError(Exception e) {
        eventBus.post(new AppErrorEvent(e));
    }
}
