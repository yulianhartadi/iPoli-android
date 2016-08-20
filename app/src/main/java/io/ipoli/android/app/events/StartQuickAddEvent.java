package io.ipoli.android.app.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/16.
 */
public class StartQuickAddEvent {
    public String additionalText;

    public StartQuickAddEvent(String additionalText) {
        this.additionalText = additionalText;
    }
}
