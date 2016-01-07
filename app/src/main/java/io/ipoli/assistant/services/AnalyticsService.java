package io.ipoli.assistant.services;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class AnalyticsService {

    public static final String CATEGORY_UI = "UI";
    private final Tracker tracker;

    public AnalyticsService(Tracker tracker) {

        this.tracker = tracker;
    }

    @Subscribe
    public void onCreateQuest(CreateQuestEvent e) {
        trackUIEvent("quest", "create");
    }

    @Subscribe
    public void onUpdateQuest(UpdateQuestEvent e) {
        trackUIEvent("quest", "update", e.quest.id);
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
