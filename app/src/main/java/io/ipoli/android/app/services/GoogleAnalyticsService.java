package io.ipoli.android.app.services;

import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.events.PlayerRequestedInviteEvent;
import io.ipoli.android.assistant.events.HelpEvent;
import io.ipoli.android.assistant.events.NewFeedbackEvent;
import io.ipoli.android.assistant.events.NewTodayQuestEvent;
import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.events.ShowExamplesEvent;
import io.ipoli.android.assistant.events.ShowQuestsEvent;
import io.ipoli.android.chat.events.AvatarChangedEvent;
import io.ipoli.android.chat.events.NewMessageEvent;
import io.ipoli.android.chat.events.RequestAvatarChangeEvent;
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
    public void onRequestAvatarChange(RequestAvatarChangeEvent e) {
        track(createEventBuilder("avatar", "request-change")
                .setCustomDimension(MESSAGE_AUTHOR_DIMENSION_INDEX, e.messageAuthor.name()));
    }

    @Subscribe
    public void onAvatarChanged(AvatarChangedEvent e) {
        track(createEventBuilder("avatar", "change")
                .setCustomDimension(MESSAGE_AUTHOR_DIMENSION_INDEX, e.messageAuthor.name())
                .setCustomDimension(AVATAR_DIMENSION_INDEX, e.avatar));
    }

    @Subscribe
    public void onNewFeedback(NewFeedbackEvent e) {
        track(createEventBuilder("feedback", "create")
                .setCustomDimension(TEXT_DIMENSION_INDEX, e.text));
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent e) {
        track(createEventBuilder("message", "create")
                .setCustomDimension(TEXT_DIMENSION_INDEX, e.message.getText()));
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        track(createEventBuilder("quest", "create")
                .setCustomDimension(NAME_DIMENSION_INDEX, e.name));
    }

    @Subscribe
    public void onTodayNewQuest(NewTodayQuestEvent e) {
        track(createEventBuilder("quest", "create-today")
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
    public void onPlanToday(PlanTodayEvent e) {
        trackUIEvent("chat", "plan-day");
    }

    @Subscribe
    public void onShowExamples(ShowExamplesEvent e) {
        trackUIEvent("chat", "show-examples");
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
        track(createEventBuilder("quest", "planned")
                .setCustomDimension(QUESTS_DIMENSION_INDEX, TextUtils.join("\n", questNames)));
    }

    @Subscribe
    public void onReviewToday(ReviewTodayEvent e) {
        trackUIEvent("chat", "review-day");
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
