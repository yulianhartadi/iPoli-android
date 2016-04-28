package io.ipoli.android.app.ui.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/28/16.
 */
public class ShowLoaderEvent {

    public final String message;

    public ShowLoaderEvent() {
        this("");
    }

    public ShowLoaderEvent(String message) {
        this.message = message;
    }

}
