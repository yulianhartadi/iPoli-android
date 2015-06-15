package com.curiousily.ipoli;

import android.content.res.ColorStateList;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.curiousily.ipoli.assistant.event.DoneRespondingEvent;
import com.curiousily.ipoli.assistant.event.ReadyEvent;
import com.curiousily.ipoli.assistant.event.ReadyForQueryEvent;
import com.curiousily.ipoli.assistant.event.StartRespondingEvent;
import com.curiousily.ipoli.assistant.iPoli;
import com.curiousily.ipoli.assistant.io.speech.event.RmsChangedEvent;
import com.curiousily.ipoli.ui.ConversationFragment;
import com.curiousily.ipoli.ui.InputFragment;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class MainActivity extends AppCompatActivity {

    public static final int VOICE_INPUT_ANIMATION_DURATION_MS = 50;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.voice_button)
    FloatingActionButton voiceButton;

    @InjectView(R.id.nav_view)
    NavigationView navigationView;

    private iPoli iPoli;

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
        voiceButton.setImageResource(0);
        voiceButton.setEnabled(false);
        iPoli.requestInput();
    }

    private void addConversionFragment() {
        ConversationFragment firstFragment = new ConversationFragment();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, firstFragment).commit();
    }

    private void initAssistant() {
        iPoli = new iPoli(this);
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
    public void onAssistantReady(ReadyEvent e) {
        voiceButton.setVisibility(View.VISIBLE);
        voiceButton.setEnabled(true);
    }

    @Subscribe
    public void onAssistantStartedResponding(StartRespondingEvent e) {
        Log.d("PoliVoice", "Started responding");
        voiceButton.setImageResource(R.drawable.ic_volume_up_white_48dp);
        voiceButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_blue_a400)));
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(InputFragment.FRAGMENT_TAG);
        if (fragment != null) {
            removeInputFragment(fragment);
        }
        voiceButton.clearAnimation();
        voiceButton.animate().setDuration(VOICE_INPUT_ANIMATION_DURATION_MS).scaleX(1.0f).scaleY(1.0f).start();
        voiceButton.setEnabled(false);
    }

    private void removeInputFragment(Fragment fragment) {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        fm.remove(fragment);
        fm.commit();
    }

    @Subscribe
    public void onAssistantStoppedResponding(DoneRespondingEvent e) {
        voiceButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_red_a400)));
        voiceButton.setEnabled(true);
        voiceButton.setImageResource(R.drawable.ic_mic_white_48dp);
    }

    @Subscribe
    public void onAssistantReadyForQuery(ReadyForQueryEvent e) {
        InputFragment fragment = new InputFragment();
        fragment.show(getSupportFragmentManager(), InputFragment.FRAGMENT_TAG);
        voiceButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_red_a100)));
    }

    @Subscribe
    public void onRmsChanged(RmsChangedEvent e) {
        float height = e.getRmsdB();
        height /= 5;
        height = Math.max(Math.min(height, 2.0f), 0.5f);
        voiceButton.animate().setDuration(VOICE_INPUT_ANIMATION_DURATION_MS).scaleX(height).scaleY(height).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iPoli.shutdown();
    }
}
