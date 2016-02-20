package io.ipoli.android.app.services;

import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.events.PlayerRequestedInviteEvent;
import io.ipoli.android.player.events.PlayerLevelUpEvent;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.events.CompleteQuestEvent;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DateSelectedEvent;
import io.ipoli.android.quest.events.DeleteQuestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.QuestsPlannedEvent;
import io.ipoli.android.quest.events.StartQuestEvent;
import io.ipoli.android.quest.events.StopQuestEvent;
import io.ipoli.android.quest.events.TimeSelectedEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class GoogleAnalyticsService implements AnalyticsService {

    public static final int LEVEL_DIMENSION_INDEX = 1;
    public static final int MESSAGE_AUTHOR_DIMENSION_INDEX = 2;
    public static final int AVATAR_DIMENSION_INDEX = 3;
    private static final int TEXT_DIMENSION_INDEX = 4;
    public static final int NAME_DIMENSION_INDEX = 5;
    public static final int QUESTS_DIMENSION_INDEX = 6;

    public static final String CATEGORY_UI = "UI";

    private final Tracker tracker;

    public GoogleAnalyticsService(Tracker tracker) {
        this.tracker = tracker;
    }

    @Subscribe
    public void onPlayerRequestedInvite(PlayerRequestedInviteEvent e) {
        track(createEventBuilder("invite", "create"));
    }

    @Subscribe
    public void onPlayerLevelUp(PlayerLevelUpEvent e) {
        track(createEventBuilder("player", "level-up")
                .setCustomDimension(LEVEL_DIMENSION_INDEX, e.newLevel + ""));
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        track(createEventBuilder("quest", "create")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.name));
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        track(createEventBuilder("quest", "edit-request")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
        track(createEventBuilder("quest", "edit")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }


    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        track(createEventBuilder("quest", "complete-request")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onDeleteQuestRequest(DeleteQuestRequestEvent e) {
        track(createEventBuilder("quest", "delete-request")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onDeleteQuest(DeleteQuestEvent e) {
        track(createEventBuilder("quest", "delete")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onUndoDeleteQuest(UndoDeleteQuestEvent e) {
        track(createEventBuilder("quest", "undo-delete")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onTimeSelected(TimeSelectedEvent e) {
        trackUIEvent("quest", "time-selected");
    }

    @Subscribe
    public void onDateSelected(DateSelectedEvent e) {
        trackUIEvent("quest", "date-selected");
    }

    @Subscribe
    public void onQuestsPlanned(QuestsPlannedEvent e) {
        List<String> questNames = new ArrayList<>();
        for (Quest q : e.quests) {
            questNames.add(q.getName());
        }
        track(createEventBuilder("quest", "planned")
                .setCustomDimension(QUESTS_DIMENSION_INDEX, TextUtils.join("\n", questNames)));
    }

    @Subscribe
    public void onStartQuest(StartQuestEvent e) {
        track(createEventBuilder("quest", "start")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onStopQuest(StopQuestEvent e) {
        track(createEventBuilder("quest", "stop")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onCompleteQuest(CompleteQuestEvent e) {
        track(createEventBuilder("quest", "complete")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    private void track(HitBuilders.EventBuilder builder) {
        tracker.send(builder.build());
    }

    private void trackUIEvent(String label, String action, String id) {
        track(createEventBuilder(label, action, id));
    }

    private void trackUIEvent(String label, String action) {
        track(createEventBuilder(label, action));
    }

    private HitBuilders.EventBuilder createEventBuilder(String label, String action, String id) {
        return createEventBuilder(label, action).set("id", id);
    }

    private HitBuilders.EventBuilder createEventBuilder(String label, String action) {
        return new HitBuilders.EventBuilder().setCategory(CATEGORY_UI).setAction(action).setLabel(label);
    }
}
