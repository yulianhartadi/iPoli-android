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
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import me.everything.providers.android.calendar.Event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/17.
 */
public class AndroidCalendarLoader extends AsyncTaskLoader<Void> {

    private Map<Long, Category> selectedCalendars;
    private Player player;
    private SyncAndroidCalendarProvider syncAndroidCalendarProvider;
    private AndroidCalendarEventParser androidCalendarEventParser;
    private CalendarPersistenceService calendarPersistenceService;

    public AndroidCalendarLoader(Context context, Map<Long, Category> selectedCalendars, Player player, SyncAndroidCalendarProvider syncAndroidCalendarProvider, AndroidCalendarEventParser androidCalendarEventParser, CalendarPersistenceService calendarPersistenceService) {
        super(context);
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
        Set<Long> calendarsToAdd = getCalendarsToAdd(selectedCalendars, player.getAndroidCalendars().keySet());
        List<Quest> quests = new ArrayList<>();
        for (Long calendarId : calendarsToAdd) {
            Map<Event, List<InstanceData>> events = syncAndroidCalendarProvider.getCalendarEvents(calendarId, LocalDate.now(), LocalDate.now().plusMonths(3));
            List<Quest> result = androidCalendarEventParser.parse(events, selectedCalendars.get(calendarId));
            quests.addAll(result);
        }

        Set<Long> calendarsToRemove = getCalendarsToRemove(selectedCalendars, player.getAndroidCalendars().keySet());
        Map<Long, Category> calendarsToUpdate = getCalendarsToUpdate(selectedCalendars, player.getAndroidCalendars());

        player.setAndroidCalendars(selectedCalendars);
        calendarPersistenceService.updateSync(player, quests, calendarsToRemove, calendarsToUpdate);
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
