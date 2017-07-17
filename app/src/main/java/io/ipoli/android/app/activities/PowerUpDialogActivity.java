package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.view.Window;

import io.ipoli.android.R;
import io.ipoli.android.player.PowerUpDialog;
import io.ipoli.android.store.PowerUp;

public class PowerUpDialogActivity extends BaseActivity implements PowerUpDialog.OnDismissListener {

    public static final String POWER_UP = "power_up";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_power_up_dialog);

        PowerUp powerUp = PowerUp.valueOf(getIntent().getStringExtra(POWER_UP));
        PowerUpDialog.newInstance(powerUp, this).show(getSupportFragmentManager());
    }

    @Override
    public void onDismiss() {
        finish();
    }
}
