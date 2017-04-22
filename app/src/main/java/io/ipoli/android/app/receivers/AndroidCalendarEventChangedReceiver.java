package io.ipoli.android.app.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.squareup.otto.Bus;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.App;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
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

        if(player == null) {
            return;
        }

        updateDirtyEvents(context, player);
    }

    private void updateDirtyEvents(Context context, Player player) {
        List<Quest> quests = new ArrayList<>();
        Map<Quest, Long> questToOriginalId = new HashMap<>();
        List<RepeatingQuest> repeatingQuests = new ArrayList<>();

        for (Map.Entry<Long, Category> calendar : player.getAndroidCalendars().entrySet()) {
            List<Event> dirtyEvents = syncAndroidCalendarProvider.getDirtyEvents(calendar.getKey());
            AndroidCalendarEventParser.Result result = androidCalendarEventParser.parse(dirtyEvents, calendar.getValue());
            quests.addAll(result.quests);
            questToOriginalId.putAll(result.questToOriginalId);
            repeatingQuests.addAll(result.repeatingQuests);
        }

        Map<RepeatingQuest, Pair<List<Quest>, List<Quest>>> repeatingQuestToQuestsToRemoveAndCreate = prepareRepeatingQuests(context, repeatingQuests);

        List<Quest> questsToUpdate = new ArrayList<>();
        for(Quest q : quests) {
            Quest existingQuest = questPersistenceService.findFromAndroidCalendar(q.getSourceMapping().getAndroidCalendarMapping());
            if(existingQuest != null && existingQuest.isCompleted()) {
                continue;
            }
            if(existingQuest != null) {
                copyQuestProperties(q, existingQuest);
            }
            questsToUpdate.add(q);
        }

        calendarPersistenceService.updateAsync(questsToUpdate, repeatingQuestToQuestsToRemoveAndCreate);
    }

    @NonNull
    private Map<RepeatingQuest, Pair<List<Quest>, List<Quest>>> prepareRepeatingQuests(Context context, List<RepeatingQuest> repeatingQuests) {
        Map<RepeatingQuest, Pair<List<Quest>, List<Quest>>> repeatingQuestToQuestsToRemoveAndCreate = new HashMap<>();

        for (RepeatingQuest rq : repeatingQuests) {
            RepeatingQuest existingRepeatingQuest = repeatingQuestPersistenceService.findFromAndroidCalendar(rq.getSourceMapping().getAndroidCalendarMapping());
            if (existingRepeatingQuest != null && !existingRepeatingQuest.isCompleted()) {
                copyRepeatingQuestProperties(rq, existingRepeatingQuest);

                LocalDate periodStart = getPeriodStart(rq);
                List<Quest> questsSincePeriodStart = questPersistenceService.findAllUpcomingForRepeatingQuest(periodStart, rq.getId());

                List<Quest> questsToRemove = new ArrayList<>();
                List<Quest> scheduledQuests = new ArrayList<>();

                LocalDate today = LocalDate.now();
                for (Quest q : questsSincePeriodStart) {
                    if (q.isCompleted() || q.getOriginalScheduledDate().isBefore(today)) {
                        scheduledQuests.add(q);
                    } else {
                        questsToRemove.add(q);
                        QuestNotificationScheduler.cancelAll(q, context);
                    }
                }

                long todayStartOfDay = DateUtils.toMillis(today);
                List<String> periodsToDelete = new ArrayList<>();
                for (String periodEnd : rq.getScheduledPeriodEndDates().keySet()) {
                    if (Long.valueOf(periodEnd) >= todayStartOfDay) {
                        periodsToDelete.add(periodEnd);
                    }
                }
                rq.getScheduledPeriodEndDates().keySet().removeAll(periodsToDelete);
                List<Quest> questsToCreate = repeatingQuestScheduler.schedule(rq, today, scheduledQuests);
                repeatingQuestToQuestsToRemoveAndCreate.put(rq, new Pair<>(questsToRemove, questsToCreate));

            } else {
                List<Quest> questsToCreate = repeatingQuestScheduler.schedule(rq, LocalDate.now());
                repeatingQuestToQuestsToRemoveAndCreate.put(rq, new Pair<>(new ArrayList<>(), questsToCreate));

            }
        }
        return repeatingQuestToQuestsToRemoveAndCreate;
    }

    private LocalDate getPeriodStart(RepeatingQuest repeatingQuest) {
        LocalDate today = LocalDate.now();
        Recurrence.RepeatType repeatType = repeatingQuest.getRecurrence().getRecurrenceType();
        LocalDate periodStart;
        switch (repeatType) {
            case DAILY:
            case WEEKLY:
                periodStart = today.with(DayOfWeek.MONDAY);
                break;
            case MONTHLY:
                periodStart = today.withDayOfMonth(1);
                break;
            default:
                periodStart = today.withDayOfYear(1);
                break;
        }
        return periodStart;
    }

    private void copyRepeatingQuestProperties(RepeatingQuest newRepeatingQuest, RepeatingQuest existingRepeatingQuest) {
        newRepeatingQuest.setId(existingRepeatingQuest.getId());
        newRepeatingQuest.setCreatedAt(existingRepeatingQuest.getCreatedAt());
        newRepeatingQuest.setScheduledPeriodEndDates(existingRepeatingQuest.getScheduledPeriodEndDates());
        newRepeatingQuest.setChallengeId(existingRepeatingQuest.getChallengeId());
        newRepeatingQuest.setCategory(existingRepeatingQuest.getCategory());
        newRepeatingQuest.setNotes(existingRepeatingQuest.getNotes());
        newRepeatingQuest.setPreferredStartTime(existingRepeatingQuest.getPreferredStartTime());
        newRepeatingQuest.setPriority(existingRepeatingQuest.getPriority());
        newRepeatingQuest.setSubQuests(existingRepeatingQuest.getSubQuests());
        newRepeatingQuest.setTimesADay(existingRepeatingQuest.getTimesADay());
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