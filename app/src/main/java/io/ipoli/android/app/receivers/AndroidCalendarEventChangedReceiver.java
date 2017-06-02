package io.ipoli.android.app.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.App;
import io.ipoli.android.app.InstanceData;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import me.everything.providers.android.calendar.Event;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class AndroidCalendarEventChangedReceiver extends BroadcastReceiver {

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    SyncAndroidCalendarProvider syncAndroidCalendarProvider;

    @Inject
    AndroidCalendarEventParser androidCalendarEventParser;

    @Inject
    CalendarPersistenceService calendarPersistenceService;

    @Inject
    RepeatingQuestScheduler repeatingQuestScheduler;

    @Inject
    Bus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!EasyPermissions.hasPermissions(context, Manifest.permission.READ_CALENDAR)) {
            return;
        }

        App.getAppComponent(context).inject(this);
        Player player = playerPersistenceService.get();

        if (player == null) {
            return;
        }

        updateEvents(context, player);
    }

    private void updateEvents(Context context, Player player) {
        List<Quest> quests = new ArrayList<>();

        for (Map.Entry<Long, Category> calendar : player.getAndroidCalendars().entrySet()) {
            Map<Event, List<InstanceData>> events = syncAndroidCalendarProvider.getCalendarEvents(calendar.getKey(), LocalDate.now(), LocalDate.now().plusMonths(3));
            quests.addAll(androidCalendarEventParser.parse(events, calendar.getValue()));
        }

        List<Quest> questsToUpdate = new ArrayList<>();
        for (Quest q : quests) {
            Quest existingQuest = questPersistenceService.findFromAndroidCalendar(q.getSourceMapping().getAndroidCalendarMapping());
            if (existingQuest != null && existingQuest.isCompleted()) {
                continue;
            }
            if (existingQuest != null) {
                copyQuestProperties(q, existingQuest);
            }
            questsToUpdate.add(q);
        }

        calendarPersistenceService.updateAsync(questsToUpdate);
    }

    private void copyQuestProperties(Quest newQuest, Quest existingQuest) {
        newQuest.setId(existingQuest.getId());
        newQuest.setCreatedAt(existingQuest.getCreatedAt());
        newQuest.setChallengeId(existingQuest.getChallengeId());
        newQuest.setPriority(existingQuest.getPriority());
        newQuest.setCategory(existingQuest.getCategory());
        newQuest.setSubQuests(existingQuest.getSubQuests());
        newQuest.setNotes(existingQuest.getNotes());
        newQuest.setPreferredStartTime(existingQuest.getPreferredStartTime());
        //what is difficulty for
        newQuest.setDifficulty(existingQuest.getDifficulty());
        newQuest.setTimesADay(existingQuest.getTimesADay());
    }

}