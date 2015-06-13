package com.curiousily.ipoli.assistant.io.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.assistant.io.event.GetInputEvent;
import com.curiousily.ipoli.assistant.io.event.NewQueryEvent;
import com.curiousily.ipoli.assistant.io.speech.event.RecognizerReadyForSpeechEvent;
import com.curiousily.ipoli.assistant.io.speech.event.SpeakerNoMatchError;
import com.curiousily.ipoli.ui.events.ChangeInputEvent;
import com.curiousily.ipoli.ui.events.ShutdownEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class VoiceRecognizer implements RecognitionListener {

    private final SpeechRecognizer recognizer;

    public VoiceRecognizer(Context context) {
        EventBus.get().register(this);
        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
    }

    @Subscribe
    public void onGetVoiceInput(GetInputEvent e) {
        recognizer.setRecognitionListener(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizer.startListening(intent);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
//        Log.d("PoliVoice", "Ready to speak");
        post(new RecognizerReadyForSpeechEvent());
    }

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
        if (error == SpeechRecognizer.ERROR_NO_MATCH) {
            recognizer.setRecognitionListener(null);
            post(new SpeakerNoMatchError());
        }
        Log.d("PoliVoice", "Speaker error " + error);
    }

    @Override
    public void onResults(Bundle results) {
        String input = getInput(results);
        fireChangeInputEvent(input);
        post(new NewQueryEvent(input));
    }

    private String getInput(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        return matches.get(0);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        fireChangeInputEvent(getInput(partialResults));
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    private void fireChangeInputEvent(String input) {
        post(new ChangeInputEvent(input));
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

    @Subscribe
    public void onShutdown(ShutdownEvent e) {
        recognizer.destroy();
    }
}
