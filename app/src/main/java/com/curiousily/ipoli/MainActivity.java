package com.curiousily.ipoli;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.curiousily.ipoli.app.events.TrackEvent;
import com.curiousily.ipoli.assistant.events.DoneRespondingEvent;
import com.curiousily.ipoli.assistant.events.ReadyEvent;
import com.curiousily.ipoli.assistant.events.ReadyForQueryEvent;
import com.curiousily.ipoli.assistant.events.StartRespondingEvent;
import com.curiousily.ipoli.assistant.iPoli;
import com.curiousily.ipoli.assistant.io.event.NewMessageEvent;
import com.curiousily.ipoli.assistant.io.speech.event.VoiceRmsChangedEvent;
import com.curiousily.ipoli.auth.AuthListener;
import com.curiousily.ipoli.auth.FirebaseUserAuthenticator;
import com.curiousily.ipoli.auth.events.UserAuthenticatedEvent;
import com.curiousily.ipoli.models.Message;
import com.curiousily.ipoli.models.User;
import com.curiousily.ipoli.ui.AlertDialogFragment;
import com.curiousily.ipoli.ui.ConversationFragment;
import com.curiousily.ipoli.ui.InputFragment;
import com.curiousily.ipoli.ui.events.AlertDialogClickEvent;
import com.curiousily.ipoli.ui.events.ChangeInputEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class MainActivity extends AppCompatActivity implements AuthListener {

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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isOnline()) {
            showAlertDialog(R.string.no_internet_dialog_title, R.string.no_internet_dialog_message);
            return;
        }
        FirebaseUserAuthenticator userAuthenticator = new FirebaseUserAuthenticator();
        userAuthenticator.authenticateAnonymousUser(this);
        ButterKnife.inject(this);
        initUI(savedInstanceState);
    }

    private void showAlertDialog(int title, int message) {
        DialogFragment newFragment = AlertDialogFragment.newInstance(
                title,
                message);
        newFragment.show(getSupportFragmentManager(), Constants.ALERT_DIALOG_TAG);

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
        voiceButton.setImageResource(R.drawable.ic_volume_up_white_48dp);
        voiceButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_blue_a400)));
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(InputFragment.FRAGMENT_TAG);
        if (fragment != null) {
            removeInputFragment(fragment);
        }
        voiceButton.animate().cancel();
        voiceButton.setScaleX(1.0f);
        voiceButton.setScaleY(1.0f);
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
    public void onRmsChanged(VoiceRmsChangedEvent e) {
        float rmsdB = e.getRmsdB();
        rmsdB /= Constants.RMS_FILTER_VOICE_ANIMATION;
        rmsdB = Math.max(Math.min(rmsdB, Constants.MAX_VOICE_ANIMATION_SCALE), Constants.MIN_VOICE_ANIMATION_SCALE);
        voiceButton.animate().setDuration(Constants.VOICE_INPUT_ANIMATION_DURATION_MS).scaleX(rmsdB).scaleY(rmsdB).start();
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent e) {
        Message message = e.getMessage();
        String messageType = message.author == ChangeInputEvent.Author.User ? "query" : "response";
        post(TrackEvent.from(new HitBuilders.EventBuilder(
                "assistant", messageType).setLabel(message.text).build()));
    }

    @Subscribe
    public void onAlertDialogClick(AlertDialogClickEvent e) {
        finish();
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    @Subscribe
    public void onUserAuthenticated(UserAuthenticatedEvent e) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iPoli != null) {
            iPoli.shutdown();
        }
    }

    @Override
    public void onUserAuthenticated(User user) {
        initAssistant();

    }

    @Override
    public void onUnableToAuthenticateUser() {
        showAlertDialog(R.string.user_login_error_dialog_title, R.string.user_login_error_dialog_message);
    }
}
