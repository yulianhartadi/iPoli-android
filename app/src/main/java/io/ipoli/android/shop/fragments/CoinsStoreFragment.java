package io.ipoli.android.shop.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
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
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.shop.adapters.CoinsStoreAdapter;
import io.ipoli.android.shop.events.BuyCoinsEvent;
import io.ipoli.android.shop.iab.IabHelper;
import io.ipoli.android.shop.iab.IabResult;
import io.ipoli.android.shop.iab.Inventory;
import io.ipoli.android.shop.iab.Purchase;
import io.ipoli.android.shop.iab.SkuDetails;
import io.ipoli.android.shop.viewmodels.ProductViewModels;

public class CoinsStoreFragment extends BaseFragment {
    private static final String SKU_COINS_10 = "test";
    private static final String SKU_COINS_100 = "coins_100";
    private static final int RC_REQUEST = 10001;

    @Inject
    Bus eventBus;

    @BindView(R.id.root_layout)
    ViewGroup rootLayout;

    @BindView(R.id.coins_list)
    EmptyStateRecyclerView coinsList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

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

        iabHelper = new IabHelper(getContext(), BillingConstants.getAppPublicKey());


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        coinsList.setLayoutManager(layoutManager);
        coinsList.setEmptyView(rootLayout, R.string.empty_inbox_text, R.drawable.ic_inbox_grey_24dp);
        adapter = new CoinsStoreAdapter(new ArrayList<>(), eventBus);
        coinsList.setAdapter(adapter);

        iabHelper.startSetup(result -> {

            if (!result.isSuccess()) {
                return;
            }

            if (iabHelper == null) return;

            Log.d("AAAA", "setup completed");

            try {
                ArrayList<String> skuList = new ArrayList<>();
                skuList.add(SKU_COINS_10);
                skuList.add(SKU_COINS_100);
                iabHelper.queryInventoryAsync(true, skuList, new IabHelper.QueryInventoryFinishedListener() {
                    @Override
                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                        if (iabHelper == null) return;

                        if (result.isFailure()) {
                            Log.d("AAA", "inventory failure " + result.getMessage());
                            return;
                        }
                        Log.d("AAA", "inventory");
//                        consumePurchase(inv.getPurchase(SKU_COINS_10));
                        initItems(inv);
                    }
                });
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        });

        return view;
    }

    private void initItems(Inventory inventory) {
        SkuDetails coins10 = inventory.getSkuDetails(SKU_COINS_10);
        SkuDetails coins100 = inventory.getSkuDetails(SKU_COINS_100);

        List<ProductViewModels> viewModels = new ArrayList<>();
        viewModels.add(new ProductViewModels(SKU_COINS_10, coins10.getTitle(), coins10.getPrice()));
        viewModels.add(new ProductViewModels(SKU_COINS_100, coins100.getTitle(), coins100.getPrice()));
        Log.d("AAA", "initItems");
        adapter.setViewModels(viewModels);
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_inbox, R.string.help_dialog_inbox_title, "inbox").show(getActivity().getSupportFragmentManager());
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
                            consumePurchase(purchase);
                        }
                    }, payload);
        } catch (IabHelper.IabAsyncInProgressException ex) {
            ex.printStackTrace();
        }
    }

    private void consumePurchase(Purchase purchase) {
        try {
            iabHelper.consumeAsync(purchase, (purchase1, result1) -> {
                if (result1.isSuccess()) {
                    Log.d("AAAA", "+ 100 coins");
                    updateCoins();
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
        }
    }

    private void updateCoins() {
//        int coins = Integer.parseInt(String.valueOf(coinsText.getText()));
//        coins += 100;
//        coinsText.setText(coins + "");
    }

    boolean verifyDeveloperPayload(String payload, Purchase p) {
        return p.getDeveloperPayload().equals(payload);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AAA", "activity result " + requestCode + " " + resultCode);
        if (iabHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
        }
    }
}
