package io.ipoli.android.store.viewmodels;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/6/16.
 */

public class CoinsViewModel {
    private final String sku;
    private final String name;
    private final String price;
    private final long value;

    public CoinsViewModel(String sku, String name, String price, long value) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getSku() {
        return sku;
    }

    public long getValue() {
        return value;
    }
}
