package io.ipoli.android.store.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/8/16.
 */
public class CoinsPurchasedEvent {
    public final String sku;

    public CoinsPurchasedEvent(String sku) {
        this.sku = sku;
    }
}
