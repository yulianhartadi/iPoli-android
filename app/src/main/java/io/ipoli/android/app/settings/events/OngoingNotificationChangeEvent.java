package io.ipoli.android.app.settings.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/16.
 */
public class OngoingNotificationChangeEvent {
    public final boolean isEnabled;

    public OngoingNotificationChangeEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
