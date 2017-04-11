package io.ipoli.android.app;

import android.provider.CalendarContract;
import android.text.TextUtils;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
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
        List<Quest> repeatingQuestQuests = new ArrayList<>();
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
                if(StringUtils.isEmpty(e.originalId)) {
                    quests.add(parseQuest(e, category));
                } else {
                    repeatingQuestQuests.add(q);
                }
            }
        }

        return new Result(quests, repeatingQuestQuests, repeatingQuests);
    }

    private Quest parseQuest(Event e, Category category) {
        if(StringUtils.isEmpty(e.title) || String.valueOf(CalendarContract.Events.STATUS_CANCELED).equals(e.status)) {
            return null;
        }

        LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.dTStart), getZoneId(e));
        LocalDate startDate = DateUtils.toStartOfDayUTCLocalDate(startLocalDateTime.toLocalDate());
        if(startDate.isBefore(LocalDate.now())) {
            return null;
        }

        Quest q = new Quest(e.title);
        q.setSource(Constants.SOURCE_ANDROID_CALENDAR);
        q.setSourceMapping(SourceMapping.fromGoogleCalendar(e.calendarId, e.id));
        q.setCategoryType(category);
        TimeZone.getTimeZone(e.eventTimeZone);

        q.setStartMinute(startLocalDateTime.getHour() * 60 + startLocalDateTime.getMinute());
        q.setStartDate(startDate);
        q.setEndDate(DateUtils.toStartOfDayUTCLocalDate(DateUtils.fromMillis(e.dTend)));
        q.setScheduledDate(q.getStartDate());

        if (e.allDay) {
            q.setDuration(Constants.QUEST_MIN_DURATION);
        } else {
            int duration;
            if(StringUtils.isEmpty(e.duration)) {
                duration = (int) TimeUnit.MILLISECONDS.toMinutes(e.dTend - e.dTStart);
            } else {
                Duration dur = Duration.parse(e.duration);
                duration = (int) dur.toMinutes();
            }
            duration = Math.min(duration, Constants.MAX_QUEST_DURATION_HOURS * 60);
            duration = Math.max(duration, Constants.QUEST_MIN_DURATION);
            q.setDuration(duration);
        }
        if (q.getScheduledDate().isBefore(LocalDate.now())) {
            q.setCompletedAtDate(q.getScheduledDate());
            int completedAtMinute = Math.min(q.getStartMinute() + q.getDuration(), MINUTES_IN_DAY);
            q.setCompletedAtMinute(completedAtMinute);
            q.increaseCompletedCount();
            q.setExperience(experienceRewardGenerator.generate(q));
            q.setCoins(coinsRewardGenerator.generate(q));
        } else if (e.hasAlarm) {
            List<Reminder> reminders = syncAndroidCalendarProvider.getEventReminders(e.id);
            for (Reminder r : reminders) {
                int minutes = r.minutes == -1 ? 0 : -r.minutes;
                q.addReminder(new io.ipoli.android.reminder.data.Reminder(minutes));
            }
        }

        return q;
    }

    private ZoneId getZoneId(Event e) {
        String timeZone = e.eventTimeZone;
        if(StringUtils.isEmpty(timeZone)) {
            timeZone = e.eventEndTimeZone;
            if(StringUtils.isEmpty(timeZone)) {
                timeZone = e.calendarTimeZone;
            }
        }

        ZoneId zoneId = null;
        try {
            zoneId = StringUtils.isEmpty(timeZone) ? ZoneId.systemDefault() : ZoneId.of(timeZone);
        } catch (Exception ex) {
            zoneId = ZoneId.of(e.calendarTimeZone);
        }

        return zoneId;
    }

    private RepeatingQuest parseRepeatingQuest(Event event, Category category) {
        RepeatingQuest rq = new RepeatingQuest(event.title);
//        return rq;
        return null;
    }

    public class Result {
        public List<Quest> quests;
        public List<Quest> repeatingQuestQuests;
        public List<RepeatingQuest> repeatingQuests;

        public Result(List<Quest> quests, List<Quest> repeatingQuestQuests, List<RepeatingQuest> repeatingQuests) {
            this.quests = quests;
            this.repeatingQuestQuests = repeatingQuestQuests;
            this.repeatingQuests = repeatingQuests;
        }
    }
}
