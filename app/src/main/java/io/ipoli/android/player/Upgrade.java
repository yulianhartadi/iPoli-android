package io.ipoli.android.player;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/21/17.
 */

public enum Upgrade {
    CHALLENGES(1, 300),
    NOTES(2, 200),
    REMINDERS(3, 100),
    CALENDAR_SYNC(4, 500),
    SUB_QUESTS(5, 300),
    REPEATING_QUESTS(6, 2),
    TIMER(7, 200),
    CUSTOM_DURATION(8, 200);

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

    public static Upgrade get(int code) {
        for(Upgrade upgrade : values()) {
            if(upgrade.code == code) {
                return upgrade;
            }
        }
        return null;
    }
}