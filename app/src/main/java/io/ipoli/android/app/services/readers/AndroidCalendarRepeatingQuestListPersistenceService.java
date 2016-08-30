package io.ipoli.android.app.services.readers;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Dur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;

import java.text.ParseException;
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
public class AndroidCalendarRepeatingQuestListPersistenceService implements AndroidCalendarListPersistenceService<RepeatingQuest> {

    private final RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    public AndroidCalendarRepeatingQuestListPersistenceService(RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        this.repeatingQuestPersistenceService = repeatingQuestPersistenceService;
    }

    @Override
    public void save(List<Event> events) {
        for (Event e : events) {
            if (e.allDay || e.deleted) {
                continue;
            }
            try {
                Recur recur = new Recur(e.rRule);
                String frequency = recur.getFrequency();
                if (frequency.equals(Recur.YEARLY)) {
                    continue;
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
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

                try {
                    Recur recur = new Recur(e.rRule);
                    String frequency = recur.getFrequency();
                    switch (frequency) {
                        case Recur.MONTHLY:
                            recurrence.setRecurrenceType(Recurrence.RecurrenceType.MONTHLY);
                            recurrence.setRrule(e.rRule);
                            break;
                        case Recur.WEEKLY:
                            recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
                            recurrence.setRrule(e.rRule);
                            break;
                        case Recur.DAILY:
                            recurrence.setRecurrenceType(Recurrence.RecurrenceType.DAILY);
                            recurrence.setRrule(createDailyRrule());
                            break;
                    }
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                recurrence.setRdate(e.rDate);
                if (e.dTStart > 0) {
                    recurrence.setDtstartDate(DateUtils.toStartOfDayUTC(new LocalDate(e.dTStart, DateTimeZone.UTC)));
                }
                if (e.dTend > 0) {
                    recurrence.setDtendDate(DateUtils.toStartOfDayUTC(new LocalDate(e.dTend, DateTimeZone.UTC)));
                }
                repeatingQuest.setRecurrence(recurrence);
                repeatingQuest.setSourceMapping(SourceMapping.fromGoogleCalendar(e.id));
                repeatingQuestPersistenceService.save(repeatingQuest);
            });
        }
    }

    @NonNull
    private String createDailyRrule() {
        Recur r = new Recur(Recur.WEEKLY, null);
        r.getDayList().add(WeekDay.MO);
        r.getDayList().add(WeekDay.TU);
        r.getDayList().add(WeekDay.WE);
        r.getDayList().add(WeekDay.TH);
        r.getDayList().add(WeekDay.FR);
        r.getDayList().add(WeekDay.SA);
        r.getDayList().add(WeekDay.SU);
        return r.toString();
    }
}
