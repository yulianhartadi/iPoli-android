package com.curiousily.ipoli.assistant;

import android.content.Context;
import android.util.Log;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.assistant.event.DidNotUnderstandEvent;
import com.curiousily.ipoli.assistant.event.DoneRespondingEvent;
import com.curiousily.ipoli.assistant.event.ReadyEvent;
import com.curiousily.ipoli.assistant.event.ReadyForQueryEvent;
import com.curiousily.ipoli.assistant.event.StartRespondingEvent;
import com.curiousily.ipoli.assistant.io.GuiOutputHandler;
import com.curiousily.ipoli.assistant.io.event.NewQueryEvent;
import com.curiousily.ipoli.assistant.io.speaker.VoiceOutputHandler;
import com.curiousily.ipoli.assistant.io.speaker.event.SpeakerReadyEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.UtteranceDoneEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.UtteranceStartEvent;
import com.curiousily.ipoli.assistant.io.speech.VoiceInputHandler;
import com.curiousily.ipoli.assistant.io.speech.event.RecognizerReadyForSpeechEvent;
import com.curiousily.ipoli.assistant.io.speech.event.SpeakerNoMatchError;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class iPoli {

    private final Context context;
    private ElizaChat chat = new ElizaChat();
    private List<InputHandler> inputHandlers = new ArrayList<>();
    private List<OutputHandler> outputHandlers = new ArrayList<>();

    public iPoli(Context context) {
        this.context = context;
        EventBus.get().register(this);
        outputHandlers.add(new VoiceOutputHandler(context));
        outputHandlers.add(new GuiOutputHandler());
        inputHandlers.add(new VoiceInputHandler(context));
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
            String welcomeMessage = context.getString(R.string.welcome_message, "Poli");
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
    public void onSpeakerNoMatchError(SpeakerNoMatchError e) {
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
        String response = chat.respond(e.getQuery());
        for (OutputHandler outputHandler : outputHandlers) {
            outputHandler.showResponse(response);
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
