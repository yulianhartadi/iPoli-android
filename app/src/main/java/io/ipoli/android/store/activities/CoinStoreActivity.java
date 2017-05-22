package io.ipoli.android.store.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.BillingConstants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.adapters.CoinsStoreAdapter;
import io.ipoli.android.store.events.BuyCoinsTappedEvent;
import io.ipoli.android.store.events.CoinsPurchasedEvent;
import io.ipoli.android.store.iab.IabHelper;
import io.ipoli.android.store.iab.Inventory;
import io.ipoli.android.store.iab.Purchase;
import io.ipoli.android.store.iab.SkuDetails;
import io.ipoli.android.store.viewmodels.CoinsViewModel;

public class CoinStoreActivity extends BaseActivity {
    private static final String SKU_COINS_100 = "coins_100";
    private static final String SKU_COINS_300 = "coins_300";
    private static final String SKU_COINS_500 = "coins_500";
    private static final String SKU_COINS_1000 = "coins_1000";
    private static final int RC_REQUEST = 10001;

    private Map<String, Integer> skuToValue = new HashMap<String, Integer>() {{
        put(SKU_COINS_100, 100);
        put(SKU_COINS_300, 300);
        put(SKU_COINS_500, 500);
        put(SKU_COINS_1000, 1000);
    }};

    @Inject
    Bus eventBus;

    @BindView(R.id.root_layout)
    ViewGroup rootLayout;

    @BindView(R.id.coins_list)
    EmptyStateRecyclerView coinsList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.loader)
    ProgressBar progressBar;

    @BindView(R.id.loader_container)
    ViewGroup loaderContainer;

    @BindView(R.id.failure_message)
    TextView failureMessage;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    private IabHelper iabHelper;
    private CoinsStoreAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_store);

        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        coinsList.setLayoutManager(layoutManager);
        coinsList.setEmptyView(rootLayout, R.string.empty_store_items, R.drawable.ic_coins_grey_24dp);
        adapter = new CoinsStoreAdapter(new ArrayList<>(), eventBus);
        coinsList.setAdapter(adapter);

        if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
            showFailureMessage(R.string.no_internet_to_buy_coins);
        } else {
            iabHelper = new IabHelper(this, BillingConstants.getAppPublicKey());
            iabHelper.startSetup(result -> {
                if (!result.isSuccess() || iabHelper == null) {
                    showFailureMessage(R.string.something_went_wrong);
                    return;
                }
                queryInventory();
            });
        }

        eventBus.post(new ScreenShownEvent(EventSource.STORE));
    }

    private void showFailureMessage(int messageRes) {
        progressBar.setVisibility(View.GONE);
        failureMessage.setText(messageRes);
        failureMessage.setVisibility(View.VISIBLE);
    }

    private void queryInventory() {
        try {
            ArrayList<String> skuList = new ArrayList<>();
            skuList.add(SKU_COINS_100);
            skuList.add(SKU_COINS_300);
            skuList.add(SKU_COINS_500);
            skuList.add(SKU_COINS_1000);
            iabHelper.queryInventoryAsync(true, skuList, (result, inv) -> {
                if (!result.isSuccess() || iabHelper == null) {
                    showFailureMessage(R.string.something_went_wrong);
                    return;
                }
                initItems(inv);
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            eventBus.post(new AppErrorEvent(e));
        }
    }

    private void initItems(Inventory inventory) {
        SkuDetails coins100 = inventory.getSkuDetails(SKU_COINS_100);
        SkuDetails coins300 = inventory.getSkuDetails(SKU_COINS_300);
        SkuDetails coins500 = inventory.getSkuDetails(SKU_COINS_500);
        SkuDetails coins1000 = inventory.getSkuDetails(SKU_COINS_1000);

        List<CoinsViewModel> viewModels = new ArrayList<>();
        viewModels.add(new CoinsViewModel(SKU_COINS_100, coins100.getTitle(), coins100.getPrice(), 100));
        viewModels.add(new CoinsViewModel(SKU_COINS_300, coins300.getTitle(), coins300.getPrice(), 300));
        viewModels.add(new CoinsViewModel(SKU_COINS_500, coins500.getTitle(), coins500.getPrice(), 500));
        viewModels.add(new CoinsViewModel(SKU_COINS_1000, coins1000.getTitle(), coins1000.getPrice(), 1000));
        adapter.setViewModels(viewModels);

        hideLoaderContainer();
    }

    private void hideLoaderContainer() {
        loaderContainer.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                showHelpDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_store, R.string.help_dialog_store_title, "store").show(getSupportFragmentManager());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iabHelper != null) {
            iabHelper.disposeWhenFinished();
            iabHelper = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onBuyCoinsTapped(BuyCoinsTappedEvent e) {
        String payload = UUID.randomUUID().toString();

        try {
            iabHelper.launchPurchaseFlow(this, e.sku, RC_REQUEST,
                    (result, purchase) -> {
                        if (result.isFailure()) {
                            return;
                        }
                        if (!verifyDeveloperPayload(payload, purchase)) {
                            return;
                        }

                        if (result.isSuccess() && purchase.getSku().equals(e.sku)) {
                            eventBus.post(new CoinsPurchasedEvent(e.sku));
                            consumePurchase(purchase, skuToValue.get(e.sku));
                        }
                    }, payload);
        } catch (IabHelper.IabAsyncInProgressException ex) {
            eventBus.post(new AppErrorEvent(ex));
        }
    }

    private void consumePurchase(Purchase purchase, int value) {
        try {
            iabHelper.consumeAsync(purchase, (p, result) -> {
                if (result.isSuccess()) {
                    updateCoins(value);
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            eventBus.post(new AppErrorEvent(e));
        }
    }

    private void updateCoins(int coins) {
        Player player = playerPersistenceService.get();
        player.addCoins(coins);
        playerPersistenceService.save(player);
        Snackbar.make(rootLayout, String.format(getString(R.string.coins_bought), coins), Snackbar.LENGTH_SHORT).show();
    }

    boolean verifyDeveloperPayload(String payload, Purchase p) {
        return p.getDeveloperPayload().equals(payload);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (iabHelper == null) return;
        // Pass on the activity result to the helper for handling
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
