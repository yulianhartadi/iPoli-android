package com.curiousily.ipoli;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class InviteOnlyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_only);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.md_blue_700));
        }

        ButterKnife.bind(this);
    }

    @OnClick(R.id.invite_button)
    public void onInviteTapped(Button button) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "support@curiousily.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "iPoli invitation request");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
