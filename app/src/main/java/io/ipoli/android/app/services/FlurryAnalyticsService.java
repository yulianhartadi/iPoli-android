package io.ipoli.android.app.services;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryEventRecordStatus;
import com.squareup.otto.Subscribe;

import java.util.HashMap;

import io.ipoli.android.app.events.CalendarPermissionResponseEvent;
import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.InviteFriendEvent;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.events.QuestShareProviderPickedEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.help.events.HelpDialogShownEvent;
import io.ipoli.android.app.help.events.MoreHelpTappedEvent;
import io.ipoli.android.app.rate.events.RateDialogFeedbackDeclinedEvent;
import io.ipoli.android.app.rate.events.RateDialogFeedbackSentEvent;
import io.ipoli.android.app.rate.events.RateDialogLoveTappedEvent;
import io.ipoli.android.app.rate.events.RateDialogRateTappedEvent;
import io.ipoli.android.app.rate.events.RateDialogShownEvent;
import io.ipoli.android.app.services.analytics.EventParams;
import io.ipoli.android.app.ui.events.SuggestionsUnavailableEvent;
import io.ipoli.android.app.ui.events.ToolbarCalendarTapEvent;
import io.ipoli.android.player.events.AvatarPickedEvent;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.AddQuestButtonTappedEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestedEvent;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.events.DoneQuestTapEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestContextChangedEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewQuestSavedEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.QuestContextUpdatedEvent;
import io.ipoli.android.quest.events.QuestDraggedEvent;
import io.ipoli.android.quest.events.QuestDurationUpdatedEvent;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.RescheduleQuestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.ScheduleQuestRequestEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.ShowRepeatingQuestEvent;
import io.ipoli.android.quest.events.StartQuestTapEvent;
import io.ipoli.android.quest.events.StopQuestTapEvent;
import io.ipoli.android.quest.events.SuggestionAcceptedEvent;
import io.ipoli.android.quest.events.SuggestionItemTapEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;
import io.ipoli.android.quest.events.UndoDeleteRepeatingQuestEvent;
import io.ipoli.android.quest.events.UnscheduledQuestDraggedEvent;
import io.ipoli.android.quest.events.UpdateQuestEndDateRequestEvent;
import io.ipoli.android.quest.events.UpdateQuestStartTimeRequestEvent;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.RepeatingQuestDeletedEvent;
import io.ipoli.android.reward.events.BuyRewardEvent;
import io.ipoli.android.reward.events.DeleteRewardRequestEvent;
import io.ipoli.android.reward.events.EditRewardRequestEvent;
import io.ipoli.android.reward.events.NewRewardSavedEvent;
import io.ipoli.android.tutorial.events.PredefinedQuestDeselectedEvent;
import io.ipoli.android.tutorial.events.PredefinedQuestSelectedEvent;
import io.ipoli.android.tutorial.events.PredefinedRepeatingQuestDeselectedEvent;
import io.ipoli.android.tutorial.events.PredefinedRepeatingQuestSelectedEvent;
import io.ipoli.android.tutorial.events.ShowTutorialEvent;
import io.ipoli.android.tutorial.events.SyncCalendarCheckTappedEvent;
import io.ipoli.android.tutorial.events.TutorialDoneEvent;
import io.ipoli.android.tutorial.events.TutorialSkippedEvent;

public class FlurryAnalyticsService implements AnalyticsService {


    @Subscribe
    public void onPlayerCreated(PlayerCreatedEvent e) {
        FlurryAgent.setUserId(e.remoteId);
        log("player_created");
    }

    @Subscribe
    public void onScreenShown(ScreenShownEvent e) {
        log("screen_shown", EventParams.of("name", e.source.name().toLowerCase()));
    }

    @Subscribe
    public void onNewQuestAdded(NewQuestEvent e) {
        log("quest_created", EventParams.create()
                .add("name", e.quest.getName())
                .add("raw_text", e.quest.getRawText()));
    }

    @Subscribe
    public void onNewRepeatingQuestAdded(NewRepeatingQuestEvent e) {
        log("repeating_quest_created", EventParams.of("raw_text", e.repeatingQuest.getRawText()));
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        log("edit_quest_requested", e.quest.getId(), e.quest.getName(), e.source.name().toLowerCase());
    }

    @Subscribe
    public void onUndoCompletedQuest(UndoCompletedQuestEvent e) {
        log("undo_completed_quest", e.quest.getId(), e.quest.getName());
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        log("quest_completed", e.quest.getId(), e.quest.getName(), e.source.name().toLowerCase());
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
        log("schedule_quest_for_today", e.quest.getId(), e.quest.getName(), e.source.name().toLowerCase());
    }

    @Subscribe
    public void onDeleteQuestRequested(DeleteQuestRequestedEvent e) {
        log("delete_quest_requested", e.quest.getId(), e.quest.getName(), e.source.name().toLowerCase());
    }

    @Subscribe
    public void onQuestDeleted(QuestDeletedEvent e) {
        log("quest_deleted", EventParams.of("id", e.id));
    }

    @Subscribe
    public void onUndoDeleteQuest(UndoDeleteQuestEvent e) {
        log("undo_delete_quest", e.quest.getId(), e.quest.getName(), e.source.name().toLowerCase());
    }

    @Subscribe
    public void onShowRepeatingQuest(ShowRepeatingQuestEvent e) {
        log("show_repeating_quest_request", e.repeatingQuest.getId(), e.repeatingQuest.getName());
    }

    @Subscribe
    public void onDeleteRepeatingQuestRequest(DeleteRepeatingQuestRequestEvent e) {
        log("delete_repeating_quest_requested", e.repeatingQuest.getId(), e.repeatingQuest.getName());
    }

    @Subscribe
    public void onUndoDeleteRepeatingQuest(UndoDeleteRepeatingQuestEvent e) {
        log("undo_delete_repeating_quest", e.repeatingQuest.getId(), e.repeatingQuest.getName());
    }

    @Subscribe
    public void onUndoDeleteRepeatingQuest(RepeatingQuestDeletedEvent e) {
        log("undo_delete_repeating_quest", EventParams.of("id", e.id));
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
                .add("source", e.source.name().toLowerCase()));
    }

    @Subscribe
    public void onShowTutorial(ShowTutorialEvent e) {
        log("show_tutorial");
    }

    @Subscribe
    public void onTutorialDone(TutorialDoneEvent e) {
        log("tutorial_done");
    }

    @Subscribe
    public void onTutorialSkipped(TutorialSkippedEvent e) {
        log("tutorial_skipped");
    }

    @Subscribe
    public void onPredefinedQuestSelectedEvent(PredefinedQuestSelectedEvent e) {
        log("predefined_quest_selected", EventParams.create()
                .add("name", e.name)
                .add("source", e.source.name().toLowerCase()));
    }

    @Subscribe
    public void onPredefinedQuestDeselectedEvent(PredefinedQuestDeselectedEvent e) {
        log("predefined_quest_deselected", EventParams.create()
                .add("name", e.name)
                .add("source", e.source.name().toLowerCase()));
    }

    @Subscribe
    public void onPredefinedRepeatingQuestSelectedEvent(PredefinedRepeatingQuestSelectedEvent e) {
        log("predefined_repeating_quest_selected", EventParams.create()
                .add("raw_text", e.rawText)
                .add("source", e.source.name().toLowerCase()));
    }

    @Subscribe
    public void onPredefinedRepeatingQuestDeselectedEvent(PredefinedRepeatingQuestDeselectedEvent e) {
        log("predefined_repeating_quest_deselected", EventParams.create()
                .add("raw_text", e.rawText)
                .add("source", e.source.name().toLowerCase()));
    }

    @Subscribe
    public void onScheduleQuestRequest(ScheduleQuestRequestEvent e) {
        Quest q = e.viewModel.getQuest();
        log("schedule_quest_request", q.getId(), q.getName());
    }

    @Subscribe
    public void onRescheduleQuest(RescheduleQuestEvent e) {
        Quest q = e.calendarEvent.getQuest();
        log("reschedule_quest", q.getId(), q.getName());
    }

    @Subscribe
    public void onSuggestionAccepted(SuggestionAcceptedEvent e) {
        Quest q = e.calendarEvent.getQuest();
        log("reschedule_quest", q.getId(), q.getName());
    }

    @Subscribe
    public void onSuggestionsUnavailable(SuggestionsUnavailableEvent e) {
        Quest q = e.quest;
        log("suggestions_unavailable", q.getId(), q.getName());
    }

    @Subscribe
    public void onVersionUpdated(VersionUpdatedEvent e) {
        log("version_updated", EventParams.create()
                .add("old_version", String.valueOf(e.oldVersion))
                .add("new_version", String.valueOf(e.newVersion)));
    }

    @Subscribe
    public void onShareQuestTapped(ShareQuestEvent e) {
        log("share_quest_tapped", e.quest.getId(), e.quest.getName(), e.source.name().toLowerCase());
    }

    @Subscribe
    public void onQuestShareProviderPicked(QuestShareProviderPickedEvent e) {
        log("quest_share_provider_picked", EventParams.create()
                .add("id", e.quest.getId())
                .add("name", e.quest.getName())
                .add("provider", e.provider));
    }

    @Subscribe
    public void onInviteFriendTapped(InviteFriendEvent e) {
        log("invite_friend_tapped");
    }

    @Subscribe
    public void onToolbarCalendarTap(ToolbarCalendarTapEvent e) {
        log("toolbar_calendar_tap", EventParams.of("is_expanded", String.valueOf(e.isExpanded)));
    }

    @Subscribe
    public void onCurrentDayChanged(CurrentDayChangedEvent e) {
        log("current_day_changed", EventParams.of("new_day", e.date.toString())
                .add("source", e.source.toString()));
    }

    @Subscribe
    public void onSynCalendarCheckTapped(SyncCalendarCheckTappedEvent e) {
        log("sync_calendar_check_tapped", EventParams.of("is_checked", String.valueOf(e.isChecked)));
    }

    @Subscribe
    public void onSyncCalendarRequest(SyncCalendarRequestEvent e) {
        log("sync_calendar_request", e.source);
    }

    @Subscribe
    public void onCalendarPermissionResponse(CalendarPermissionResponseEvent e) {
        log("calendar_permission_response", EventParams.create()
                .add("response", e.response.name().toLowerCase())
                .add("source", e.source.name().toLowerCase()));
    }

    @Subscribe
    public void onRateDialogShownEvent(RateDialogShownEvent e) {
        log("rate_dialog_shown", EventParams.create()
                .add("app_run", String.valueOf(e.appRun))
                .add("date_time", e.dateTime.toString()));
    }

    @Subscribe
    public void onRateDialogLoveTapped(RateDialogLoveTappedEvent e) {
        log("rate_dialog_love_tapped", EventParams.create()
                .add("answer", e.answer.name().toLowerCase())
                .add("app_run", String.valueOf(e.appRun))
                .add("date_time", e.dateTime.toString()));
    }

    @Subscribe
    public void onRateDialogRateTapped(RateDialogRateTappedEvent e) {
        log("rate_dialog_rate_tapped", EventParams.create()
                .add("answer", e.answer.name().toLowerCase())
                .add("app_run", String.valueOf(e.appRun))
                .add("date_time", e.dateTime.toString()));
    }

    @Subscribe
    public void onRateDialogFeedbackSent(RateDialogFeedbackSentEvent e) {
        log("rate_dialog_feedback_sent", EventParams.create()
                .add("feedback", e.feedback)
                .add("app_run", String.valueOf(e.appRun))
                .add("date_time", e.dateTime.toString()));
    }

    @Subscribe
    public void onRateDialogFeedbackDeclined(RateDialogFeedbackDeclinedEvent e) {
        log("rate_dialog_feedback_declined", EventParams.create()
                .add("app_run", String.valueOf(e.appRun))
                .add("date_time", e.dateTime.toString()));
    }

    @Subscribe
    public void onHelpDialogShown(HelpDialogShownEvent e) {
        log("help_dialog_shown", EventParams.create()
                .add("screen", e.screen)
                .add("app_run", String.valueOf(e.appRun)));
    }

    @Subscribe
    public void onMoreHelpTapped(MoreHelpTappedEvent e) {
        log("help_dialog_more_tapped", EventParams.create()
                .add("screen", e.screen)
                .add("app_run", String.valueOf(e.appRun)));
    }

    @Subscribe
    public void onLevelUp(LevelUpEvent e) {
        log("level_up", EventParams.of("new_level", e.newLevel + ""));
    }

    @Subscribe
    public void onLevelDown(LevelDownEvent e) {
        log("level_down", EventParams.of("new_level", e.newLevel + ""));
    }

    @Subscribe
    public void onBuyReward(BuyRewardEvent e) {
        log("buy_reward", EventParams.create()
        .add("name", e.reward.getName())
        .add("price", e.reward.getPrice() + ""));
    }

    @Subscribe
    public void onDeleteRewardRequest(DeleteRewardRequestEvent e) {
        log("delete_reward", EventParams.create()
                .add("name", e.reward.getName())
                .add("price", e.reward.getPrice() + ""));
    }

    @Subscribe
    public void onEditRewardRequest(EditRewardRequestEvent e) {
        log("edit_reward", EventParams.create()
                .add("name", e.reward.getName())
                .add("price", e.reward.getPrice() + ""));
    }

    @Subscribe
    public void onNewRewardSaved(NewRewardSavedEvent e) {
        log("new_reward_saved", EventParams.create()
                .add("name", e.reward.getName())
                .add("price", e.reward.getPrice() + ""));
    }

    @Subscribe
    public void onAddQuestButtonTapped(AddQuestButtonTappedEvent e) {
        log("add_quest_button_tapped", e.source);
    }

    @Subscribe
    public void onAvatarPicked(AvatarPickedEvent e) {
        log("avatar_picked", EventParams.of("name", e.avatarName));
    }

    @Subscribe
    public void onItemActionsShown(ItemActionsShownEvent e) {
        log("item_actions_shown", e.source);
    }

    private FlurryEventRecordStatus log(String eventName) {
        return FlurryAgent.logEvent(eventName);
    }

    private FlurryEventRecordStatus log(String eventName, EventSource source) {
        return log(eventName, EventParams.of("source", source.name().toLowerCase()));
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
