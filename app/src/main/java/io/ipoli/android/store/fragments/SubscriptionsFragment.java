package io.ipoli.android.store.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.IntentStarter;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.PurchaseFlow;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.Sku;
import org.solovyev.android.checkout.UiCheckout;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.BillingConstants;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.player.data.MembershipType;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.Upgrade;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.events.BuyCoinsTappedEvent;
import io.ipoli.android.store.events.CoinsPurchasedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */
public class SubscriptionsFragment extends BaseFragment {

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.root_layout)
    ViewGroup rootLayout;

    @BindView(R.id.loader)
    ProgressBar progressBar;

    @BindView(R.id.loader_container)
    ViewGroup loaderContainer;

    @BindView(R.id.failure_message)
    TextView failureMessage;

    @BindView(R.id.monthly_price)
    TextView monthlyBuy;

    @BindView(R.id.yearly_buy)
    Button yearlyBuy;

    @BindView(R.id.quarterly_buy)
    Button quarterlyBuy;

    @BindView(R.id.yearly_root_layout)
    ViewGroup yearlyContainer;

    @BindView(R.id.monthly_root_layout)
    ViewGroup monthlyPlanContainer;

    @BindView(R.id.quarterly_root_layout)
    ViewGroup quarterlyContainer;

    @BindView(R.id.yearly_ribbon)
    ImageView yearlyRibbon;

    @BindView(R.id.yearly_most_popular)
    TextView yearlyMostPopular;

    private Unbinder unbinder;

    private Billing billing;
    private UiCheckout checkout;

    private Set<String> activeSkus;
    private List<String> skus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        App.getAppComponent(getContext()).inject(this);
        unbinder = ButterKnife.bind(this, view);
        ((StoreActivity) getActivity()).populateTitle(R.string.fragment_coin_store_title);

        loaderContainer.setVisibility(View.VISIBLE);

        if (!NetworkConnectivityUtils.isConnectedToInternet(getContext())) {
            showFailureMessage(R.string.no_internet_to_buy_coins);
        }

        skus = new ArrayList<>();
        skus.add(Constants.SKU_SUBSCRIPTION_MONTHLY);
        skus.add(Constants.SKU_SUBSCRIPTION_QUARTERLY);
        skus.add(Constants.SKU_SUBSCRIPTION_YEARLY);

        postEvent(new ScreenShownEvent(getActivity(), EventSource.STORE_COINS));
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
        queryInventory();
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
        Animation monthlyAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        monthlyPlanContainer.startAnimation(monthlyAnimation);

        Animation yearlyAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        yearlyAnimation.setStartOffset(monthlyAnimation.getDuration() / 5);
        yearlyContainer.startAnimation(yearlyAnimation);

        Animation quarterlyAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        quarterlyAnimation.setStartOffset(yearlyAnimation.getStartOffset() + yearlyAnimation.getDuration() / 5);
        quarterlyAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // intentional
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                yearlyRibbon.setVisibility(View.VISIBLE);
                yearlyMostPopular.setVisibility(View.VISIBLE);
                animateMostPopular();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // intentional
            }
        });
        quarterlyContainer.startAnimation(quarterlyAnimation);
    }

    private void showFailureMessage(int messageRes) {
        progressBar.setVisibility(View.GONE);
        failureMessage.setText(messageRes);
        failureMessage.setVisibility(View.VISIBLE);
    }

    private void queryInventory() {
        checkout.loadInventory(Inventory.Request.create().loadAllPurchases()
                .loadSkus(ProductTypes.SUBSCRIPTION, skus), products -> {
            Inventory.Product subscriptions = products.get(ProductTypes.SUBSCRIPTION);
            activeSkus = new HashSet<>();
            for (Purchase purchase : subscriptions.getPurchases()) {
                if (purchase.state == Purchase.State.PURCHASED && purchase.autoRenewing) {
                    activeSkus.add(purchase.sku);
                }
            }
            initItems(subscriptions);
        });
    }

    private void initItems(Inventory.Product subscriptions) {
        Sku monthlySubscription = subscriptions.getSku(Constants.SKU_SUBSCRIPTION_MONTHLY);
        Sku quarterlySubscription = subscriptions.getSku(Constants.SKU_SUBSCRIPTION_QUARTERLY);
        Sku yearlySubscription = subscriptions.getSku(Constants.SKU_SUBSCRIPTION_YEARLY);

        if (monthlySubscription == null || quarterlySubscription == null || yearlySubscription == null) {
            showFailureMessage(R.string.something_went_wrong);
            return;
        }

        Sku.Price monthlyPrice = monthlySubscription.detailedPrice;
        monthlyBuy.setText(calculatePricePerMonth(monthlyPrice.amount, 1) + monthlyPrice.currency + " per month");

        yearlyBuy.setText(quarterlySubscription.price);

        quarterlyBuy.setText(yearlySubscription.price);

        hideLoaderContainer();

        animatePacks();
    }

    private double calculatePricePerMonth(long price, int months) {
        double pricePerMonth = price / (double) months;
        int x = (int) (pricePerMonth / 10000);
        return x / 100.0;
    }

    private void animateMostPopular() {
        yearlyRibbon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_from_right_top));
        yearlyMostPopular.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_from_right_top));
    }

    @OnClick(R.id.monthly_buy)
    public void onSubscribeMonthlyClick(View v) {
        subscribe(Constants.SKU_SUBSCRIPTION_MONTHLY);
    }

    @OnClick(R.id.yearly_buy)
    public void onSubscribeYearlyClick(View v) {
        subscribe(Constants.SKU_SUBSCRIPTION_YEARLY);
    }

    @OnClick(R.id.quarterly_buy)
    public void onSubscribeQuarterlyClick(View v) {
        subscribe(Constants.SKU_SUBSCRIPTION_QUARTERLY);
    }

    private void hideLoaderContainer() {
        loaderContainer.setVisibility(View.GONE);
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    public void subscribe(String sku) {
        postEvent(new BuyCoinsTappedEvent(sku));
        if (activeSkus.isEmpty()) {
            doSubscribe(sku);
        } else {
            changeSubscription(sku);
        }
    }

    private void changeSubscription(String sku) {
        checkout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(@Nonnull BillingRequests requests) {
                final PurchaseFlow flow = checkout.createOneShotPurchaseFlow(new RequestListener<Purchase>() {
                    @Override
                    public void onSuccess(@Nonnull Purchase purchase) {
                        updatePlayer(getPlayer(), sku, purchase.time);
                        queryInventory();
                    }

                    @Override
                    public void onError(int i, @Nonnull Exception e) {
                        //log error
                    }
                });
                requests.changeSubscription(new ArrayList<>(activeSkus), sku, null, flow);
            }
        });
    }

    private void doSubscribe(final String sku) {
        String payload = UUID.randomUUID().toString();
        checkout.startPurchaseFlow(ProductTypes.SUBSCRIPTION, sku, payload, new EmptyRequestListener<Purchase>() {
            @Override
            public void onSuccess(@Nonnull Purchase purchase) {
                postEvent(new CoinsPurchasedEvent(sku));

                Player player = getPlayer();
                Map<Integer, Long> activeUpgrades = getActiveUpgrades(player);
                int coinsToReturn = findCoinsToReturn(activeUpgrades);
                player.addCoins(coinsToReturn);

                updatePlayer(player, sku, purchase.time);
                queryInventory();
            }

            @Override
            public void onError(int i, @Nonnull Exception e) {

            }
        });
    }

    private int findCoinsToReturn(Map<Integer, Long> activeUpgrades) {
        int coinsToReturn = 0;
        LocalDate today = LocalDate.now();
        for (Map.Entry<Integer, Long> entry : activeUpgrades.entrySet()) {
            LocalDate expiration = DateUtils.fromMillis(entry.getValue());
            int days = (int) ChronoUnit.DAYS.between(today, expiration) + 1;
            coinsToReturn += (Upgrade.get(entry.getKey()).price / 30f) * days;
        }
        return coinsToReturn;
    }


    private Map<Integer, Long> getActiveUpgrades(Player player) {
        Map<Integer, Long> upgrades = player.getInventory().getUpgrades();
        if (upgrades.isEmpty()) {
            return new HashMap<>();
        }

        Map<Integer, Long> activeUpgrades = new HashMap<>();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        for (Map.Entry<Integer, Long> entry : upgrades.entrySet()) {
            LocalDate expirationDate = DateUtils.fromMillis(entry.getValue());
            if (expirationDate.isAfter(yesterday)) {
                activeUpgrades.put(entry.getKey(), entry.getValue());
            }
        }

        return activeUpgrades;
    }

    private void updatePlayer(Player player, String sku, Long purchasedTime) {
        MembershipType membershipType;
        LocalDate purchasedDate = DateUtils.fromMillis(purchasedTime);
        LocalDate expirationDate;
        switch (sku) {
            case Constants.SKU_SUBSCRIPTION_MONTHLY:
                membershipType = MembershipType.MONTHLY;
                expirationDate = purchasedDate.plusMonths(1).minusDays(1);
                break;
            case Constants.SKU_SUBSCRIPTION_QUARTERLY:
                membershipType = MembershipType.THREE_MONTHS;
                expirationDate = purchasedDate.plusMonths(3).minusDays(1);
                break;
            case Constants.SKU_SUBSCRIPTION_YEARLY:
                membershipType = MembershipType.YEARLY;
                expirationDate = purchasedDate.plusYears(1).minusDays(1);
                break;
            default:
                membershipType = MembershipType.NONE;
                expirationDate = LocalDate.now();
        }

        player.getInventory().unlockAllUpgrades(expirationDate);
        player.setMembership(membershipType);
        playerPersistenceService.save(player);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}

