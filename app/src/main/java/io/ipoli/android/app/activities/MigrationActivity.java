package io.ipoli.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.InitAppEvent;

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
        db.getReference("/v1/players/" + playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();

                if (data.containsKey("quests")) {
                    Map<String, Object> quests = (Map<String, Object>) data.get("quests");
                    for (Object q : quests.values()) {
                        updateQuestData((Map<String, Object>) q);
                    }
                }

                if (data.containsKey("inboxQuests")) {
                    Map<String, Object> quests = (Map<String, Object>) data.get("inboxQuests");
                    for (Object q : quests.values()) {
                        updateQuestData((Map<String, Object>) q);
                    }
                }

                if (data.containsKey("dayQuests")) {
                    Map<String, Object> dayQuests = (Map<String, Object>) data.get("dayQuests");
                    for (Object dayQuestList : dayQuests.values()) {
                        Map<String, Object> dayData = (Map<String, Object>) dayQuestList;
                        for (Object q : dayData.values()) {
                            updateQuestData((Map<String, Object>) q);
                        }
                    }
                }

                if (data.containsKey("repeatingQuests")) {
                    Map<String, Object> quests = (Map<String, Object>) data.get("repeatingQuests");
                    for (Object q : quests.values()) {
                        updateQuestData((Map<String, Object>) q);
                    }
                }

                if (data.containsKey("challenges")) {
                    Map<String, Object> challenges = (Map<String, Object>) data.get("challenges");

                    for (Object challenge : challenges.values()) {
                        Map<String, Object> challengeData = (Map<String, Object>) challenge;

                        if (challengeData.containsKey("challengeQuests")) {
                            Map<String, Object> quests = (Map<String, Object>) challengeData.get("challengeQuests");
                            for (Object q : quests.values()) {
                                updateQuestData((Map<String, Object>) q);
                            }
                        }

                        if (challengeData.containsKey("challengeRepeatingQuests")) {
                            Map<String, Object> quests = (Map<String, Object>) challengeData.get("challengeRepeatingQuests");
                            for (Object q : quests.values()) {
                                updateQuestData((Map<String, Object>) q);
                            }
                        }

                    }
                }

                data.put("schemaVersion", Constants.SCHEMA_VERSION);

                db.getReference("/v1/players/" + playerId).updateChildren(data, (error, dbRef) -> {

                    if (error != null) {
                        throw new RuntimeException(error.toException());
//                        eventBus.post(new AppErrorEvent(new MigrationException("Unable to update player children: " + playerId, error.toException())));
//                        Toast.makeText(MigrationActivity.this, R.string.migration_error_message, Toast.LENGTH_LONG).show();
//                        finish();
//                        return;
                    }
                    localStorage.saveInt(Constants.KEY_SCHEMA_VERSION, Constants.SCHEMA_VERSION);
                    eventBus.post(new InitAppEvent());
                    startActivity(new Intent(MigrationActivity.this, MainActivity.class));
                    finish();
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw new RuntimeException(databaseError.toException());
//                eventBus.post(new AppErrorEvent(new MigrationException("Unable to find player: " + playerId, databaseError.toException())));
//                Toast.makeText(MigrationActivity.this, R.string.migration_error_message, Toast.LENGTH_LONG).show();
//                finish();
            }
        });
    }

    private void updateQuestData(Map<String, Object> questData) {
        String originalStartKey = "originalStart";
        if (questData.containsKey(originalStartKey)) {
            Object originalStart = questData.get(originalStartKey);
            questData.put("originalScheduled", originalStart);
            questData.remove(originalStartKey);
        }
        if (questData.containsKey("end")) {
            questData.put("scheduled", questData.get("end"));
        }
        questData.put("preferredStartTime", "ANY");
        questData.remove("flexibleStartTime");
    }

    @Override
    public void onBackPressed() {

    }
}
