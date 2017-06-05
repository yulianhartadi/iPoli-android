package io.ipoli.android.app.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.support.v4.util.Pair;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.App;
import io.ipoli.android.app.InstanceData;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.AndroidCalendarMapping;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import me.everything.providers.android.calendar.Event;

import static io.ipoli.android.Constants.KEY_LAST_ANDROID_CALENDAR_SYNC_DATE;
import static io.ipoli.android.app.sync.AndroidCalendarLoader.SYNC_ANDROID_CALENDAR_MONTHS_AHEAD;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/3/17.
 */

public class AndroidCalendarSyncJobService extends JobService {

    @Inject
    LocalStorage localStorage;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    SyncAndroidCalendarProvider syncAndroidCalendarProvider;

    @Inject
    AndroidCalendarEventParser androidCalendarEventParser;

    @Inject
    CalendarPersistenceService calendarPersistenceService;
    private AsyncTask<Void, Void, Boolean> syncCalendarsTask;

    @Override
    public void onCreate() {
        super.onCreate();
        App.getAppComponent(this).inject(this);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        syncCalendarsTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                Player player = playerPersistenceService.get();

                if (player == null) {
                    return true;
                }

                updateEvents(player);
                return true;
            }

            private void updateEvents(Player player) {
                LocalDate startDate = DateUtils.fromMillis(localStorage.readLong(KEY_LAST_ANDROID_CALENDAR_SYNC_DATE, DateUtils.toMillis(LocalDate.now())));
                LocalDate endDate = LocalDate.now().plusMonths(SYNC_ANDROID_CALENDAR_MONTHS_AHEAD);

//                LocalDate startDate = LocalDate.now();
//                LocalDate endDate = LocalDate.now(); //inclusive

                Set<Quest> questsToSave = new HashSet<>();
                Set<Event> eventsToDelete = new HashSet<>();
                Set<Quest> questsToDelete = new HashSet<>();
                for (Map.Entry<Long, Category> calendar : player.getAndroidCalendars().entrySet()) {
                    Long calendarId = calendar.getKey();
                    Category category = calendar.getValue();
                    eventsToDelete.addAll(syncAndroidCalendarProvider.getDeletedEvents(calendarId));
                    populateQuestsToSaveAndDelete(startDate, endDate, questsToSave, questsToDelete, calendarId, category);
                }

                List<AndroidCalendarMapping> mappingsToDelete = new ArrayList<>();
                for(Event e : eventsToDelete) {
                    mappingsToDelete.add(new AndroidCalendarMapping(e.calendarId, e.id));
                }
                calendarPersistenceService.updateSync(questsToSave, questsToDelete, mappingsToDelete);

                localStorage.saveLong(KEY_LAST_ANDROID_CALENDAR_SYNC_DATE, DateUtils.toMillis(LocalDate.now()));
            }

            private void populateQuestsToSaveAndDelete(LocalDate startDate, LocalDate endDate, Set<Quest> questsToSave, Set<Quest> questsToDelete, Long calendarId, Category category) {
                Map<Event, List<InstanceData>> events = syncAndroidCalendarProvider.getCalendarEvents(calendarId, startDate, endDate.plusDays(1));

                for (Map.Entry<Event, List<InstanceData>> eventEntry : events.entrySet()) {
                    Event event = eventEntry.getKey();
                    List<InstanceData> instances = eventEntry.getValue();
                    List<Quest> newQuests = androidCalendarEventParser.parse(event, instances, category);
                    List<Quest> existingQuests = questPersistenceService.findNotCompletedFromAndroidCalendar(new AndroidCalendarMapping(calendarId, event.id), startDate, endDate);

                    Map<Quest, Boolean> existingQuestToIsUsed = new HashMap<>();
                    Map<LocalDate, List<Quest>> scheduledToQuests = new HashMap<>();
                    for (Quest q : existingQuests) {
                        existingQuestToIsUsed.put(q, false);
                        LocalDate scheduled = q.getScheduledDate();
                        if (scheduled == null) {
                            questsToDelete.add(q);
                            continue;
                        }
                        if (!scheduledToQuests.containsKey(scheduled)) {
                            scheduledToQuests.put(scheduled, new ArrayList<>());
                        }
                        scheduledToQuests.get(scheduled).add(q);
                    }


                    Pair<Set<Quest>, Set<Quest>> newQuestsResult = processNewQuests(newQuests, existingQuestToIsUsed, scheduledToQuests);
                    questsToSave.addAll(newQuestsResult.first);
                    questsToDelete.addAll(newQuestsResult.second);

                    questsToDelete.addAll(findExistingQuestsToDelete(existingQuestToIsUsed));

                }
            }

            private Pair<Set<Quest>, Set<Quest>> processNewQuests(List<Quest> newQuests, Map<Quest, Boolean> existingQuestToIsUsed, Map<LocalDate, List<Quest>> scheduledToQuests) {
                Set<Quest> questsToSave = new HashSet<>();
                Set<Quest> questsToDelete = new HashSet<>();
                for (Quest newQuest : newQuests) {
                    LocalDate scheduledDate = newQuest.getScheduledDate();
                    if (scheduledToQuests.containsKey(scheduledDate)) {
                        List<Quest> scheduledForDate = scheduledToQuests.get(scheduledDate);
                        Quest existingQuest = scheduledForDate.get(0);
                        copyQuestProperties(newQuest, existingQuest);
                        existingQuestToIsUsed.put(existingQuest, true);
                        if (scheduledForDate.size() > 1) {
                            addQuestsForDeletion(questsToDelete, scheduledForDate);
                        }

                    }
                    questsToSave.add(newQuest);
                }

                return new Pair<>(questsToSave, questsToDelete);
            }

            private void addQuestsForDeletion(Set<Quest> questsToDelete, List<Quest> scheduledForDate) {
                for (int i = 1; i < scheduledForDate.size(); i++) {
                    questsToDelete.add(scheduledForDate.get(i));
                }
            }

            private Set<Quest> findExistingQuestsToDelete(Map<Quest, Boolean> existingQuestToIsUsed) {
                Set<Quest> questsToDelete = new HashSet<>();
                for (Quest q : existingQuestToIsUsed.keySet()) {
                    if (!existingQuestToIsUsed.get(q)) {
                        questsToDelete.add(q);
                    }
                }
                return questsToDelete;
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
                // @TODO difficulty?
                newQuest.setDifficulty(existingQuest.getDifficulty());
                newQuest.setTimesADay(existingQuest.getTimesADay());
            }

            @Override
            protected void onPostExecute(Boolean success) {
                jobFinished(params, !success);
            }
        }.execute((Void) null);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (syncCalendarsTask != null) {
            syncCalendarsTask.cancel(true);
        }
        return true;
    }
}