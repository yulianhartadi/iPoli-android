package io.ipoli.android.store.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/6/16.
 */
public class GoPremiumTappedEvent {
    public final String sku;

    public GoPremiumTappedEvent(String sku) {
        this.sku = sku;
    }
}
