package io.ipoli.android.store.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.StoreItemType;
import io.ipoli.android.store.fragments.StoreFragment;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class StoreActivity extends BaseActivity implements OnDataChangedListener<Player> {

    public static final String START_ITEM_TYPE = "start_item_type";
    @Inject
    Bus eventBus;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.player_coins)
    TextView coins;

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
            ab.setDisplayShowTitleEnabled(false);
        }
        populateTitle(R.string.fragment_store_title);

        StoreFragment fragment;
        if (getIntent().hasExtra(START_ITEM_TYPE)) {
            StoreItemType storeItemType = StoreItemType.valueOf(getIntent().getStringExtra(START_ITEM_TYPE));
            fragment = StoreFragment.newInstance(storeItemType);
        } else {
            fragment = StoreFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, fragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        playerPersistenceService.listen(this);
    }

    @Override
    protected void onStop() {
        playerPersistenceService.removeAllListeners();
        super.onStop();
    }

    public void populateTitle(@StringRes int title) {
        toolbarTitle.setText(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        populateTitle(R.string.fragment_store_title);
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    @Override
    public void onDataChanged(Player player) {
        coins.setText(String.valueOf(player.getCoins()));
    }
}
