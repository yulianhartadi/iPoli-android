package io.ipoli.android.app.services.readers;

import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Dur;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

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
public class AndroidCalendarRepeatingQuestListPersistenceService implements AndroidCalendarListPersistenceService<RepeatingQuest> {

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Override
    public void save(List<Event> events) {
        for (Event e : events) {
            if (e.allDay || e.deleted) {
                continue;
            }
            RepeatingQuest repeatingQuest = new RepeatingQuest("");
            repeatingQuestPersistenceService.findByExternalSourceMappingId(Constants.EXTERNAL_SOURCE_ANDROID_CALENDAR, String.valueOf(e.id), foundRepeatingQuest -> {
                if (foundRepeatingQuest != null) {
                    repeatingQuest.setId(foundRepeatingQuest.getId());
                    repeatingQuest.setCreatedAt(foundRepeatingQuest.getCreatedAt());
                }
                repeatingQuest.setName(e.title);
                repeatingQuest.setSource(Constants.SOURCE_ANDROID_CALENDAR);
                repeatingQuest.setAllDay(e.allDay);

                DateTimeZone timeZone = DateTimeZone.getDefault();
                if (!TextUtils.isEmpty(e.eventTimeZone)) {
                    timeZone = DateTimeZone.forID(e.eventTimeZone);
                }
                DateTime startDateTime = new DateTime(e.dTStart, timeZone);
                repeatingQuest.setStartMinute(startDateTime.getMinuteOfDay());
                Dur dur = new Dur(e.duration);
                repeatingQuest.setDuration((int) TimeUnit.MILLISECONDS.toMinutes(dur.getTime(new Date(0)).getTime()));
                Recurrence recurrence = Recurrence.create();
                recurrence.setRrule(e.rRule);
                recurrence.setRdate(e.rDate);
                if (e.dTStart > 0) {
                    recurrence.setDtstart(DateUtils.toStartOfDayUTC(new LocalDate(e.dTStart, DateTimeZone.UTC)));
                }
                if (e.dTend > 0) {
                    recurrence.setDtend(DateUtils.toStartOfDayUTC(new LocalDate(e.dTend, DateTimeZone.UTC)));
                }
                repeatingQuest.setRecurrence(recurrence);
                repeatingQuest.setSourceMapping(SourceMapping.fromGoogleCalendar(e.id));
                repeatingQuestPersistenceService.save(repeatingQuest);
            });
        }
    }
}
