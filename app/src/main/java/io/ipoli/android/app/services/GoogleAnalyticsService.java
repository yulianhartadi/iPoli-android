package io.ipoli.android.app.services;

import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.assistant.events.HelpEvent;
import io.ipoli.android.assistant.events.NewTodayQuestEvent;
import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.events.ShowQuestsEvent;
import io.ipoli.android.chat.events.NewMessageEvent;
import io.ipoli.android.player.events.PlayerLevelUpEvent;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.events.ChangeQuestOrderEvent;
import io.ipoli.android.quest.events.CompleteQuestEvent;
import io.ipoli.android.quest.events.DeleteQuestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.QuestsPlannedEvent;
import io.ipoli.android.quest.events.StartQuestEvent;
import io.ipoli.android.quest.events.StopQuestEvent;
import io.ipoli.android.quest.events.UndoCompleteQuestEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class GoogleAnalyticsService implements AnalyticsService {

    public static final String CATEGORY_UI = "UI";
    private final Tracker tracker;

    public GoogleAnalyticsService(Tracker tracker) {
        this.tracker = tracker;
    }

    @Subscribe
    public void onPlayerLevelUp(PlayerLevelUpEvent e) {
        track(createEventBuilder("player", "level-up").set("level", e.level + ""));
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent e) {
        track(createEventBuilder("message", "create").set("text", e.message.getText()));
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        track(createEventBuilder("quest", "create").set("name", e.name));
    }

    @Subscribe
    public void onTodayNewQuest(NewTodayQuestEvent e) {
        track(createEventBuilder("quest", "create-today").set("name", e.name));
    }

    @Subscribe
    public void onDeleteQuest(DeleteQuestEvent e) {
        track(createEventBuilder("quest", "delete").set("name", e.quest.getName()));
    }

    @Subscribe
    public void onUndoDeleteQuest(UndoDeleteQuestEvent e) {
        track(createEventBuilder("quest", "undo-delete").set("name", e.quest.getName()));
    }

    @Subscribe
    public void onPlanToday(PlanTodayEvent e) {
        trackUIEvent("chat", "plan-day");
    }

    @Subscribe
    public void onChangeQuestOrder(ChangeQuestOrderEvent e) {
        track(createEventBuilder("quest", "change-order").set("name", e.quest.getName()));
    }

    @Subscribe
    public void onHelp(HelpEvent e) {
        trackUIEvent("chat", "help");
    }

    @Subscribe
    public void onRename(RenameAssistantEvent e) {
        trackUIEvent("chat", "rename");
    }

    @Subscribe
    public void onShowQuests(ShowQuestsEvent e) {
        trackUIEvent("chat", "show-quests");
    }

    @Subscribe
    public void onQuestsPlanned(QuestsPlannedEvent e) {
        List<String> questNames = new ArrayList<>();
        for (Quest q : e.quests) {
            questNames.add(q.getName());
        }
        track(createEventBuilder("quest", "planned").set("quests", TextUtils.join("\n", questNames)));
    }

    @Subscribe
    public void onReviewToday(ReviewTodayEvent e) {
        trackUIEvent("chat", "review-day");
    }

    @Subscribe
    public void onStartQuest(StartQuestEvent e) {
        track(createEventBuilder("quest", "start").set("name", e.quest.getName()));
    }

    @Subscribe
    public void onStopQuest(StopQuestEvent e) {
        track(createEventBuilder("quest", "stop").set("name", e.quest.getName()));
    }

    @Subscribe
    public void onCompleteQuest(CompleteQuestEvent e) {
        track(createEventBuilder("quest", "complete").set("name", e.quest.getName()));
    }

    @Subscribe
    public void onUndoCompleteQuest(UndoCompleteQuestEvent e) {
        track(createEventBuilder("quest", "undo-complete").set("name", e.quest.getName()));
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
