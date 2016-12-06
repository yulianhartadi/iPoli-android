package io.ipoli.android.shop.viewmodels;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/6/16.
 */

public class ProductViewModels {
    private String sku;
    private String name;
    private String price;

    public ProductViewModels(String sku, String name, String price) {
        this.sku = sku;
        this.name = name;
        this.price = price;
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
}
