package io.ipoli.android.store.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.StoreItemType;
import io.ipoli.android.store.fragments.StoreFragment;
import io.ipoli.android.store.iab.Purchase;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */
public class StoreActivity extends BaseActivity implements OnDataChangedListener<Player> {

    private static final int RC_BUY_COINS = 10001;

    public static final String START_ITEM_TYPE = "start_item_type";

    @Inject
    Bus eventBus;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.root_layout)
    ViewGroup rootLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.player_coins)
    TextView coins;

//    private IabHelper iabHelper;

//    private Billing billing;
//
//    private ActivityCheckout checkout;

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

//        billing = new Billing(this, new Billing.DefaultConfiguration() {
//            @Override
//            @NonNull
//            public String getPublicKey() {
//                return BillingConstants.getAppPublicKey();
//            }
//        });
//
//        checkout = Checkout.forActivity(this, billing);
    }

    @Override
    protected void onStart() {
        super.onStart();
        playerPersistenceService.listen(this);
//        checkout.start();
//        Inventory inventory = checkout.makeInventory();
//        inventory.load(Inventory.Request.create().loadAllPurchases().loadSkus(ProductTypes.SUBSCRIPTION, "test_subscription"), products -> {
//            Log.d("Products", products.size() + "");
//        });
    }

    @Override
    protected void onStop() {
        playerPersistenceService.removeAllListeners();
//        checkout.stop();
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

//    public void buyCoins(IabHelper iabHelper, String sku, int coins) {
//        this.iabHelper = iabHelper;
//        String payload = UUID.randomUUID().toString();
//
//        try {
//            iabHelper.launchPurchaseFlow(this, sku, RC_BUY_COINS,
//                    (result, purchase) -> {
//                        if (result.isFailure()) {
//                            return;
//                        }
//                        if (!verifyDeveloperPayload(payload, purchase)) {
//                            return;
//                        }
//
//                        if (result.isSuccess() && purchase.getSku().equals(sku)) {
//                            eventBus.post(new CoinsPurchasedEvent(sku));
//                            consumePurchase(purchase, coins);
//                        }
//                    }, payload);
//        } catch (IabHelper.IabAsyncInProgressException ex) {
//            eventBus.post(new AppErrorEvent(ex));
//        }
//    }
//
//    private void consumePurchase(Purchase purchase, int coins) {
//        try {
//            iabHelper.consumeAsync(purchase, (p, result) -> {
//                if (result.isSuccess()) {
//                    updateCoins(coins);
//                }
//            });
//        } catch (IabHelper.IabAsyncInProgressException e) {
//            eventBus.post(new AppErrorEvent(e));
//        }
//    }

    private void updateCoins(int coins) {
        Player player = playerPersistenceService.get();
        player.addCoins(coins);
        playerPersistenceService.save(player);
        Snackbar.make(rootLayout, getString(R.string.coins_bought, coins), Snackbar.LENGTH_SHORT).show();
    }

    boolean verifyDeveloperPayload(String payload, Purchase p) {
        return p.getDeveloperPayload().equals(payload);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode != RC_BUY_COINS) {
//            super.onActivityResult(requestCode, resultCode, data);
//            return;
//        }
//        if (iabHelper == null) {
//            return;
//        }
//        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
}
