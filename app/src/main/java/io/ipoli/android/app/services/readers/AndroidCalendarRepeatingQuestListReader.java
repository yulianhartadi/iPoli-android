package io.ipoli.android.app.services.readers;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Dur;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SourceMapping;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import me.everything.providers.android.calendar.Event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class AndroidCalendarRepeatingQuestListReader implements AndroidCalendarListReader<RepeatingQuest> {

    private final RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    public AndroidCalendarRepeatingQuestListReader(RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        this.repeatingQuestPersistenceService = repeatingQuestPersistenceService;
    }

    @Override
    public List<RepeatingQuest> read(List<Event> events) {
        List<RepeatingQuest> res = new ArrayList<>();
        for (Event e : events) {
            if (e.allDay || e.deleted) {
                continue;
            }
            RepeatingQuest repeatingQuest = new RepeatingQuest("");
            RepeatingQuest foundRepeatingQuest = repeatingQuestPersistenceService.findByExternalSourceMappingId(Constants.EXTERNAL_SOURCE_ANDROID_CALENDAR, String.valueOf(e.id));
            if (foundRepeatingQuest != null) {
                repeatingQuest.setId(foundRepeatingQuest.getId());
                repeatingQuest.setCreatedAt(foundRepeatingQuest.getCreatedAt());
                repeatingQuest.setRemoteId(foundRepeatingQuest.getRemoteId());
            }
            repeatingQuest.setName(e.title);
            repeatingQuest.setSource(Constants.SOURCE_ANDROID_CALENDAR);
            repeatingQuest.setAllDay(e.allDay);
            DateTime startDateTime = new DateTime(e.dTStart, DateTimeZone.forID(e.eventTimeZone));
            repeatingQuest.setStartMinute(startDateTime.getMinuteOfDay());
            Dur dur = new Dur(e.duration);
            repeatingQuest.setDuration((int) TimeUnit.MILLISECONDS.toMinutes(dur.getTime(new Date(0)).getTime()));
            Recurrence recurrence = Recurrence.create();
            recurrence.setRrule(e.rRule);
            recurrence.setRdate(e.rDate);
            if(e.dTStart > 0) {
                recurrence.setDtstart(DateUtils.toStartOfDayUTC(new LocalDate(e.dTStart, DateTimeZone.UTC)));
            }
            if(e.dTend > 0) {
                recurrence.setDtend(DateUtils.toStartOfDayUTC(new LocalDate(e.dTend, DateTimeZone.UTC)));
            }
            repeatingQuest.setRecurrence(recurrence);
            repeatingQuest.setSourceMapping(SourceMapping.fromGoogleCalendar(e.id));
            res.add(repeatingQuest);
        }
        return res;
    }
}
