package io.ipoli.android.store.events;

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
