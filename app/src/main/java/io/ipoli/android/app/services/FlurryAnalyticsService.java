package io.ipoli.android.app.services;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryEventRecordStatus;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.InvitationScreenRequestedAutomaticInviteEvent;
import io.ipoli.android.app.events.InviteLogoTappedEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.events.PlayerRequestedInviteEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestedEvent;
import io.ipoli.android.quest.events.DeleteRecurrentQuestRequestEvent;
import io.ipoli.android.quest.events.DoneQuestTapEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestAddedEvent;
import io.ipoli.android.quest.events.NewQuestContextChangedEvent;
import io.ipoli.android.quest.events.NewQuestSavedEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.QuestContextUpdatedEvent;
import io.ipoli.android.quest.events.QuestDifficultyChangedEvent;
import io.ipoli.android.quest.events.QuestDraggedEvent;
import io.ipoli.android.quest.events.QuestDurationUpdatedEvent;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.ShowRecurrentQuestEvent;
import io.ipoli.android.quest.events.StartQuestTapEvent;
import io.ipoli.android.quest.events.StopQuestTapEvent;
import io.ipoli.android.quest.events.SuggestionItemTapEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;
import io.ipoli.android.quest.events.UndoDeleteRecurrentQuestEvent;
import io.ipoli.android.quest.events.UnscheduledQuestDraggedEvent;
import io.ipoli.android.quest.events.UpdateQuestEndDateRequestEvent;
import io.ipoli.android.quest.events.UpdateQuestStartTimeRequestEvent;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.RecurrentQuestDeletedEvent;
import io.ipoli.android.tutorial.events.TutorialDoneEvent;
import io.ipoli.android.tutorial.events.TutorialSkippedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */

class EventParams {
    private Map<String, String> params = new HashMap<>();

    private EventParams() {
    }

    public static EventParams create() {
        return new EventParams();
    }

    public static EventParams of(String key, String value) {
        EventParams eventParams = new EventParams();
        eventParams.params.put(key, value);
        return eventParams;
    }

    public EventParams add(String key, String value) {
        params.put(key, value);
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }
}

public class FlurryAnalyticsService implements AnalyticsService {


    @Subscribe
    public void onPlayerCreated(PlayerCreatedEvent e) {
        FlurryAgent.setUserId(e.id);
        log("player_created");
    }

    @Subscribe
    public void onInvitationScreenRequestAutomaticInviteEvent(InvitationScreenRequestedAutomaticInviteEvent e) {
        log("invitation_screen_requested_invite", EventParams.of("is_invite_received", e.isInviteReceived + ""));
    }

    @Subscribe
    public void onPlayerRequestedInvite(PlayerRequestedInviteEvent e) {
        log("invite_requested");
    }

    @Subscribe
    public void onInviteLogoTapped(InviteLogoTappedEvent e) {
        log("invite_logo_tapped");
    }

    @Subscribe
    public void onScreenShown(ScreenShownEvent e) {
        log("screen_shown", EventParams.of("name", e.screenName));
    }

    @Subscribe
    public void onNewQuestAdded(NewQuestAddedEvent e) {
        log("quest_created", EventParams.create()
                .add("id", e.quest.getId())
                .add("name", e.quest.getName())
                .add("raw_text", e.quest.getRawText()));
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        log("edit_quest_requested", e.quest.getId(), e.quest.getName(), e.source);
    }

    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        log("complete_quest_request", e.quest.getId(), e.quest.getName(), e.source);
    }


    @Subscribe
    public void onUndoCompletedQuest(UndoCompletedQuestEvent e) {
        log("undo_completed_quest", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        log("quest_completed", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onQuestSnoozed(QuestSnoozedEvent e) {
        log("quest_snoozed", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onUnscheduledQuestDragged(UnscheduledQuestDraggedEvent e) {
        log("unscheduled_quest_dragged", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onQuestDragged(QuestDraggedEvent e) {
        log("quest_dragged", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onShowQuest(ShowQuestEvent e) {
        log("quest_shown", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onFeedbackTap(FeedbackTapEvent e) {
        log("feedback_tapped");
    }

    @Subscribe
    public void onContactUsTap(ContactUsTapEvent e) {
        log("contact_us_tapped");
    }

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        log("schedule_quest_for_today", e.quest.getId(), e.quest.getName(), e.source);
    }

    @Subscribe
    public void onDeleteQuestRequested(DeleteQuestRequestedEvent e) {
        log("delete_quest_requested", e.quest.getId(), e.quest.getName(), e.source);
    }

    @Subscribe
    public void onQuestDeleted(QuestDeletedEvent e) {
        log("quest_deleted", EventParams.of("id", e.id));
    }

    @Subscribe
    public void onUndoDeleteQuest(UndoDeleteQuestEvent e) {
        log("undo_delete_quest", e.quest.getId(), e.quest.getName(), e.source);
    }

    @Subscribe
    public void onShowRecurrentQuest(ShowRecurrentQuestEvent e) {
        log("show_recurrent_quest_request", e.recurrentQuest.getId(), e.recurrentQuest.getName());
    }

    @Subscribe
    public void onDeleteRecurrentQuestRequest(DeleteRecurrentQuestRequestEvent e) {
        log("delete_recurrent_quest_requested", e.recurrentQuest.getId(), e.recurrentQuest.getName());
    }

    @Subscribe
    public void onUndoDeleteRecurrentQuest(UndoDeleteRecurrentQuestEvent e) {
        log("undo_delete_recurrent_quest", e.recurrentQuest.getId(), e.recurrentQuest.getName());
    }

    @Subscribe
    public void onUndoDeleteRecurrentQuest(RecurrentQuestDeletedEvent e) {
        log("undo_delete_recurrent_quest", EventParams.of("id", e.id));
    }

    @Subscribe
    public void onStartQuestTap(StartQuestTapEvent e) {
        log("start_quest", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onStopQuestTap(StopQuestTapEvent e) {
        log("stop_quest", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onDoneQuestTap(DoneQuestTapEvent e) {
        log("done_quest", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onUpdateQuestEndDateRequest(UpdateQuestEndDateRequestEvent e) {
        log("update_quest_end_date_request", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onUpdateQuestStartTimeRequest(UpdateQuestStartTimeRequestEvent e) {
        log("update_quest_start_time_request", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onQuestDurationUpdated(QuestDurationUpdatedEvent e) {
        log("update_quest_duration", EventParams.create()
                .add("id", e.quest.getId())
                .add("name", e.quest.getName())
                .add("duration", e.duration));
    }

    @Subscribe
    public void onQuestContextUpdated(QuestContextUpdatedEvent e) {
        log("updated_quest_context", EventParams.create()
                .add("id", e.quest.getId())
                .add("name", e.quest.getName())
                .add("context", e.questContext.name()));
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
        log("quest_updated", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onSuggestionItemTap(SuggestionItemTapEvent e) {
        log("suggestion_item_tap", EventParams.create()
                .add("suggestion_text", e.suggestionText)
                .add("current_text", e.currentText));
    }

    @Subscribe
    public void onNewQuestContextChanged(NewQuestContextChangedEvent e) {
        log("new_quest_context_changed", EventParams.of("context", e.questContext.name()));
    }

    @Subscribe
    public void onNewQuestSaved(NewQuestSavedEvent e) {
        log("new_quest_saved", EventParams.create()
                .add("text", e.text)
                .add("source", e.source));
    }

    @Subscribe
    public void onQuestDifficultyChanged(QuestDifficultyChangedEvent e) {
        log("quest_difficulty_changed", EventParams.create()
                .add("id", e.quest.getId())
                .add("name", e.quest.getName())
                .add("difficulty", e.difficulty));
    }

    @Subscribe
    public void onTutorialDone(TutorialDoneEvent e) {
        log("tutorial_done");
    }

    @Subscribe
    public void onTutorialSkipped(TutorialSkippedEvent e) {
        log("tutorial_skipped");
    }

    private FlurryEventRecordStatus log(String eventName) {
        return FlurryAgent.logEvent(eventName);
    }

    private FlurryEventRecordStatus log(String eventName, HashMap<String, String> params) {
        return FlurryAgent.logEvent(eventName, params);
    }

    private FlurryEventRecordStatus log(String eventName, EventParams eventParams) {
        return FlurryAgent.logEvent(eventName, eventParams.getParams());
    }

    private FlurryEventRecordStatus log(String eventName, String id, String name) {
        return log(eventName, EventParams.create()
                .add("id", id)
                .add("name", name));
    }

    private FlurryEventRecordStatus log(String eventName, String id, String name, String source) {
        return log(eventName, EventParams.create()
                .add("id", id)
                .add("name", name)
                .add("source", source));
    }
}
