package io.ipoli.android.app.services.readers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Dur;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.SourceMapping;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class AndroidCalendarRepeatingQuestListReader implements ListReader<RepeatingQuest> {

    private final Set<String> repeatingQuestIds;
    private final Context context;
    private final CalendarProvider calendarProvider;

    public AndroidCalendarRepeatingQuestListReader(Context context, CalendarProvider calendarProvider, LocalStorage localStorage) {
        this.context = context;
        this.calendarProvider = calendarProvider;
        this.repeatingQuestIds = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_REPEATING_QUESTS_TO_UPDATE);
    }

    @Override
    public Observable<RepeatingQuest> read() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return Observable.empty();
        }

        return Observable.just(repeatingQuestIds).concatMapIterable(repeatingQuestIds -> repeatingQuestIds).concatMap(repeatingQuestId -> {
            Event e = calendarProvider.getEvent(Integer.valueOf(repeatingQuestId));
            RepeatingQuest repeatingQuest = new RepeatingQuest("");
            repeatingQuest.setId(null);
            repeatingQuest.setName(e.title);
            repeatingQuest.setSource(Constants.SOURCE_ANDROID_CALENDAR);
            repeatingQuest.setAllDay(e.allDay);
            DateTime startDateTime = new DateTime(e.dTStart, DateTimeZone.forID(e.eventTimeZone));
            repeatingQuest.setStartMinute(startDateTime.getMinuteOfDay());
            Dur dur = new Dur(e.duration);
            repeatingQuest.setDuration((int) TimeUnit.MILLISECONDS.toMinutes(dur.getTime(new Date(0)).getTime()));
            Recurrence recurrence = Recurrence.create();
            recurrence.setRrule(e.rRule, Recurrence.RecurrenceType.WEEKLY);
            recurrence.setRdate(e.rDate);
            recurrence.setDtstart(DateUtils.toStartOfDayUTC(new LocalDate(e.dTStart, DateTimeZone.UTC)));
            recurrence.setDtend(DateUtils.toStartOfDayUTC(new LocalDate(e.dTend, DateTimeZone.UTC)));
            repeatingQuest.setRecurrence(recurrence);
            repeatingQuest.setSourceMapping(SourceMapping.fromGoogleCalendar(e.id));
            return Observable.just(repeatingQuest);
        }).compose(applyAPISchedulers());
    }

    private <T> Observable.Transformer<T, T> applyAPISchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
