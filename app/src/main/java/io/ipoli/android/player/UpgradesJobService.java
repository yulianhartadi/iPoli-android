package io.ipoli.android.player;

import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.ipoli.android.BillingConstants;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.QuickAddActivity;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.data.MembershipType;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.Upgrade;

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
            return finishJobOnMainThread(params);
        }

        LocalDate currentDate = LocalDate.now();

        if (player.getMembership() == MembershipType.NONE) {
            LocalDate createdAt = player.getCreatedAtDate();
            if (isOnLastDayOfTrial(createdAt, currentDate)) {
                showTrialExpiringNotification();
                return finishJobOnMainThread(params);
            }

            List<Upgrade> expiringUpgrades = new ArrayList<>();
            for (Map.Entry<Upgrade, LocalDate> entry : player.getUpgrades().entrySet()) {
                if (entry.getValue().equals(currentDate)) {
                    expiringUpgrades.add(entry.getKey());
                }
            }

            showUpgradesExpiringNotification(expiringUpgrades);
            return finishJobOnMainThread(params);
        }

        List<String> skus = new ArrayList<>();
        skus.add(Constants.SKU_SUBSCRIPTION_MONTHLY);
        skus.add(Constants.SKU_SUBSCRIPTION_QUARTERLY);
        skus.add(Constants.SKU_SUBSCRIPTION_YEARLY);
        checkout.loadInventory(Inventory.Request.create().loadAllPurchases()
                .loadSkus(ProductTypes.SUBSCRIPTION, skus), products -> {
            Inventory.Product subscriptions = products.get(ProductTypes.SUBSCRIPTION);
            Purchase purchase = getActivePurchase(subscriptions.getPurchases());

            if (purchase == null) {
                player.setMembership(MembershipType.NONE);
                player.getInventory().unlockAllUpgrades(currentDate.minusDays(1));
                playerPersistenceService.save(player);
                jobFinished(params, false);
                return;
            }

            api.getMembershipStatus(purchase.sku, purchase.token, createMembershipStatusResponseListener(currentDate, player, params));
        });


        return true;
    }

    private Api.MembershipStatusResponseListener createMembershipStatusResponseListener(LocalDate currentDate, Player player, JobParameters params) {
        return new Api.MembershipStatusResponseListener() {
            @Override
            public void onSuccess(Long startTimeMillis, Long expiryTimeMillis, Boolean autoRenewing) {
                LocalDate membershipExpirationDate = DateUtils.fromMillis(expiryTimeMillis);

                if (!autoRenewing && membershipExpirationDate.isEqual(currentDate)) {
                    showMembershipExpiringTodayNotification();
                    jobFinished(params, false);
                    return;
                }

                if (autoRenewing) {
                    LocalDate gracePeriodStart = membershipExpirationDate.minusDays(Constants.UPGRADE_GRACE_PERIOD_DAYS - 1);
                    if (isInGracePeriod(membershipExpirationDate, gracePeriodStart, currentDate)) {
                        player.getInventory().unlockAllUpgrades(membershipExpirationDate);
                        playerPersistenceService.save(player);
                        showMembershipExpiringAfterDays((int) ChronoUnit.DAYS.between(currentDate, membershipExpirationDate) + 1);
                        jobFinished(params, false);
                        return;
                    }

                    player.getInventory().unlockAllUpgrades(membershipExpirationDate.minusDays(Constants.UPGRADE_GRACE_PERIOD_DAYS));
                    playerPersistenceService.save(player);
                    jobFinished(params, false);

                }
            }

            @Override
            public void onError(Exception e) {
                //log error
                jobFinished(params, true);
            }
        };
    }

    private boolean isInGracePeriod(LocalDate membershipExpirationDate, LocalDate gracePeriodStart, LocalDate currentDate) {
        return DateUtils.isBetween(currentDate, gracePeriodStart, membershipExpirationDate);
    }

    private void showMembershipExpiringAfterDays(int days) {

    }

    private void showMembershipExpiringTodayNotification() {

    }

    protected boolean finishJobOnMainThread(JobParameters params) {
        jobFinished(params, false);
        return false;
    }

    private void showUpgradesExpiringNotification(List<Upgrade> expiringUpgrades) {
        if (expiringUpgrades.isEmpty()) {
            return;
        }
    }

    private void showTrialExpiringNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Intent addIntent = new Intent(this, QuickAddActivity.class);
        addIntent.putExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT, " " + getString(R.string.today).toLowerCase());

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.trial_expiring_title))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.trial_expiring_message)))
                .setContentText(getString(R.string.trial_expiring_message))
                .setContentIntent(contentIntent)
                .setShowWhen(true)
                .setSmallIcon(R.drawable.ic_crown_white_24dp)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColor(ContextCompat.getColor(this, R.color.md_yellow_700))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.MEMBERSHIP_EXPIRATION_NOTIFICATION_ID, builder.build());
    }

    private boolean isOnLastDayOfTrial(LocalDate createdAt, LocalDate currentDate) {
        return createdAt.plusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS - 1).isEqual(currentDate);
    }

    private Purchase getActivePurchase(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            if (purchase.state == Purchase.State.PURCHASED) {
                return purchase;
            }
        }
        return null;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        checkout.stop();
        return true;
    }
}