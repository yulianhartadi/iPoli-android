package io.ipoli.android;

import android.support.v7.app.AppCompatActivity;

import io.ipoli.android.app.App;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class BaseActivity extends AppCompatActivity {
    protected AppComponent appComponent() {
        return ((App) getApplication()).getAppComponent();
    }
}
