package io.ipoli.android.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;

import com.squareup.otto.Bus;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import javax.inject.Inject;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class BaseActivity extends RxAppCompatActivity {

    @Inject
    protected Bus eventBus;

    protected AppComponent appComponent() {
        return App.getAppComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}
