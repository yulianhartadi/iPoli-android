package io.ipoli.android.store.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.IntentStarter;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.Sku;
import org.solovyev.android.checkout.UiCheckout;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.BillingConstants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.events.BuyCoinsTappedEvent;
import io.ipoli.android.store.events.CoinsPurchasedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */
public class CoinStoreFragment extends BaseFragment {

    private static final String SKU_STARTER_PACK = "starter_pack";
    private static final String SKU_PREMIUM_PACK = "premium_pack";
    private static final String SKU_JUMBO_PACK = "jumbo_pack";
    private static final String SKU_DONATION_PACK = "donation_pack";
    private static final String SKU_SUBSCRIPTION = "test_subscription";

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

    @BindView(R.id.donation_price)
    TextView donationPrice;

    @BindView(R.id.donation_buy)
    Button donationBuy;

    @BindView(R.id.premium_root_layout)
    ViewGroup premiumContainer;

    @BindView(R.id.starter_root_layout)
    ViewGroup starterContainer;

    @BindView(R.id.jumbo_root_layout)
    ViewGroup jumboContainer;

    @BindView(R.id.donation_root_layout)
    ViewGroup donationContainer;

    @BindView(R.id.premium_ribbon)
    ImageView premiumRibbon;

    @BindView(R.id.premium_most_popular)
    TextView premiumMostPopular;

    private Unbinder unbinder;

    private Map<String, Integer> skuToValue;
    private Billing billing;
    private UiCheckout checkout;
    private Sku testSKU;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_coin_store, container, false);
        App.getAppComponent(getContext()).inject(this);
        unbinder = ButterKnife.bind(this, view);
        ((StoreActivity) getActivity()).populateTitle(R.string.fragment_coin_store_title);

        loaderContainer.setVisibility(View.VISIBLE);

        skuToValue = new HashMap<>();
        skuToValue.put(SKU_STARTER_PACK, 300);
        skuToValue.put(SKU_PREMIUM_PACK, 2000);
        skuToValue.put(SKU_JUMBO_PACK, 4000);
        skuToValue.put(SKU_DONATION_PACK, 1);


        if (!NetworkConnectivityUtils.isConnectedToInternet(getContext())) {
            showFailureMessage(R.string.no_internet_to_buy_coins);
        } else {
//            iabHelper = new IabHelper(getContext(), BillingConstants.getAppPublicKey());
//            iabHelper.startSetup(result -> {
//                if (!result.isSuccess() || iabHelper == null) {
//                    showFailureMessage(R.string.something_went_wrong);
//                    return;
//                }
//                queryInventory();
//            });
        }

        eventBus.post(new ScreenShownEvent(getActivity(), EventSource.STORE_COINS));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        billing = new Billing(getActivity(), new Billing.DefaultConfiguration() {
            @Override
            @NonNull
            public String getPublicKey() {
                return BillingConstants.getAppPublicKey();
            }
        });
        checkout = Checkout.forUi(new MyIntentStarter(this), this, billing);
    }

    private static class MyIntentStarter implements IntentStarter {
        @Nonnull
        private final Fragment fragment;

        public MyIntentStarter(@Nonnull Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void startForResult(@Nonnull IntentSender intentSender, int requestCode, @Nonnull Intent intent) throws IntentSender.SendIntentException {
            fragment.startIntentSenderForResult(intentSender, requestCode, intent, 0, 0, 0, null);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkout.start();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkout.loadInventory(Inventory.Request.create().loadAllPurchases().loadSkus(ProductTypes.SUBSCRIPTION, "test_subscription"), products -> {
            Inventory.Product subscriptions = products.get(ProductTypes.SUBSCRIPTION);
            initItems(subscriptions);
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        checkout.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        checkout.stop();
        super.onDestroy();
    }


    private void animatePacks() {
        Animation starterAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        starterContainer.startAnimation(starterAnimation);

        Animation premiumAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        premiumAnimation.setStartOffset(starterAnimation.getDuration() / 5);
        premiumContainer.startAnimation(premiumAnimation);

        Animation jumboAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        jumboAnimation.setStartOffset(premiumAnimation.getStartOffset() + premiumAnimation.getDuration() / 5);
        jumboContainer.startAnimation(jumboAnimation);

        Animation donationAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        donationAnimation.setStartOffset(jumboAnimation.getStartOffset() + jumboAnimation.getDuration() / 5);

        donationAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // intentional
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                premiumRibbon.setVisibility(View.VISIBLE);
                premiumMostPopular.setVisibility(View.VISIBLE);
                animateMostPopular();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // intentional
            }
        });
        donationContainer.startAnimation(donationAnimation);
    }

    private void showFailureMessage(int messageRes) {
        progressBar.setVisibility(View.GONE);
        failureMessage.setText(messageRes);
        failureMessage.setVisibility(View.VISIBLE);
    }

    private void queryInventory() {
//        try {
//            ArrayList<String> skuList = new ArrayList<>();
//            skuList.addAll(skuToValue.keySet());
//            iabHelper.queryInventoryAsync(true, skuList, (result, inv) -> {
//                if (!result.isSuccess() || iabHelper == null) {
//                    showFailureMessage(R.string.something_went_wrong);
//                    return;
//                }
//                initItems(inv);
//            });
//        } catch (IabHelper.IabAsyncInProgressException e) {
//            eventBus.post(new AppErrorEvent(e));
//        }
    }

    private void initItems(Inventory.Product subscriptions) {
        testSKU = subscriptions.getSku(SKU_SUBSCRIPTION);
        Sku starterPack = subscriptions.getSku(SKU_SUBSCRIPTION);
        Sku premiumPack = subscriptions.getSku(SKU_SUBSCRIPTION);
        Sku jumboPack = subscriptions.getSku(SKU_SUBSCRIPTION);
        Sku donationPack = subscriptions.getSku(SKU_SUBSCRIPTION);

        if (starterPack == null || premiumPack == null || jumboPack == null || donationPack == null) {
            showFailureMessage(R.string.something_went_wrong);
            return;
        }

        starterPrice.setText(getString(R.string.coins_pack_value, skuToValue.get(SKU_STARTER_PACK)));
        starterBuy.setText(starterPack.price);

        premiumPrice.setText(getString(R.string.coins_pack_value, skuToValue.get(SKU_PREMIUM_PACK)));
        premiumBuy.setText(premiumPack.price);

        jumboPrice.setText(getString(R.string.coins_pack_value, skuToValue.get(SKU_JUMBO_PACK)));
        jumboBuy.setText(jumboPack.price);

        donationPrice.setText(getString(R.string.coins_pack_single_value, skuToValue.get(SKU_DONATION_PACK)));
        donationBuy.setText(donationPack.price);

        hideLoaderContainer();

        animatePacks();
    }

    private void animateMostPopular() {
        premiumRibbon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_from_right_top));
        premiumMostPopular.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_from_right_top));
    }

    @OnClick(R.id.starter_buy)
    public void onBuyStarterClick(View v) {
        buyCoins(SKU_SUBSCRIPTION);
    }

    @OnClick(R.id.premium_buy)
    public void onBuyPremiumClick(View v) {
        buyCoins(SKU_SUBSCRIPTION);
    }

    @OnClick(R.id.jumbo_buy)
    public void onBuyJumboClick(View v) {
        buyCoins(SKU_SUBSCRIPTION);
    }

    @OnClick(R.id.donation_buy)
    public void onBuyDonationClick(View v) {
        buyCoins(SKU_SUBSCRIPTION);
    }

    private void hideLoaderContainer() {
        loaderContainer.setVisibility(View.GONE);
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    public void buyCoins(String sku) {
        eventBus.post(new BuyCoinsTappedEvent(sku));
        String payload = UUID.randomUUID().toString();
        checkout.startPurchaseFlow(ProductTypes.SUBSCRIPTION, sku, payload, new EmptyRequestListener<Purchase>() {
            @Override
            public void onSuccess(@Nonnull Purchase purchase) {
                eventBus.post(new CoinsPurchasedEvent(sku));
                Log.d("Subscription", "Purchased");
            }

            @Override
            public void onError(int i, @Nonnull Exception e) {

            }
        });

//        ((StoreActivity) getActivity()).buyCoins(iabHelper, sku, skuToValue.get(sku));
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
//        if (iabHelper != null) {
//            iabHelper.disposeWhenFinished();
//            iabHelper = null;
//        }
        super.onDestroyView();
    }
}

