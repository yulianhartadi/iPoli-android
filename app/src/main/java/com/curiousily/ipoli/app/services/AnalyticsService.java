package com.curiousily.ipoli.app.services;

import com.curiousily.ipoli.quest.events.CreateQuestEvent;
import com.curiousily.ipoli.quest.services.events.UpdateQuestEvent;
import com.curiousily.ipoli.schedule.events.QuestPostponedEvent;
import com.curiousily.ipoli.schedule.events.QuestRatedEvent;
import com.curiousily.ipoli.schedule.events.UpdateDailyScheduleEvent;
import com.curiousily.ipoli.ui.events.UserUsedInviteEvent;
import com.curiousily.ipoli.user.events.UserLoadedEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/11/15.
 */
public class AnalyticsService {

    public static final String CATEGORY_UI = "UI";
    private final Tracker tracker;

    public AnalyticsService(Tracker tracker) {

        this.tracker = tracker;
    }

    @Subscribe
    public void onUserLoader(UserLoadedEvent e) {
        tracker.set("&uid", e.user.id);
        trackUIEvent("user", "sign-in");
    }

    @Subscribe
    public void onUserUsedInvite(UserUsedInviteEvent e) {
        trackUIEvent("user", "used-invite");
    }

    @Subscribe
    public void onCreateQuest(CreateQuestEvent e) {
        trackUIEvent("quest", "create");
    }

    @Subscribe
    public void onUpdateQuest(UpdateQuestEvent e) {
        trackUIEvent("quest", "update", e.quest.id);
    }

    @Subscribe
    public void onQuestRated(QuestRatedEvent e) {
        trackUIEvent("quest", "rated", e.quest.id);
    }

    @Subscribe
    public void onQuestPostponed(QuestPostponedEvent e) {
        trackUIEvent("quest", "postponed", e.quest.id);
    }

    @Subscribe
    public void onUpdateDailySchedule(UpdateDailyScheduleEvent e) {
        trackUIEvent("schedule", "updated");
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
