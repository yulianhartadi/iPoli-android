package com.curiousily.ipoli.assistant.io.speaker;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.assistant.OutputHandler;
import com.curiousily.ipoli.assistant.io.speaker.event.SpeakerReadyEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.UtteranceDoneEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.UtteranceStartEvent;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class VoiceOutputHandler extends UtteranceProgressListener implements TextToSpeech.OnInitListener, OutputHandler {

    private TextToSpeech textToSpeech;

    public VoiceOutputHandler(Context context) {
        EventBus.get().register(this);
        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setLanguage(Locale.US);
    }

    @Override
    public void onInit(int status) {
        post(new SpeakerReadyEvent());
        textToSpeech.setOnUtteranceProgressListener(this);
    }

    @Override
    public void onStart(String utteranceId) {
        post(new UtteranceStartEvent());
    }

    @Override
    public void onDone(String utteranceId) {
        post(new UtteranceDoneEvent());
    }

    @Override
    public void onError(String utteranceId) {

    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

    @Override
    public void shutdown() {
        textToSpeech.shutdown();
    }

    @Override
    public void showResponse(String response) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "response");
        textToSpeech.speak(response, TextToSpeech.QUEUE_ADD, params);
    }

    @Override
    public void showQuery(String query) {

    }
}
