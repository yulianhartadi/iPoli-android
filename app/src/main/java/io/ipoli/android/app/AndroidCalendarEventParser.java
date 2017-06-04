package io.ipoli.android.app;

import android.provider.CalendarContract;

import com.squareup.otto.Bus;

import net.fortuna.ical4j.model.Dur;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.SourceMapping;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.generators.RewardPointsRewardGenerator;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.android.calendar.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class AndroidCalendarEventParser {
    private static final int DEFAULT_REMINDER_MINUTES = -10;

    private final ExperienceRewardGenerator experienceRewardGenerator;
    private final CoinsRewardGenerator coinsRewardGenerator;
    private final RewardPointsRewardGenerator rewardPointsRewardGenerator;
    private final SyncAndroidCalendarProvider syncAndroidCalendarProvider;
    private final Bus eventBus;

    public AndroidCalendarEventParser(SyncAndroidCalendarProvider syncAndroidCalendarProvider, Bus eventBus, CoinsRewardGenerator coinsRewardGenerator, ExperienceRewardGenerator experienceRewardGenerator, RewardPointsRewardGenerator rewardPointsRewardGenerator) {
        this.syncAndroidCalendarProvider = syncAndroidCalendarProvider;
        this.eventBus = eventBus;
        this.coinsRewardGenerator = coinsRewardGenerator;
        this.experienceRewardGenerator = experienceRewardGenerator;
        this.rewardPointsRewardGenerator = rewardPointsRewardGenerator;
    }

    public List<Quest> parse(Map<Event, List<InstanceData>> eventToInstances, Category category) {
        List<Quest> quests = new ArrayList<>();

        for (Map.Entry<Event, List<InstanceData>> entry : eventToInstances.entrySet()) {
            Event e = entry.getKey();
            List<InstanceData> instances = entry.getValue();
            quests.addAll(parse(e, instances, category));
        }

        return quests;
    }

    public List<Quest> parse(Event event, List<InstanceData> instances, Category category) {
        List<Quest> quests = new ArrayList<>();
        if (event.deleted || !event.visible) {
            return quests;
        }

        for (InstanceData i : instances) {
            Quest q = parseQuest(event, i, category);
            if (q == null) {
                continue;
            }
            quests.add(q);
        }
        return quests;
    }

    private Quest parseQuest(Event event, InstanceData instance, Category category) {
        if (StringUtils.isEmpty(event.title) || String.valueOf(CalendarContract.Events.STATUS_CANCELED).equals(event.status)) {
            return null;
        }

        Quest q = new Quest(event.title);
        q.setSource(Constants.SOURCE_ANDROID_CALENDAR);
        q.setSourceMapping(SourceMapping.fromGoogleCalendar(event.calendarId, event.id));
        q.setCategoryType(category);
        q.setStartMinute(instance.startMinute);

        ZoneId zoneId = getZoneId(event);
        LocalDate startDate = DateUtils.fromMillis(instance.begin, zoneId);
        LocalDate endDate = instance.end > 0 ? DateUtils.fromMillis(instance.end, zoneId) : startDate;
        q.setStartDate(startDate);
        q.setEndDate(endDate);
        q.setScheduledDate(startDate);

        if (isForThePast(q.getScheduledDate()) && event.allDay) {
            return null;
        }

        if (event.allDay) {
            q.setDuration(Constants.QUEST_MIN_DURATION);
            q.setStartMinute(null);
            if (!event.hasAlarm) {
                q.addReminder(new io.ipoli.android.reminder.data.Reminder(0));
            }
        } else {
            int duration;
            if (StringUtils.isEmpty(event.duration) && instance.end > 0 && instance.begin > 0) {
                duration = (int) TimeUnit.MILLISECONDS.toMinutes(instance.end - instance.begin);
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

        if (isForThePast(q.getScheduledDate())) {
            int completedAtMinute = Math.min(q.getStartMinute() + q.getDuration(), Time.MINUTES_IN_A_DAY);
            q.setCompletedAt(q.getScheduled());
            q.setCompletedAtMinute(completedAtMinute);
            q.increaseCompletedCount();
            q.setExperience(experienceRewardGenerator.generate(q));
            q.setCoins(coinsRewardGenerator.generate(q));
            q.setRewardPoints(rewardPointsRewardGenerator.generate(q));
        }

        return q;
    }

    private boolean isForThePast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
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

    protected void postError(Exception e) {
        eventBus.post(new AppErrorEvent(e));
    }
}
