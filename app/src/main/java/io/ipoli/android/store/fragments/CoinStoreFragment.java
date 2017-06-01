package io.ipoli.android.store.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.BillingConstants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.events.BuyCoinsTappedEvent;
import io.ipoli.android.store.events.CoinsPurchasedEvent;
import io.ipoli.android.store.iab.IabHelper;
import io.ipoli.android.store.iab.Inventory;
import io.ipoli.android.store.iab.Purchase;
import io.ipoli.android.store.iab.SkuDetails;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class CoinStoreFragment extends BaseFragment {
    private static final String SKU_STARTER_PACK = "starter_pack";
    private static final String SKU_PREMIUM_PACK = "premium_pack";
    private static final String SKU_JUMBO_PACK = "jumbo_pack";

    private static final int RC_REQUEST = 10001;

    private Map<String, Integer> skuToValue = new HashMap<String, Integer>() {{
        put(SKU_STARTER_PACK, 300);
        put(SKU_PREMIUM_PACK, 2000);
        put(SKU_JUMBO_PACK, 4000);
    }};

    @Inject
    Bus eventBus;

    @BindView(R.id.root_layout)
    ViewGroup rootLayout;

    @BindView(R.id.loader)
    ProgressBar progressBar;

    @BindView(R.id.loader_container)
    ViewGroup loaderContainer;

    @BindView(R.id.failure_message)
    TextView failureMessage;

    @BindView(R.id.starter_price)
    TextView starterPrice;

    @BindView(R.id.starter_buy)
    Button starterBuy;

    @BindView(R.id.premium_price)
    TextView premiumPrice;

    @BindView(R.id.premium_buy)
    Button premiumBuy;

    @BindView(R.id.jumbo_price)
    TextView jumboPrice;

    @BindView(R.id.jumbo_buy)
    Button jumboBuy;

    @BindView(R.id.premium_root_layout)
    ViewGroup premiumContainer;

    @BindView(R.id.starter_root_layout)
    ViewGroup starterContainer;

    @BindView(R.id.jumbo_root_layout)
    ViewGroup jumboContainer;

    @BindView(R.id.premium_ribbon)
    ImageView premiumRibbon;

    @BindView(R.id.premium_most_popular)
    TextView premiumMostPopular;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    private Unbinder unbinder;
    private IabHelper iabHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_coin_store, container, false);
        App.getAppComponent(getContext()).inject(this);
        unbinder = ButterKnife.bind(this, view);
        ((StoreActivity) getActivity()).getSupportActionBar().setTitle(R.string.fragment_coin_store_title);

        loaderContainer.setVisibility(View.VISIBLE);

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

        eventBus.post(new ScreenShownEvent(EventSource.STORE_COINS));

        return view;
    }

    private void animatePacks() {
        Animation starterAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        starterContainer.startAnimation(starterAnimation);
        Animation premiumAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        premiumAnimation.setStartOffset(starterAnimation.getDuration() / 5);
        premiumContainer.startAnimation(premiumAnimation);
        Animation jumboAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        jumboAnimation.setStartOffset(premiumAnimation.getStartOffset() + premiumAnimation.getDuration() / 5);
        jumboAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                premiumRibbon.setVisibility(View.VISIBLE);
                premiumMostPopular.setVisibility(View.VISIBLE);
                animateMostPopular();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        jumboContainer.startAnimation(jumboAnimation);
    }

    private void showFailureMessage(int messageRes) {
        progressBar.setVisibility(View.GONE);
        failureMessage.setText(messageRes);
        failureMessage.setVisibility(View.VISIBLE);
    }

    private void queryInventory() {
        try {
            ArrayList<String> skuList = new ArrayList<>();
            skuList.addAll(skuToValue.keySet());
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
        SkuDetails starterPack = inventory.getSkuDetails(SKU_STARTER_PACK);
        SkuDetails premiumPack = inventory.getSkuDetails(SKU_PREMIUM_PACK);
        SkuDetails jumboPack = inventory.getSkuDetails(SKU_JUMBO_PACK);

        if (starterPack == null || premiumPack == null || jumboPack == null) {
            showFailureMessage(R.string.something_went_wrong);
            return;
        }

        starterPrice.setText(getString(R.string.coins_pack_value, skuToValue.get(SKU_STARTER_PACK)));
        starterBuy.setText(starterPack.getPrice());

        premiumPrice.setText(getString(R.string.coins_pack_value, skuToValue.get(SKU_PREMIUM_PACK)));
        premiumBuy.setText(premiumPack.getPrice());

        jumboPrice.setText(getString(R.string.coins_pack_value, skuToValue.get(SKU_JUMBO_PACK)));
        jumboBuy.setText(jumboPack.getPrice());

        hideLoaderContainer();

        animatePacks();
    }

    private void animateMostPopular() {
        premiumRibbon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_from_right_top));
        premiumMostPopular.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_from_right_top));
    }

    @OnClick(R.id.starter_buy)
    public void onBuyStarterClick(View v) {
        buyCoins(SKU_STARTER_PACK);
    }

    @OnClick(R.id.premium_buy)
    public void onBuyPremiumClick(View v) {
        buyCoins(SKU_PREMIUM_PACK);
    }

    @OnClick(R.id.jumbo_buy)
    public void onBuyJumboClick(View v) {
        buyCoins(SKU_JUMBO_PACK);
    }

    private void hideLoaderContainer() {
        loaderContainer.setVisibility(View.GONE);
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
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

    public void buyCoins(String sku) {
        eventBus.post(new BuyCoinsTappedEvent(sku));

        String payload = UUID.randomUUID().toString();

        try {
            iabHelper.launchPurchaseFlow(this, sku, RC_REQUEST,
                    (result, purchase) -> {
                        if (result.isFailure()) {
                            return;
                        }
                        if (!verifyDeveloperPayload(payload, purchase)) {
                            return;
                        }

                        if (result.isSuccess() && purchase.getSku().equals(sku)) {
                            eventBus.post(new CoinsPurchasedEvent(sku));
                            consumePurchase(purchase, skuToValue.get(sku));
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

