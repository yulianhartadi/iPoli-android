package io.ipoli.android.app.settings.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeReminderChangeEvent {
    public final boolean enabled;

    public DailyChallengeReminderChangeEvent(boolean enabled) {
        this.enabled = enabled;
    }
}
