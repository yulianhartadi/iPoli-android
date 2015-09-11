package com.curiousily.ipoli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.curiousily.ipoli.schedule.DailyScheduleActivity;

import java.util.Random;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class InviteOnlyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_only);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("is_invited", false)) {
            startMainActivity();
            return;
        }

        Random random = new Random();
        int r = random.nextInt(100);

        SharedPreferences.Editor editor = preferences.edit();
        boolean isInvited = r < 20;
        editor.putBoolean("is_invited", isInvited);
        editor.apply();

        if (isInvited) {
            startMainActivity();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.md_blue_700));
        }
        ButterKnife.bind(this);
    }

    private void startMainActivity() {
        startActivity(new Intent(this, DailyScheduleActivity.class));
        finish();
    }

    @OnClick(R.id.invite_button)
    public void onInviteTapped(Button button) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "support@curiousily.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "iPoli invitation request");
        startActivity(Intent.createChooser(emailIntent, "Request your invite"));
    }
}
