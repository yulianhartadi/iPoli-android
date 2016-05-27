package io.ipoli.android.app.services.readers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.SourceMapping;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class AndroidCalendarQuestListReader implements ListReader<Quest> {

    private final Set<String> questIds;
    private final Context context;
    private final CalendarProvider calendarProvider;
    private final RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    public AndroidCalendarQuestListReader(Context context, CalendarProvider calendarProvider, LocalStorage localStorage, RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        this.context = context;
        this.calendarProvider = calendarProvider;
        this.repeatingQuestPersistenceService = repeatingQuestPersistenceService;
        this.questIds = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE);
    }

    @Override
    public Observable<Quest> read() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return Observable.empty();
        }

        return Observable.just(questIds).concatMapIterable(questIds -> questIds).concatMap(questId -> {
            Event e = calendarProvider.getEvent(Integer.valueOf(questId));
            Quest q = new Quest(e.title);
            q.setId(null);
            DateTime startDateTime = new DateTime(e.dTStart, DateTimeZone.forID(e.eventTimeZone));
            DateTime endDateTime = new DateTime(e.dTend, DateTimeZone.forID(e.eventTimeZone));
            q.setAllDay(e.allDay);
            if (e.allDay) {
                q.setDuration((int) TimeUnit.DAYS.toMinutes(1));
                q.setStartMinute(0);
                startDateTime = startDateTime.plusDays(1);
                endDateTime = endDateTime.minusMillis(1);
            } else {
                q.setDuration(Minutes.minutesBetween(startDateTime, endDateTime).getMinutes());
                q.setStartMinute(startDateTime.getMinuteOfDay());
            }
            q.setStartDate(startDateTime.toLocalDate().toDate());
            q.setEndDate(endDateTime.toLocalDate().toDate());
            q.setSource(Constants.SOURCE_ANDROID_CALENDAR);
            q.setSourceMapping(SourceMapping.fromGoogleCalendar(e.id));

            if (endDateTime.toLocalDate().isBefore(new LocalDate())) {
                q.setCompletedAt(new Date(e.dTend));
                q.setCompletedAtMinute(Time.of(q.getCompletedAt()).toMinutesAfterMidnight());
            }
            if (TextUtils.isEmpty(e.originalId)) {
                return Observable.just(q);
            }
            RepeatingQuest h = repeatingQuestPersistenceService.findByExternalSourceMappingIdSync("androidCalendar", e.originalId);
            q.setRepeatingQuest(h);
            return Observable.just(q);
        }).compose(applyAPISchedulers());
    }

    private <T> Observable.Transformer<T, T> applyAPISchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
