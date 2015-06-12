package com.curiousily.ipoli.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class ChangeInputEvent {
    private final String input;

    public ChangeInputEvent(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }
}
