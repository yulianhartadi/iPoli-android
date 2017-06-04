package io.ipoli.android.app.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

                List<Quest> quests = new ArrayList<>();
                for (Map.Entry<Long, Category> calendar : player.getAndroidCalendars().entrySet()) {
                    Map<Event, List<InstanceData>> events = syncAndroidCalendarProvider.getCalendarEvents(calendar.getKey(), startDate, endDate);
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
                calendarPersistenceService.updateSync(null, questsToUpdate, new HashSet<>(), new HashMap<>());
                localStorage.saveLong(KEY_LAST_ANDROID_CALENDAR_SYNC_DATE, DateUtils.toMillis(LocalDate.now()));
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