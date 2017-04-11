package io.ipoli.android.app;

import android.text.TextUtils;
import android.util.Pair;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SourceMapping;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import me.everything.providers.android.calendar.Event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class AndroidCalendarEventParser {

    private final ExperienceRewardGenerator experienceRewardGenerator;
    private final CoinsRewardGenerator coinsRewardGenerator;

    public AndroidCalendarEventParser(ExperienceRewardGenerator experienceRewardGenerator, CoinsRewardGenerator coinsRewardGenerator) {
        this.experienceRewardGenerator = experienceRewardGenerator;
        this.coinsRewardGenerator = coinsRewardGenerator;
    }

    private boolean isRepeatingAndroidCalendarEvent(Event e, Category category) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }

    public Pair<List<Quest>, List<RepeatingQuest>> parse(List<Event> events) {
        return parse(events, Category.PERSONAL);
    }

    public Pair<List<Quest>, List<RepeatingQuest>> parse(List<Event> events, Category category) {
        List<Quest> quests = new ArrayList<>();
        List<RepeatingQuest> repeatingQuests = new ArrayList<>();
        for (Event e : events) {
            //allDay?
            if (e.deleted || !e.visible) {
                continue;
            }
            if (isRepeatingAndroidCalendarEvent(e, category)) {
                repeatingQuests.add(parseRepeatingQuest(e));
            } else {
                quests.add(parseQuest(e, category));
            }
        }

        return new Pair<>(quests, repeatingQuests);
    }

    private Quest parseQuest(Event e, Category category) {
        Quest q = new Quest(e.title);
        q.setSource(Constants.SOURCE_ANDROID_CALENDAR);
        q.setSourceMapping(SourceMapping.fromGoogleCalendar(e.calendarId, e.id));
        q.setCategoryType(category);
        TimeZone.getTimeZone(e.eventTimeZone);

        LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.dTStart), ZoneId.of(e.eventTimeZone));
        q.setStartMinute(startLocalDateTime.getHour() * 60 + startLocalDateTime.getMinute());
        q.setStartDate(DateUtils.toStartOfDayUTCLocalDate(startLocalDateTime.toLocalDate()));
        q.setEndDate(DateUtils.toStartOfDayUTCLocalDate(DateUtils.fromMillis(e.dTend)));
        q.setScheduledDate(q.getStartDate());

        Duration dur = Duration.parse(e.duration);
        int duration = Math.min((int) dur.toMinutes(), Constants.MAX_QUEST_DURATION_HOURS * 60);
        duration = Math.max(duration, Constants.QUEST_MIN_DURATION);
        q.setDuration(duration);

        if(q.getScheduledDate().isBefore(LocalDate.now())) {
        }

        return q;
    }

    private RepeatingQuest parseRepeatingQuest(Event event) {
        RepeatingQuest rq = new RepeatingQuest(event.title);
        return rq;
    }
}
