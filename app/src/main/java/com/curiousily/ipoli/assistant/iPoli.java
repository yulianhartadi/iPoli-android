package com.curiousily.ipoli.assistant;

import android.content.Context;
import android.util.Log;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.assistant.data.StorageManager;
import com.curiousily.ipoli.assistant.data.events.NewResponseEvent;
import com.curiousily.ipoli.assistant.data.models.Query;
import com.curiousily.ipoli.assistant.data.models.Response;
import com.curiousily.ipoli.assistant.events.DidNotUnderstandEvent;
import com.curiousily.ipoli.assistant.events.DoneRespondingEvent;
import com.curiousily.ipoli.assistant.events.ReadyEvent;
import com.curiousily.ipoli.assistant.events.ReadyForQueryEvent;
import com.curiousily.ipoli.assistant.events.StartRespondingEvent;
import com.curiousily.ipoli.assistant.intents.ChatIntentHandler;
import com.curiousily.ipoli.assistant.intents.IntentHandler;
import com.curiousily.ipoli.assistant.intents.LogIntentHandler;
import com.curiousily.ipoli.assistant.intents.events.IntentProcessedEvent;
import com.curiousily.ipoli.assistant.io.GuiOutputHandler;
import com.curiousily.ipoli.assistant.io.events.NewQueryEvent;
import com.curiousily.ipoli.assistant.io.speaker.VoiceOutputHandler;
import com.curiousily.ipoli.assistant.io.speaker.events.SpeakerReadyEvent;
import com.curiousily.ipoli.assistant.io.speaker.events.UtteranceDoneEvent;
import com.curiousily.ipoli.assistant.io.speaker.events.UtteranceStartEvent;
import com.curiousily.ipoli.assistant.io.speech.VoiceInputHandler;
import com.curiousily.ipoli.assistant.io.speech.events.RecognizerReadyForSpeechEvent;
import com.curiousily.ipoli.assistant.io.speech.events.SpeechNoMatchEvent;
import com.curiousily.ipoli.auth.FirebaseUserAuthenticator;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class iPoli {

    private final Context context;
    private final List<InputHandler> inputHandlers = new ArrayList<>();
    private final List<OutputHandler> outputHandlers = new ArrayList<>();
    private final List<IntentHandler> intentHandlers = new ArrayList<>();
    private final StorageManager storageManager;

    public iPoli(Context context) {
        this.context = context;
        EventBus.get().register(this);
        outputHandlers.add(new VoiceOutputHandler(context));
        outputHandlers.add(new GuiOutputHandler());
        inputHandlers.add(new VoiceInputHandler(context));
        intentHandlers.add(new LogIntentHandler(context));
        intentHandlers.add(new ChatIntentHandler(context));
        storageManager = new StorageManager();
    }

    public void requestInput() {
        for (InputHandler inputHandler : inputHandlers) {
            inputHandler.requestInput();
        }
    }

    @Subscribe
    public void onSpeakerReady(SpeakerReadyEvent e) {
        Log.d("PoliVoice", "Ready to speak");
        post(new ReadyEvent());
        for (OutputHandler h : outputHandlers) {
            String welcomeMessage = context.getString(R.string.welcome_message);
            h.showResponse(welcomeMessage);
        }
    }

    @Subscribe
    public void onUtteranceStart(UtteranceStartEvent e) {
        Log.d("PoliVoice", "Utterance start");
        post(new StartRespondingEvent());
    }

    @Subscribe
    public void onUtteranceDone(UtteranceDoneEvent e) {
        post(new DoneRespondingEvent());
    }

    @Subscribe
    public void onRecognizerReadyForSpeech(RecognizerReadyForSpeechEvent e) {
        post(new ReadyForQueryEvent());
    }

    @Subscribe
    public void onSpeechNoMatchError(SpeechNoMatchEvent e) {
        post(new DidNotUnderstandEvent());
        for (OutputHandler h : outputHandlers) {
            String m = context.getString(R.string.speech_not_recognized_error);
            h.showResponse(m);
        }
    }

    @Subscribe
    public void onNewQuery(NewQueryEvent e) {
        for (OutputHandler outputHandler : outputHandlers) {
            outputHandler.showQuery(e.getQuery());
        }
        processQuery(e.getQuery());
    }

    private void processQuery(String query) {
        storageManager.save("queries", Query.from(query, getUserId()));
    }

    @Subscribe
    public void onNewResponse(NewResponseEvent e) {
        Response r = e.getResponse();
        for (IntentHandler intentHandler : intentHandlers) {
            if (intentHandler.canHandle(r.intent)) {
                intentHandler.process(r.text);
                return;
            }
        }
    }

    protected String getUserId() {
        return FirebaseUserAuthenticator.getUser().id;
    }

    @Subscribe
    public void onIntentProcessed(IntentProcessedEvent e) {
        for (OutputHandler outputHandler : outputHandlers) {
            outputHandler.showResponse(e.getResponse());
        }
    }

    public void shutdown() {
        for (InputHandler h : inputHandlers) {
            h.shutdown();
        }
        for (OutputHandler h : outputHandlers) {
            h.shutdown();
        }
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

}
