package io.ipoli.android.app;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class BaseActivity extends AppCompatActivity {
    protected AppComponent appComponent() {
        return App.getAppComponent(this);
    }
}
