package com.curiousily.ipoli.assistant.io.speaker;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.assistant.io.event.NewResponseEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.SpeakerReadyEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.UtteranceDoneEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.UtteranceStartEvent;
import com.curiousily.ipoli.ui.events.ShutdownEvent;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class Speaker extends UtteranceProgressListener implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;

    public Speaker(Context context) {
        EventBus.get().register(this);
        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setLanguage(Locale.US);
    }

    @Subscribe
    public void onNewAnswer(NewResponseEvent e) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "response");
        textToSpeech.speak(e.getResponse(), TextToSpeech.QUEUE_ADD, params);
    }

    @Override
    public void onInit(int status) {
        post(new SpeakerReadyEvent());
        textToSpeech.setOnUtteranceProgressListener(this);
    }

    @Override
    public void onStart(String utteranceId) {
//        Log.d("PoliVoice", "Utterance start");
//        post(new UtteranceStartEvent());

        post(new UtteranceStartEvent());
    }

    @Override
    public void onDone(String utteranceId) {
        post(new UtteranceDoneEvent());

//        Log.d("PoliVoice", "Utterance done");

    }

    @Override
    public void onError(String utteranceId) {

    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

    @Subscribe
    public void onShutdown(ShutdownEvent e) {
        textToSpeech.shutdown();
    }
}
