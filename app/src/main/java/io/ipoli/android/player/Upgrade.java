package io.ipoli.android.player;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/21/17.
 */

public enum Upgrade {
    CHALLENGES(1, 300),
    NOTES(2, 200);

    private final int code;
    private final int price;

    Upgrade(int code, int price) {

        this.code = code;
        this.price = price;
    }

    public int getCode() {
        return code;
    }

    public int getPrice() {
        return price;
    }
}