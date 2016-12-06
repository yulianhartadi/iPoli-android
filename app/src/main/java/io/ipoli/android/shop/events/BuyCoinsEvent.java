package io.ipoli.android.shop.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/6/16.
 */
public class BuyCoinsEvent {
    public final String sku;

    public BuyCoinsEvent(String sku) {
        this.sku = sku;
    }
}
