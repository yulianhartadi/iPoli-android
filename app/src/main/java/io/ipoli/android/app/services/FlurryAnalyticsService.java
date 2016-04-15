package io.ipoli.android.app.services;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryEventRecordStatus;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.InvitationScreenRequestedAutomaticInviteEvent;
import io.ipoli.android.app.events.PlayerRequestedInviteEvent;
import io.ipoli.android.app.events.PlayerTappedInviteLogoEvent;
import io.ipoli.android.app.events.RemotePlayerCreatedEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.quest.events.CompleteQuestEvent;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DateSelectedEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.QuestDraggedEvent;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.QuestsPlannedEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.StartQuestEvent;
import io.ipoli.android.quest.events.StopQuestEvent;
import io.ipoli.android.quest.events.TimeSelectedEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;
import io.ipoli.android.quest.events.UnscheduledQuestDraggedEvent;
import io.ipoli.android.tutorial.events.ShowTutorialItemEvent;

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
    public void onRemotePlayerCreated(RemotePlayerCreatedEvent e) {
        FlurryAgent.setUserId(e.playerId);
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
    public void onPlayerTappedLogo(PlayerTappedInviteLogoEvent e) {
        log("invite_logo_tapped");
    }

    @Subscribe
    public void onScreenShown(ScreenShownEvent e) {
        log("screen_shown", EventParams.of("name", e.screenName));
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        log("quest_created", new HashMap<String, String>() {{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
            put("raw_text", e.quest.getRawText());
        }});
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        log("edit_quest_requested", new HashMap<String, String>() {{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
            put("source", e.source);
        }});
    }

    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        log("complete_quest_request", new HashMap<String, String>() {{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
            put("source", e.source);
        }});
    }


    @Subscribe
    public void onUndoCompletedQuest(UndoCompletedQuestEvent e) {
        log("undo_completed_quest", new HashMap<String, String>() {{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
        }});
    }

    @Subscribe
    public void onQuestSnoozed(QuestSnoozedEvent e) {
        log("quest_snoozed", new HashMap<String, String>() {{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
        }});
    }

    @Subscribe
    public void onUnscheduledQuestDragged(UnscheduledQuestDraggedEvent e) {
        log("unscheduled_quest_dragged", new HashMap<String, String>() {{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
        }});
    }

    @Subscribe
    public void onQuestDragged(QuestDraggedEvent e) {
        log("quest_dragged", EventParams.of("id", e.quest.getId()).add("name", e.quest.getName()));
    }

    @Subscribe
    public void onShowQuest(ShowQuestEvent e){
        log("quest_shown", EventParams.of("id", e.quest.getId()).add("name", e.quest.getName()));
    }

    @Subscribe
    public void onFeedbackClick(FeedbackTapEvent e) {
        log("feedback_tapped");
    }

    @Subscribe
    public void onContactUsClick(ContactUsTapEvent e) {
        log("contact_us_tapped");
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
//        track(createEventBuilder("quest", "edit")
//                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }


    @Subscribe
    public void onDeleteQuestRequest(DeleteQuestRequestEvent e) {
//        track(createEventBuilder("quest", "delete-request")
//                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }


    @Subscribe
    public void onUndoDeleteQuest(UndoDeleteQuestEvent e) {
//        track(createEventBuilder("quest", "undo-delete")
//                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onTimeSelected(TimeSelectedEvent e) {
//        trackUIEvent("quest", "time-selected");
    }

    @Subscribe
    public void onDateSelected(DateSelectedEvent e) {
//        trackUIEvent("quest", "date-selected");
    }

    @Subscribe
    public void onQuestsPlanned(QuestsPlannedEvent e) {
//        List<String> questNames = new ArrayList<>();
//        for (Quest q : e.quests) {
//            questNames.add(q.getName());
//        }
//        track(createEventBuilder("quest", "planned")
//                .setCustomDimension(QUESTS_DIMENSION_INDEX, TextUtils.join("\n", questNames)));
    }

    @Subscribe
    public void onStartQuest(StartQuestEvent e) {
//        track(createEventBuilder("quest", "start")
//                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onStopQuest(StopQuestEvent e) {
//        track(createEventBuilder("quest", "stop")
//                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onCompleteQuest(CompleteQuestEvent e) {
//        track(createEventBuilder("quest", "complete")
//                .setCustomDimension(NAME_DIMENSION_INDEX, e.quest.getName()));
    }

    @Subscribe
    public void onShowTutorialItem(ShowTutorialItemEvent e) {
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
}
