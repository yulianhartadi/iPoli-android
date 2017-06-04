package io.ipoli.android.app.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.InstanceData;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import me.everything.providers.android.calendar.Event;

import static io.ipoli.android.Constants.KEY_LAST_ANDROID_CALENDAR_SYNC_DATE;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/17.
 */
public class AndroidCalendarLoader extends AsyncTaskLoader<Void> {

    public static final int SYNC_ANDROID_CALENDAR_MONTHS_AHEAD = 3;
    private final Player player;
    private final LocalStorage localStorage;
    private final Map<Long, Category> selectedCalendars;
    private final SyncAndroidCalendarProvider syncAndroidCalendarProvider;
    private final AndroidCalendarEventParser androidCalendarEventParser;
    private final CalendarPersistenceService calendarPersistenceService;

    public AndroidCalendarLoader(Context context, LocalStorage localStorage, Map<Long, Category> selectedCalendars,
                                 Player player,
                                 SyncAndroidCalendarProvider syncAndroidCalendarProvider,
                                 AndroidCalendarEventParser androidCalendarEventParser,
                                 CalendarPersistenceService calendarPersistenceService) {
        super(context);
        this.localStorage = localStorage;
        this.selectedCalendars = selectedCalendars;
        this.player = player;
        this.syncAndroidCalendarProvider = syncAndroidCalendarProvider;
        this.androidCalendarEventParser = androidCalendarEventParser;
        this.calendarPersistenceService = calendarPersistenceService;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Void loadInBackground() {
        LocalDate startDate = DateUtils.fromMillis(localStorage.readLong(KEY_LAST_ANDROID_CALENDAR_SYNC_DATE, DateUtils.toMillis(LocalDate.now())));
        LocalDate endDate = LocalDate.now().plusMonths(SYNC_ANDROID_CALENDAR_MONTHS_AHEAD);

        Set<Long> calendarsToAdd = getCalendarsToAdd(selectedCalendars, player.getAndroidCalendars().keySet());
        List<Quest> quests = new ArrayList<>();
        for (Long calendarId : calendarsToAdd) {
            Map<Event, List<InstanceData>> events = syncAndroidCalendarProvider.getCalendarEvents(calendarId, startDate, endDate);
            List<Quest> result = androidCalendarEventParser.parse(events, selectedCalendars.get(calendarId));
            quests.addAll(result);
        }

        Set<Long> calendarsToRemove = getCalendarsToRemove(selectedCalendars, player.getAndroidCalendars().keySet());
        Map<Long, Category> calendarsToUpdate = getCalendarsToUpdate(selectedCalendars, player.getAndroidCalendars());

        player.setAndroidCalendars(selectedCalendars);
        calendarPersistenceService.updateSync(player, quests, calendarsToRemove, calendarsToUpdate);
        localStorage.saveLong(KEY_LAST_ANDROID_CALENDAR_SYNC_DATE, DateUtils.toMillis(LocalDate.now()));
        return null;
    }

    @NonNull
    private Set<Long> getCalendarsToRemove(Map<Long, Category> selectedCalendars, Set<Long> playerCalendars) {
        Set<Long> calendarsToRemove = new HashSet<>();
        for (Long calendarId : playerCalendars) {
            if (!selectedCalendars.containsKey(calendarId)) {
                calendarsToRemove.add(calendarId);
            }
        }
        return calendarsToRemove;
    }

    @NonNull
    private Set<Long> getCalendarsToAdd(Map<Long, Category> selectedCalendars, Set<Long> playerCalendars) {
        Set<Long> calendarsToAdd = new HashSet<>();
        for (Long calendarId : selectedCalendars.keySet()) {
            if (!playerCalendars.contains(calendarId)) {
                calendarsToAdd.add(calendarId);
            }
        }
        return calendarsToAdd;
    }

    @NonNull
    private Map<Long, Category> getCalendarsToUpdate(Map<Long, Category> selectedCalendars, Map<Long, Category> playerCalendars) {
        Map<Long, Category> calendarsToUpdate = new HashMap<>();
        for (Long calendarId : selectedCalendars.keySet()) {
            if (playerCalendars.keySet().contains(calendarId)) {
                if (selectedCalendars.get(calendarId) != playerCalendars.get(calendarId)) {
                    calendarsToUpdate.put(calendarId, selectedCalendars.get(calendarId));
                }
            }
        }
        return calendarsToUpdate;
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
