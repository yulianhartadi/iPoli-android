package com.curiousily.ipoli.schedule.ui.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 10/1/15.
 */
public class ChangeToolbarTitleEvent {
    public final String text;

    public ChangeToolbarTitleEvent(String text) {
        this.text = text;
    }
}
