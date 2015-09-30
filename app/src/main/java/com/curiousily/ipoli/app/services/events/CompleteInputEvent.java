package com.curiousily.ipoli.app.services.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/27/15.
 */
public class CompleteInputEvent {
    public String input;

    public CompleteInputEvent(String input) {
        this.input = input;
    }
}
