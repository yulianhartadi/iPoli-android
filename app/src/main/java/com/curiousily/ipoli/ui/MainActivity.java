package com.curiousily.ipoli.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.assistant.Assistant;
import com.curiousily.ipoli.assistant.io.event.GetInputEvent;
import com.curiousily.ipoli.assistant.io.event.NewResponseEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.SpeakerReadyEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.UtteranceDoneEvent;
import com.curiousily.ipoli.assistant.io.speaker.event.UtteranceStartEvent;
import com.curiousily.ipoli.assistant.io.speech.event.RecognizerReadyForSpeechEvent;
import com.curiousily.ipoli.assistant.io.speech.event.SpeakerNoMatchError;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.voice_button)
    FloatingActionButton voiceButton;

    @InjectView(R.id.nav_view)
    NavigationView navigationView;
    private Assistant assistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initUI(savedInstanceState);
        initAssistant();
    }

    private void initUI(Bundle savedInstanceState) {
        voiceButton.setEnabled(false);
        setupActionBar();
        setupDrawerContent();
        if (savedInstanceState != null) {
            return;
        }
        addConversionFragment();
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.voice_button)
    public void onVoiceButtonClick() {
        post(new GetInputEvent());
    }

    private void addConversionFragment() {
        ConversationFragment firstFragment = new ConversationFragment();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, firstFragment).commit();
    }

    private void initAssistant() {
        assistant = new Assistant(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.get().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.get().unregister(this);
    }

    private void setupDrawerContent() {
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

    @Subscribe
    public void onAnswerReceived(NewResponseEvent e) {
//        Log.d("PoliVoice", "Answer received");
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(InputFragment.FRAGMENT_TAG);
        if (fragment != null) {
            removeInputFragment(fragment);
        }
    }

    private void removeInputFragment(Fragment fragment) {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        fm.remove(fragment);
        fm.commit();
    }

    @Subscribe
    public void onSpeakerReady(SpeakerReadyEvent e) {
        voiceButton.setEnabled(true);
        String welcomeMessage = getString(R.string.welcome_message, "Poli");
        post(new NewResponseEvent(welcomeMessage));
    }

    @Subscribe
    public void onUtteranceStart(UtteranceStartEvent e) {
        voiceButton.setEnabled(false);
        voiceButton.setImageResource(R.drawable.ic_volume_up_white_24dp);
    }

    @Subscribe
    public void onUtteranceDone(UtteranceDoneEvent e) {
        voiceButton.setEnabled(true);
        voiceButton.setImageResource(R.drawable.ic_mic_white_48dp);
    }

    @Subscribe
    public void onRecognizerReadyForSpeech(RecognizerReadyForSpeechEvent e) {
        InputFragment fragment = new InputFragment();
        fragment.show(getSupportFragmentManager(), InputFragment.FRAGMENT_TAG);
    }

    @Subscribe
    public void onSpeakerNoMatchError(SpeakerNoMatchError e) {
        post(new NewResponseEvent(getString(R.string.speech_not_recognized_error)));
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        assistant.shutdown();

    }
}
