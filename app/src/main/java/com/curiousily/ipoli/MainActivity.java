package com.curiousily.ipoli;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.curiousily.ipoli.events.Author;
import com.curiousily.ipoli.events.ChangeInputEvent;
import com.curiousily.ipoli.events.NewMessageEvent;
import com.curiousily.ipoli.recognition.RecognitionAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private SpeechRecognizer recognizer;
    private RecognitionAdapter recognitionListener = new RecognitionAdapter() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            InputFragment fragment = new InputFragment();
            fragment.show(getSupportFragmentManager(), InputFragment.FRAGMENT_TAG);
        }

        @Override
        public void onResults(Bundle results) {
            voiceButton.setImageResource(R.drawable.ic_done);
            String scores = "";
            for (int i = 0; i < results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES).length; i++) {
                scores += results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)[i] + "\n";
            }
            Log.d("PoliVoiceScores", scores);

            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            String text = "";
            for (String result : matches)
                text += result + "\n";

            Log.d("PoliVoiceInput", text);
            String input = matches.get(0);

            post(new ChangeInputEvent(input));

            String response = chat.respond(input);
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "response");
            textToSpeech.speak(response, TextToSpeech.QUEUE_ADD, params);

            post(new NewMessageEvent(input, Author.User));
            post(new NewMessageEvent(response, Author.iPoli));
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            post(new ChangeInputEvent(matches.get(0)));
        }
    };

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.voice_button)
    FloatingActionButton voiceButton;

    private ElizaChat chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        chat = new ElizaChat();

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);

        if (savedInstanceState != null) {
            return;
        }

        ConversationFragment firstFragment = new ConversationFragment();

        firstFragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, firstFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PoliBus.get().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PoliBus.get().unregister(this);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status) {
        textToSpeech.speak(getString(R.string.welcome_message), TextToSpeech.QUEUE_ADD, null);
        post(new NewMessageEvent(getString(R.string.welcome_message), Author.iPoli));
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        voiceButton.setImageResource(R.drawable.ic_mic_white_48dp);
                        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(InputFragment.FRAGMENT_TAG);
                        fm.remove(fragment);
                        fm.commit();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
        Log.d("PoliVoice", "Init " + status);
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(recognitionListener);
        findViewById(R.id.voice_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                recognizer.startListening(intent);
            }
        });
    }

    private void post(Object event) {
        PoliBus.get().post(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textToSpeech.shutdown();
        recognizer.destroy();
    }
}
