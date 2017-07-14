package io.ipoli.android.player;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.support.annotation.NonNull;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.ipoli.android.BillingConstants;
import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.data.MembershipType;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/3/17.
 */

public class UpgradesJobService extends JobService {
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

        if (player.hasNoUpgrades()) {
            jobFinished(params, false);
            return false;
        }


        if (player.getMembership() == MembershipType.NONE) {
            UpgradeStatusValidator validator = new UpgradeStatusValidator(player, false);
            ValidationStatus status = validator.validate();
            if (!status.expiring.isEmpty()) {
                //notification
            }
            jobFinished(params, false);
            return false;
        }

        validateSubscription(params, player);
        return true;
    }

    private void validateSubscription(JobParameters params, final Player player) {
        List<String> skus = new ArrayList<>();
        skus.add("test_subscription");
        skus.add("test_subscription_yearly");

        checkout.loadInventory(Inventory.Request.create().loadAllPurchases()
                .loadSkus(ProductTypes.SUBSCRIPTION, skus), products -> {
            Inventory.Product subscriptions = products.get(ProductTypes.SUBSCRIPTION);
            Purchase purchase = getActivePurchase(subscriptions.getPurchases());

            if (purchase == null) {
                //has no active subscriptions
                player.setMembership(MembershipType.NONE);
                playerPersistenceService.save(player);
                jobFinished(params, false);
                return;
            }

            api.getMembershipStatus(purchase.sku, purchase.token, new Api.MembershipStatusResponseListener() {
                @Override
                public void onSuccess(Long startTimeMillis, Long expiryTimeMillis, Boolean autoRenewing) {
                    LocalDate membershipExpirationDate = DateUtils.fromMillis(expiryTimeMillis);
                    if (isInGrace(player, membershipExpirationDate, autoRenewing)) {
                        addGracePeriodToUpgradesExpiration(player);
                    }

                    UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now(), membershipExpirationDate, autoRenewing);
                    ValidationStatus status = validator.validate();
                    if (!status.expired.isEmpty()) {
                        player.setMembership(MembershipType.NONE);
                    } else if (!status.expiring.isEmpty()) {
                        //notification
                    } else if (!status.toBeRenewed.isEmpty()) {
                        player.getInventory().unlockAllUpgrades(membershipExpirationDate);
                    }
                    playerPersistenceService.save(player);

                }

                @Override
                public void onError(Exception e) {
                    //log error
                }
            });

            jobFinished(params, false);
        });
    }

    private void addGracePeriodToUpgradesExpiration(Player player) {
        Map<Integer, Long> upgrades = player.getInventory().getUpgrades();
        LocalDate currentExpirationDate = DateUtils.fromMillis((upgrades.values().iterator().next()));
        LocalDate gracePeriodEnd = currentExpirationDate.plusDays(Constants.UPGRADE_GRACE_PERIOD_DAYS - 1);
        player.getInventory().unlockAllUpgrades(gracePeriodEnd);
    }

    private Purchase getActivePurchase(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            if (purchase.state == Purchase.State.PURCHASED) {
                return purchase;
            }
        }
        return null;
    }

    private boolean isInGrace(Player player, LocalDate membershipExpirationDate, Boolean autoRenewing) {
        if (!autoRenewing) {
            return false;
        }

        Map<Integer, Long> upgrades = player.getInventory().getUpgrades();
        LocalDate currentExpirationDate = DateUtils.fromMillis((upgrades.values().iterator().next()));
        LocalDate gracePeriodEnd = currentExpirationDate.plusDays(Constants.UPGRADE_GRACE_PERIOD_DAYS - 1);

        if (gracePeriodEnd.isBefore(membershipExpirationDate)) {
            return false;
        }

        LocalDate currentDate = LocalDate.now();
        if (DateUtils.isBetween(currentDate, currentExpirationDate.plusDays(1), gracePeriodEnd)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        checkout.stop();
        return true;
    }
}