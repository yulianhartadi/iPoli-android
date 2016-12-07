package io.ipoli.android.shop.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
import butterknife.Unbinder;
import io.ipoli.android.BillingConstants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.shop.adapters.CoinsStoreAdapter;
import io.ipoli.android.shop.events.BuyCoinsEvent;
import io.ipoli.android.shop.iab.IabHelper;
import io.ipoli.android.shop.iab.Inventory;
import io.ipoli.android.shop.iab.Purchase;
import io.ipoli.android.shop.iab.SkuDetails;
import io.ipoli.android.shop.viewmodels.ProductViewModels;

public class CoinsStoreFragment extends BaseFragment {
    private static final String SKU_COINS_10 = "test";
    private static final String SKU_COINS_100 = "coins_100";
    private static final int RC_REQUEST = 10001;

    private Map<String, Integer> skuToValue = new HashMap<String, Integer>(){{
        put(SKU_COINS_10, 10);
        put(SKU_COINS_100, 100);

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
    AvatarPersistenceService avatarPersistenceService;

    private Unbinder unbinder;
    private IabHelper iabHelper;
    private CoinsStoreAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_coins_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.title_fragment_store);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        coinsList.setLayoutManager(layoutManager);
        coinsList.setEmptyView(rootLayout, R.string.empty_inbox_text, R.drawable.ic_inbox_grey_24dp);
        adapter = new CoinsStoreAdapter(new ArrayList<>(), eventBus);
        coinsList.setAdapter(adapter);

        if (!NetworkConnectivityUtils.isConnectedToInternet(getContext())) {
            showFailureMessage(R.string.no_internet_to_buy_coins);
        } else {
            iabHelper = new IabHelper(getContext(), BillingConstants.getAppPublicKey());
            iabHelper.startSetup(result -> {
                if (!result.isSuccess() || iabHelper == null) {
                    showFailureMessage(R.string.something_went_wrong);
                    return;
                }
                queryInventory();
            });
        }

        return view;
    }

    private void showFailureMessage(int messageRes) {
        progressBar.setVisibility(View.GONE);
        failureMessage.setText(messageRes);
        failureMessage.setVisibility(View.VISIBLE);
    }

    private void queryInventory() {
        try {
            ArrayList<String> skuList = new ArrayList<>();
            skuList.add(SKU_COINS_10);
            skuList.add(SKU_COINS_100);
            iabHelper.queryInventoryAsync(true, skuList, (result, inv) -> {
                if (!result.isSuccess() || iabHelper == null) {
                    showFailureMessage(R.string.something_went_wrong);
                    return;
                }
                initItems(inv);
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private void initItems(Inventory inventory) {
        SkuDetails coins10 = inventory.getSkuDetails(SKU_COINS_10);
        SkuDetails coins100 = inventory.getSkuDetails(SKU_COINS_100);

        List<ProductViewModels> viewModels = new ArrayList<>();
        viewModels.add(new ProductViewModels(SKU_COINS_10, coins10.getTitle(), coins10.getPrice(), 10));
        viewModels.add(new ProductViewModels(SKU_COINS_100, coins100.getTitle(), coins100.getPrice(), 100));
        adapter.setViewModels(viewModels);

        hideLoaderContainer();
    }

    private void hideLoaderContainer() {
        loaderContainer.setVisibility(View.GONE);
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_store, R.string.help_dialog_store_title, "store").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        if (iabHelper != null) {
            iabHelper.disposeWhenFinished();
            iabHelper = null;
        }
        super.onDestroyView();
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
    public void onBuyCoins(BuyCoinsEvent e) {
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
                            consumePurchase(purchase, skuToValue.get(e.sku));
                        }
                    }, payload);
        } catch (IabHelper.IabAsyncInProgressException ex) {
            ex.printStackTrace();
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
        }
    }

    private void updateCoins(int coins) {
        avatarPersistenceService.find(avatar -> {
            avatar.addCoins(coins);
            avatarPersistenceService.save(avatar);
            Snackbar.make(rootLayout, String.format(getString(R.string.coins_bought), coins), Snackbar.LENGTH_SHORT).show();
        });
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
