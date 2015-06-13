package com.curiousily.ipoli.io.speaker;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.io.event.NewResponseEvent;
import com.curiousily.ipoli.io.speaker.event.SpeakerReadyEvent;
import com.curiousily.ipoli.io.speaker.event.UtteranceDoneEvent;
import com.curiousily.ipoli.io.speaker.event.UtteranceStartEvent;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class Speaker extends UtteranceProgressListener implements TextToSpeech.OnInitListener {

    private final Activity activity;
    private TextToSpeech textToSpeech;

    public Speaker(Activity activity) {
        this.activity = activity;
        EventBus.get().register(this);
        textToSpeech = new TextToSpeech(activity, this);
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

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                post(new UtteranceStartEvent());
            }
        });
    }

    @Override
    public void onDone(String utteranceId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                post(new UtteranceDoneEvent());
            }
        });
//        Log.d("PoliVoice", "Utterance done");

    }

    @Override
    public void onError(String utteranceId) {

    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

    public void onDestroy() {
        textToSpeech.shutdown();
    }
}
