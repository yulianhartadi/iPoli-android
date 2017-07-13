package io.ipoli.android.player;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.support.annotation.NonNull;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.BillingConstants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.data.MembershipType;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/3/17.
 */

public class SubscriptionsJobService extends JobService {
    public static final int JOB_ID = 2;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    Api api;

    private Billing billing;
    private Checkout checkout;

    @Override
    public void onCreate() {
        super.onCreate();
        App.getAppComponent(this).inject(this);
        billing = new Billing(getApplicationContext(), new Billing.DefaultConfiguration() {
            @Override
            @NonNull
            public String getPublicKey() {
                return BillingConstants.getAppPublicKey();
            }
        });
        checkout = Checkout.forService(this, billing);
        checkout.start();
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        Player player = playerPersistenceService.get();

        if (player.getMembership() != MembershipType.NONE) {
            List<String> skus = new ArrayList<>();
            skus.add("test_subscription");
            skus.add("test_subscription_yearly");

            checkout.loadInventory(Inventory.Request.create().loadAllPurchases()
                    .loadSkus(ProductTypes.SUBSCRIPTION, skus), products -> {
                Inventory.Product subscriptions = products.get(ProductTypes.SUBSCRIPTION);
                String subscriptionId = "";
                String token = "";
                for (Purchase purchase : subscriptions.getPurchases()) {
                    if (purchase.state == Purchase.State.PURCHASED) {
                        subscriptionId = purchase.sku;
                        token = purchase.token;
                    }
                }

                if (StringUtils.isNotEmpty(subscriptionId)) {
                    //send map <subscriptionId, token>, subscriptionId -> expirationlong, isInGrace
                    api.getMembershipStatus(subscriptionId, token, new Api.MembershipStatusResponseListener() {

                        @Override
                        public void onSuccess(Long expiration, Boolean isInGrace) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }

//                if (subscriptionIdToToken.isEmpty()) {
//                    lockAllUpgrades();
//                }

                jobFinished(params, false);
            });
        }
        return true;
    }

    private void lockAllUpgrades() {
        Player player = playerPersistenceService.get();
        player.setMembership(MembershipType.NONE);
        player.getInventory().lockAllUpgrades();
        playerPersistenceService.save(player);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        checkout.stop();
        return true;
    }
}