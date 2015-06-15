package com.curiousily.ipoli.assistant.io.speech.event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/15.
 */
public class RmsChangedEvent {
    private final float rmsdB;

    public RmsChangedEvent(float rmsdB) {
        this.rmsdB = rmsdB;
    }

    public float getRmsdB() {
        return rmsdB;
    }
}
