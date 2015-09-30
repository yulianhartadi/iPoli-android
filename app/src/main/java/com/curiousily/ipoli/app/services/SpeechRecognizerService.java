package com.curiousily.ipoli.app.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.app.services.events.CompleteInputEvent;
import com.curiousily.ipoli.app.services.events.NoInputMatchEvent;
import com.curiousily.ipoli.app.services.events.PartialInputEvent;
import com.curiousily.ipoli.app.services.events.ReadyForSpeechInputEvent;
import com.curiousily.ipoli.app.services.events.RequestSpeechInputEvent;
import com.curiousily.ipoli.app.services.events.RmsChangedEvent;
import com.curiousily.ipoli.app.services.events.SpeechStartedEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/27/15.
 */
public class SpeechRecognizerService implements RecognitionListener {
    private final SpeechRecognizer recognizer;

    public SpeechRecognizerService(Context context) {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        EventBus.post(new ReadyForSpeechInputEvent());
    }

    @Override
    public void onBeginningOfSpeech() {
        EventBus.post(new SpeechStartedEvent());
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        EventBus.post(new RmsChangedEvent(rmsdB));
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("PoliVoice", "Buffer received");
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int error) {
        if (error == SpeechRecognizer.ERROR_NO_MATCH) {
            recognizer.setRecognitionListener(null);
            EventBus.post(new NoInputMatchEvent());
        }
        Log.d("PoliVoice", "Speaker error " + error);
    }

    @Override
    public void onResults(Bundle results) {
        recognizer.setRecognitionListener(null);
        String input = getInput(results);
        fireChangeInputEvent(input);
        EventBus.post(new CompleteInputEvent(input));
    }

    private String getInput(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null || matches.isEmpty()) {
            return "";
        }
        return matches.get(0);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d("PoliVoice", "Partial results");
        fireChangeInputEvent(getInput(partialResults));
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    private void fireChangeInputEvent(String input) {
        EventBus.post(new PartialInputEvent(input));
    }

    public void shutdown() {
        recognizer.setRecognitionListener(null);
        recognizer.destroy();
    }

    @Subscribe
    public void onRequestSpeechInput(RequestSpeechInputEvent e) {
        recognizer.setRecognitionListener(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizer.startListening(intent);
    }
}
