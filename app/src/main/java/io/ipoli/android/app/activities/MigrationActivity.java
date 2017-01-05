package io.ipoli.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.InitAppEvent;
import io.ipoli.android.app.exceptions.MigrationException;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class MigrationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);
        ButterKnife.bind(this);
        App.getAppComponent(this).inject(this);

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String playerId = localStorage.readString(Constants.KEY_PLAYER_ID);
        db.getReference("/v0/players/" + playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> v0data = (Map<String, Object>) dataSnapshot.getValue();
                Map<String, Map<String, Object>> oldQuests = (Map<String, Map<String, Object>>) v0data.get("quests");
                if (oldQuests == null) {
                    oldQuests = new HashMap<>();
                }
                Map<String, Map<String, Object>> oldRepeatingQuests = (Map<String, Map<String, Object>>) v0data.get("repeatingQuests");
                if (oldRepeatingQuests == null) {
                    oldRepeatingQuests = new HashMap<>();
                }
                v0data.remove("quests");
                v0data.remove("repeatingQuests");
                v0data.remove("reminders");
                v0data.remove("uid");
                final Map<String, Map<String, Object>> finalOldQuests = oldQuests;
                final Map<String, Map<String, Object>> finalOldRepeatingQuests = oldRepeatingQuests;
                db.getReference("/v1/players/" + playerId).setValue(v0data, (databaseError, databaseReference) -> {

                    if (databaseError != null) {
                        eventBus.post(new AppErrorEvent(new MigrationException("Unable to create player in v1 : " + playerId, databaseError.toException())));
                        Toast.makeText(MigrationActivity.this, R.string.migration_error_message, Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    List<Map<String, Object>> simpleQuests = new ArrayList<>();
                    List<Map<String, Object>> rqQuests = new ArrayList<>();
                    for (Map<String, Object> q : finalOldQuests.values()) {
                        if (q.containsKey("repeatingQuest")) {
                            rqQuests.add(q);
                        } else {
                            simpleQuests.add(q);
                        }
                    }

                    Map<String, Object> data = new HashMap<>();
                    for (Map<String, Object> quest : simpleQuests) {
                        quest = copyQuest(quest);
                        populateQuest(quest, data);
                        data.put("/quests/" + quest.get("id"), quest);
                    }

                    Map<String, List<Map<String, Object>>> rqIdToQuests = new HashMap<>();
                    for (Map<String, Object> quest : rqQuests) {
                        String rqId = ((Map<String, Object>) quest.get("repeatingQuest")).get("id").toString();
                        if (!rqIdToQuests.containsKey(rqId)) {
                            rqIdToQuests.put(rqId, new ArrayList<>());
                        }
                        rqIdToQuests.get(rqId).add(quest);
                    }

                    for (Map<String, Object> rq : finalOldRepeatingQuests.values()) {
                        rq = copyRepeatingQuest(rq);
                        populateRepeatingQuest(rq, rqIdToQuests.get(rq.get("id").toString()), data);
                        data.put("/repeatingQuests/" + rq.get("id"), rq);
                    }

                    data.put("/schemaVersion", Constants.SCHEMA_VERSION);

                    db.getReference("/v1/players/" + playerId).updateChildren(data, (error, dbRef) -> {

                        if (error != null) {
                            eventBus.post(new AppErrorEvent(new MigrationException("Unable to update player children: " + playerId, error.toException())));
                            Toast.makeText(MigrationActivity.this, R.string.migration_error_message, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        localStorage.saveInt(Constants.KEY_SCHEMA_VERSION, Constants.SCHEMA_VERSION);
                        eventBus.post(new InitAppEvent());
                        startActivity(new Intent(MigrationActivity.this, MainActivity.class));
                        finish();
                    });
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                eventBus.post(new AppErrorEvent(new MigrationException("Unable to find player: " + playerId, databaseError.toException())));
                Toast.makeText(MigrationActivity.this, R.string.migration_error_message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void populateRepeatingQuest(Map<String, Object> repeatingQuest, List<Map<String, Object>> quests, Map<String, Object> data) {
        List<Map<String, Object>> questsToSave = new ArrayList<>();
        Long timesADay = (Long) repeatingQuest.get("timesADay");
        String rqId = repeatingQuest.get("id").toString();

        if (repeatingQuest.containsKey("challengeId")) {
            String challengeId = repeatingQuest.get("challengeId").toString();
            data.put("/challenges/" + challengeId + "/repeatingQuestIds/" + rqId, true);
            data.put("/challenges/" + challengeId + "/challengeRepeatingQuests/" + rqId, repeatingQuest);
        }

        if (quests == null || quests.isEmpty()) {
            return;
        }

        if (timesADay == 1) {

            for (Map<String, Object> quest : quests) {
                quest = copyQuest(quest);
                quest.put("repeatingQuestId", rqId);
                quest.remove("repeatingQuest");
                questsToSave.add(quest);
            }
        } else {
            Map<Long, List<Map<String, Object>>> dateToQuests = new HashMap<>();
            for (Map<String, Object> quest : quests) {
                quest = copyQuest(quest);
                quest.put("timesADay", timesADay);
                quest.put("repeatingQuestId", rqId);
                quest.remove("repeatingQuest");

                if (!dateToQuests.containsKey(quest.get("end"))) {
                    dateToQuests.put((Long) quest.get("end"), new ArrayList<>());
                }
                dateToQuests.get(quest.get("end")).add(quest);
            }

            for (List<Map<String, Object>> dateQuests : dateToQuests.values()) {
                int completedCount = 0;
                for (Map<String, Object> quest : dateQuests) {
                    if (quest.containsKey("completedAt")) {
                        completedCount++;
                    }
                }
                Map<String, Object> questToSave = dateQuests.get(0);
                questToSave.put("completedCount", completedCount);
                if (completedCount != timesADay) {
                    questToSave.remove("completedAt");
                    questToSave.remove("completedAtMinute");
                }
                questsToSave.add(questToSave);
            }
        }

        for (Map<String, Object> quest : questsToSave) {
            populateQuest(quest, data);
            data.put("/quests/" + quest.get("id"), quest);
            ((Map<String, Object>) repeatingQuest.get("questsData")).put(quest.get("id").toString(), createQuestData(quest));
        }
    }

    private Map<String, Object> copyRepeatingQuest(Map<String, Object> rq) {
        rq.put("timesADay", ((Map<String, Object>) rq.get("recurrence")).get("timesADay"));
        ((Map<String, Object>) rq.get("recurrence")).remove("timesADay");
        rq.put("questsData", new HashMap<>());
        return rq;
    }

    private Map<String, Object> copyQuest(Map<String, Object> quest) {
        int completedCount = 0;
        if (quest.containsKey("completedAt")) {
            Date newEnd = DateUtils.toStartOfDayUTC(new LocalDate(Long.valueOf(quest.get("completedAt").toString())));
            quest.put("end", newEnd.getTime());
            completedCount = 1;
        }

        quest.put("timesADay", 1);
        quest.put("completedCount", completedCount);
        quest.put("reminderStartTimes", new ArrayList<Long>());
        return quest;
    }

    private void populateQuest(Map<String, Object> quest, Map<String, Object> data) {
        String questId = (String) quest.get("id");
        if (quest.containsKey("completedAt")) {
            data.put("/dayQuests/" + quest.get("end") + "/" + questId, quest);
        } else if (!quest.containsKey("end")) {
            data.put("/inboxQuests/" + questId, quest);
        } else {
            data.put("/dayQuests/" + quest.get("end") + "/" + questId, quest);

            if (shouldAddQuestReminders(quest)) {
                createQuestReminders(quest, data);
            }
        }
        if (quest.containsKey("challengeId")) {
            String challengeId = quest.get("challengeId").toString();
            data.put("/challenges/" + challengeId + "/questsData/" + questId, createQuestData(quest));
            if (!quest.containsKey("repeatingQuest")) {
                data.put("/challenges/" + challengeId + "/challengeQuests/" + questId, quest);
            }
        }
    }

    private Map<String, Object> createQuestData(Map<String, Object> quest) {
        Long duration = (Long) quest.get("duration");
        if (quest.containsKey("completedAt") && quest.containsKey("actualStart")) {
            duration = ((((Long) quest.get("completedAt")) - ((Long) quest.get("actualStart"))) / 60000);
        }
        Long finalDuration = duration;
        return new HashMap<String, Object>() {{
            put("complete", quest.containsKey("completedAt"));
            put("duration", finalDuration);
            put("originalScheduledDate", quest.containsKey("originalStart") ? quest.get("originalStart") : null);
            put("scheduledDate", quest.containsKey("end") ? quest.get("end") : null);
        }};
    }

    private void createQuestReminders(Map<String, Object> quest, Map<String, Object> data) {
        for (Map<String, Object> reminder : (List<Map<String, Object>>) quest.get("reminders")) {
            List<Long> reminderStartTimes = (List<Long>) quest.get("reminderStartTimes");
            Long reminderStart = (Long) reminder.get("start");
            reminderStartTimes.add(reminderStart);
            data.put("/questReminders/" + reminderStart + "/" + quest.get("id").toString(), createQuestReminder(quest, reminder));
        }
    }

    private Map<String, Object> createQuestReminder(Map<String, Object> quest, Map<String, Object> reminder) {
        return new HashMap<String, Object>() {{
            put("minutesFromStart", reminder.get("minutesFromStart"));
            put("notificationId", reminder.get("notificationId"));
            put("questId", quest.get("id"));
            put("questName", quest.get("name"));
            put("start", reminder.get("start"));

        }};
    }

    private boolean shouldAddQuestReminders(Map<String, Object> quest) {
        return !quest.containsKey("completedAt") && quest.containsKey("end") && quest.containsKey("startMinute") &&
                (((Long) quest.get("startMinute")) >= 0) && quest.containsKey("reminders") && ((List<Object>) quest.get("reminders")).size() > 0 &&
                !quest.containsKey("actualStart");
    }

    @Override
    public void onBackPressed() {

    }
}
