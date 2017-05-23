package io.ipoli.android.store.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.store.fragments.StoreFragment;
import io.ipoli.android.store.StoreItemType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class StoreActivity extends BaseActivity {

    public static final String START_ITEM_TYPE = "start_item_type";
    @Inject
    Bus eventBus;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        StoreFragment fragment;
        if(getIntent().hasExtra(START_ITEM_TYPE)) {
            StoreItemType storeItemType = StoreItemType.valueOf(getIntent().getStringExtra(START_ITEM_TYPE));
            fragment = StoreFragment.newInstance(storeItemType);
        } else {
            fragment = StoreFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, fragment).commit();

        eventBus.post(new ScreenShownEvent(EventSource.STORE));
    }

    public void setTitle(@StringRes int title) {
        toolbar.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
//                setTitle(R.string.title_store_activity);
        }
        return true;
    }


    @Override
    protected boolean useParentOptionsMenu() {
        return true;
    }
}
