package io.ipoli.android.player;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.support.annotation.NonNull;
import android.util.Log;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.BillingConstants;
import io.ipoli.android.app.App;
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

        Log.d("AAAAA", "service");
        List<String> skus = new ArrayList<>();
        skus.add("test_subscription");
        skus.add("test_subscription_yearly");

        Set<String> activeSubs = new HashSet<>();
        checkout.loadInventory(Inventory.Request.create().loadAllPurchases()
                .loadSkus(ProductTypes.SUBSCRIPTION, skus), products -> {
            Inventory.Product subscriptions = products.get(ProductTypes.SUBSCRIPTION);
            for (Purchase purchase : subscriptions.getPurchases()) {
                Log.d("AAA purchase", purchase.state + " " + purchase.autoRenewing);
                if (purchase.state == Purchase.State.PURCHASED) {
                    activeSubs.add(purchase.sku);
                    Date date = new Date();
                    date.setTime(purchase.time);
                    Log.d("AAA active", purchase.sku + " " + date.toString());
                }
            }

            if(activeSubs.isEmpty()) {
                lockAllUpgrades();
            }

            jobFinished(params, false);
        });
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