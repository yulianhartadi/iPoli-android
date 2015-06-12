package com.curiousily.ipoli.recognition;

import android.os.Bundle;
import android.speech.RecognitionListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public abstract class RecognitionAdapter implements RecognitionListener {
    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
