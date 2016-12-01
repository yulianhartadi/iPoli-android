package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.iab.IabHelper;
import io.ipoli.android.app.utils.iab.IabResult;
import io.ipoli.android.app.utils.iab.Inventory;
import io.ipoli.android.app.utils.iab.Purchase;

public class IabActivity extends AppCompatActivity {
    private static final String SKU_COINS_100 = "coins100";
    private static final int RC_REQUEST = 10001;

    private IabHelper iabHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iab);
        ButterKnife.bind(this);

        String base64EncodedPublicKey = "CONSTRUCT_YOUR_KEY_AND_PLACE_IT_HERE";
        iabHelper = new IabHelper(this, base64EncodedPublicKey);

        iabHelper.startSetup(result -> {

            if (!result.isSuccess()) {
                return;
            }

            if (iabHelper == null) return;

            // Important: Dynamically register for broadcast messages about updated purchases.
            // We register the receiver here instead of as a <receiver> in the Manifest
            // because we always call getPurchases() at startup, so therefore we can ignore
            // any broadcasts sent while the app isn't running.
            // Note: registering this listener in an Activity is a bad idea, but is done here
            // because this is a SAMPLE. Regardless, the receiver must be registered after
            // IabHelper is setup, but before first call to getPurchases().
//                mBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
//                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
//                registerReceiver(mBroadcastReceiver, broadcastFilter);

            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            try {
                iabHelper.queryInventoryAsync(inventoryListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
            }
        });
    }

    IabHelper.QueryInventoryFinishedListener inventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (iabHelper == null) return;

            if (result.isFailure()) {
                return;
            }

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            Purchase gasPurchase = inventory.getPurchase(SKU_COINS_100);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                try {
                    iabHelper.consumeAsync(inventory.getPurchase(SKU_COINS_100), new IabHelper.OnConsumeFinishedListener() {
                        @Override
                        public void onConsumeFinished(Purchase purchase, IabResult result) {

                        }
                    });
                } catch (IabHelper.IabAsyncInProgressException e) {
                }
                return;
            }

//            updateUi();
//            setWaitScreen(false);
        }
    };

    @OnClick(R.id.buy_coins)
    public void onBuyCoinsClicked(View v) {
        String payload = "";

        try {
            iabHelper.launchPurchaseFlow(this, SKU_COINS_100, RC_REQUEST,
                    new IabHelper.OnIabPurchaseFinishedListener() {
                        @Override
                        public void onIabPurchaseFinished(IabResult result, Purchase info) {
                            Log.d("AAAA", "+ 100 coins");
                        }
                    }, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }


    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

//        if (mBroadcastReceiver != null) {
//            unregisterReceiver(mBroadcastReceiver);
//        }

        if (iabHelper != null) {
            iabHelper.disposeWhenFinished();
            iabHelper = null;
        }
    }
}
