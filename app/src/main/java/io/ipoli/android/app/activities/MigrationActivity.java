package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

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
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class MigrationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_add);
        ButterKnife.bind(this);
        App.getAppComponent(this).inject(this);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        String playerId = localStorage.readString(Constants.KEY_PLAYER_ID);
        final String playerId = "-K_AcxWKh-x1sMsz945k";
        db.getReference("/v0/players/" + playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> v0data = (Map<String, Object>) dataSnapshot.getValue();
                Map<String, Map<String, Object>> oldQuests = (Map<String, Map<String, Object>>) v0data.get("quests");
                Map<String, Map<String, Object>> oldRepeatingQuests = (Map<String, Map<String, Object>>) v0data.get("repeatingQuests");
                v0data.remove("quests");
                v0data.remove("repeatingQuests");
                v0data.remove("reminders");
                db.getReference("/v1/players/" + playerId).setValue(v0data, (databaseError, databaseReference) -> {
                    List<Map<String, Object>> simpleQuests = new ArrayList<>();
                    List<Map<String, Object>> rqQuests = new ArrayList<>();
                    for (Map<String, Object> q : oldQuests.values()) {
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
                    for(Map<String, Object> quest : rqQuests) {
                        String rqId = ((Map<String, Object>)quest.get("repeatingQuest")).get("id").toString();
                        if(!rqIdToQuests.containsKey(rqId)) {
                            rqIdToQuests.put(rqId, new ArrayList<>());
                        }
                        rqIdToQuests.get(rqId).add(quest);
                    }

                    for (Map<String, Object> rq : oldRepeatingQuests.values()) {
                        rq = copyRepeatingQuest(rq);
                        populateRepeatingQuest(rq, rqIdToQuests.get(rq.get("id").toString()), data);
                        data.put("/repeatingQuests/" + rq.get("id"), rq);
                    }

                    db.getReference("/v1/players/" + playerId).updateChildren(data);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateRepeatingQuest(Map<String, Object> repeatingQuest, List<Map<String,Object>> quests, Map<String, Object> data) {
        List<Map<String, Object>> questsToSave = new ArrayList<>();
        Long timesADay = (Long) repeatingQuest.get("timesADay");
        String rqId = repeatingQuest.get("id").toString();
        if(timesADay == 1) {
            for(Map<String, Object> quest : quests) {
                quest = copyQuest(quest);
                quest.put("repeatingQuestId", rqId);
                quest.remove("repeatingQuest");
                questsToSave.add(quest);
            }
        } else {
            Map<Long, List<Map<String, Object>>> dateToQuests = new HashMap<>();
            for(Map<String, Object> quest : quests) {
                quest = copyQuest(quest);
                quest.put("timesADay", timesADay);
                quest.put("repeatingQuestId", rqId);
                quest.remove("repeatingQuest");

                if(!dateToQuests.containsKey(quest.get("end"))) {
                    dateToQuests.put((Long) quest.get("end"), new ArrayList<>());
                }
                dateToQuests.get(quest.get("end")).add(quest);
            }

            for(List<Map<String, Object>> dateQuests : dateToQuests.values()) {
                int completedCount = 0;
                for(Map<String, Object> quest : dateQuests) {
                    if(quest.containsKey("completedAt")) {
                        completedCount ++;
                    }
                }
                Map<String, Object> questToSave = dateQuests.get(0);
                questToSave.put("completedCount", completedCount);
                questsToSave.add(questToSave);
            }

        }

        for(Map<String, Object> quest : questsToSave) {
            populateQuest(quest, data);
            data.put("/quests/" + quest.get("id"), quest);
            ((Map<String, Object>)repeatingQuest.get("questData")).put(quest.get("id").toString(), createQuestData(quest));
        }

        if(repeatingQuest.containsKey("challengeId")) {
            String challengeId = repeatingQuest.get("challengeId").toString();
            data.put("/challenges/" + challengeId + "/repeatingQuestIds/" + rqId, true);
            data.put("/challenges/" + challengeId + "/challengeRepeatingQuests/" + rqId, repeatingQuest);
        }
    }

    private Map<String, Object> copyRepeatingQuest(Map<String, Object> rq) {
        rq.put("timesADay", ((Map<String, Object>)rq.get("recurrence")).get("timesADay"));
        ((Map<String, Object>)rq.get("recurrence")).remove("timesADay");
        rq.put("questData", new HashMap<>());
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
        quest.put("reminderStartTimes", new ArrayList<String>());
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
        if(quest.containsKey("completedAt") && quest.containsKey("actualStart")) {
            duration = ((((Long) quest.get("completedAt")) - ((Long) quest.get("actualStart"))) / 60000);
        }
        Long finalDuration = duration;
        return new HashMap<String, Object>(){{
            put("complete", quest.containsKey("completedAt"));
            put("duration", finalDuration);
            put("originalScheduledDate", quest.containsKey("originalStart") ? quest.get("originalStart") : null);
            put("scheduledDate", quest.containsKey("end") ? quest.get("end") : null);
        }};
    }

    private void createQuestReminders(Map<String, Object> quest, Map<String, Object> data) {
        for (Map<String, Object> reminder : (List<Map<String, Object>>) quest.get("reminders")) {
            List<String> reminderStartTimes = (List<String>) quest.get("reminderStartTimes");
            String reminderStart = reminder.get("start").toString();
            reminderStartTimes.add(reminderStart);
            data.put("/questReminders/" + reminderStart + "/" + quest.get("id").toString(), createQuestReminder(quest, reminder));
        }
    }

    private Map<String, Object> createQuestReminder(Map<String, Object> quest, Map<String, Object> reminder) {
        return new HashMap<String, Object>(){{
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
}
