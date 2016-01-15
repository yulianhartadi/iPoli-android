package io.ipoli.android.assistant.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/14/16.
 */
public class NewFeedbackEvent {
    public final String text;

    public NewFeedbackEvent(String text) {
        this.text = text;
    }
}
