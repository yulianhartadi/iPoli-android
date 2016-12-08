package io.ipoli.android.shop.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/6/16.
 */
public class BuyCoinsTappedEvent {
    public final String sku;

    public BuyCoinsTappedEvent(String sku) {
        this.sku = sku;
    }
}
