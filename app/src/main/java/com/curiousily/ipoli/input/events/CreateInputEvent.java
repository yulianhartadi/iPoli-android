package com.curiousily.ipoli.input.events;

import com.curiousily.ipoli.input.Input;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class CreateInputEvent {
    public final Input input;

    public CreateInputEvent(Input input) {
        this.input = input;
    }
}
