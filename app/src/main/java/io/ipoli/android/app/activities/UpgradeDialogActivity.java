package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.view.Window;

import io.ipoli.android.R;
import io.ipoli.android.player.UpgradeDialog;
import io.ipoli.android.store.Upgrade;

public class UpgradeDialogActivity extends BaseActivity implements UpgradeDialog.OnDismissListener {

    public static final String UPGRADE = "upgrade";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_upgrade_dialog);

        Upgrade upgrade = Upgrade.valueOf(getIntent().getStringExtra(UPGRADE));
        UpgradeDialog.newInstance(upgrade, this).show(getSupportFragmentManager());
    }

    @Override
    public void onDismiss() {
        finish();
    }
}
