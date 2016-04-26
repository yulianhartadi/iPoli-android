package io.ipoli.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.squareup.otto.Bus;

import java.util.Random;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.InvitationScreenRequestedAutomaticInviteEvent;
import io.ipoli.android.app.events.InviteLogoTappedEvent;
import io.ipoli.android.app.events.PlayerRequestedInviteEvent;
import io.ipoli.android.app.utils.EmailUtils;

public class InviteOnlyActivity extends BaseActivity {

    private SharedPreferences preferences;

    @Inject
    Bus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
        setContentView(R.layout.activity_invite_only);
        ButterKnife.bind(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("is_invited", false)) {
            startMainActivity();
            return;
        }

        Random random = new Random();
        int r = random.nextInt(100);

        SharedPreferences.Editor editor = preferences.edit();
        boolean isInvited = r < Constants.INVITE_PLAYER_PROBABILITY;
        editor.putBoolean("is_invited", isInvited);
        editor.apply();

        eventBus.post(new InvitationScreenRequestedAutomaticInviteEvent(isInvited));

        if (isInvited) {
            startMainActivity();
            return;
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.md_blue_700));
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @OnClick(R.id.invite_button)
    public void onInviteClick(Button button) {
        eventBus.post(new PlayerRequestedInviteEvent());
        EmailUtils.send(this, "iPoli invitation request", "Request your invite");
    }

    @OnClick(R.id.invite_logo_image)
    public void onLogoClick(View view) {
        eventBus.post(new InviteLogoTappedEvent());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_invited", true);
        editor.apply();
        startMainActivity();
    }
}
