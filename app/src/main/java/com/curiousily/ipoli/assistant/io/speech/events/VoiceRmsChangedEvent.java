package com.curiousily.ipoli.assistant.io.speech.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/15.
 */
public class VoiceRmsChangedEvent {
    private final float rmsdB;

    public VoiceRmsChangedEvent(float rmsdB) {
        this.rmsdB = rmsdB;
    }

    public float getRmsdB() {
        return rmsdB;
    }
}
