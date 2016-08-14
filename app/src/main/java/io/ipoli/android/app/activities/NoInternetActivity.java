package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import io.ipoli.android.R;
import io.ipoli.android.app.ui.NoInternetDialogFragment;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class NoInternetActivity extends AppCompatActivity implements NoInternetDialogFragment.OnConfirmListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_no_internet);

        NoInternetDialogFragment.newInstance(this).show(getSupportFragmentManager());
    }

    @Override
    public void onConfirm() {
        finish();
    }
}
