package io.ipoli.android.app.services;

import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;

import java.util.HashMap;

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
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.QuestsPlannedEvent;
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
public class FlurryAnalyticsService implements AnalyticsService {
    @Subscribe
    public void onRemotePlayerCreated(RemotePlayerCreatedEvent e) {
        FlurryAgent.setUserId(e.playerId);
    }

    @Subscribe
    public void onInvitationScreenRequestAutomaticInviteEvent(InvitationScreenRequestedAutomaticInviteEvent e) {
        FlurryAgent.logEvent("invitation_screen_requested_invite", new HashMap<String, String>() {{
            put("is_invite_received", e.isInviteReceived + "");
        }});
    }

    @Subscribe
    public void onPlayerRequestedInvite(PlayerRequestedInviteEvent e) {
        FlurryAgent.logEvent("invite_requested");
    }

    @Subscribe
    public void onPlayerTappedLogo(PlayerTappedInviteLogoEvent e) {
        FlurryAgent.logEvent("invite_logo_tapped");
    }

    @Subscribe
    public void onScreenShown(ScreenShownEvent e) {
        FlurryAgent.logEvent("screen_shown", new HashMap<String, String>() {{
            put("name", e.screenName);
        }});
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        FlurryAgent.logEvent("quest_created", new HashMap<String, String>(){{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
            put("raw_text", e.quest.getRawText());
        }});
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        FlurryAgent.logEvent("edit_quest_requested", new HashMap<String, String>(){{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
            put("source", e.source);
        }});
    }


    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        FlurryAgent.logEvent("complete_quest_request", new HashMap<String, String>(){{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
            put("source", e.source);
        }});
    }

    @Subscribe
    public void onUndoCompletedQuest(UndoCompletedQuestEvent e) {
        FlurryAgent.logEvent("undo_completed_quest", new HashMap<String, String>(){{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
        }});
    }

    @Subscribe
    public void onQuestSnoozed(QuestSnoozedEvent e) {
        FlurryAgent.logEvent("quest_snoozed", new HashMap<String, String>(){{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
        }});
    }

    @Subscribe
    public void onUnscheduledQuestDragged(UnscheduledQuestDraggedEvent e) {
        FlurryAgent.logEvent("unscheduled_quest_dragged", new HashMap<String, String>(){{
            put("id", e.quest.getId());
            put("name", e.quest.getName());
        }});
    }

    @Subscribe
    public void onFeedbackClick(FeedbackTapEvent e) {
        FlurryAgent.logEvent("feedback_tapped");
    }

    @Subscribe
    public void onContactUsClick(ContactUsTapEvent e) {
        FlurryAgent.logEvent("contact_us_tapped");
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
}
