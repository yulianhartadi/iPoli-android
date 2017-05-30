package io.ipoli.android.reward.data;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/17.
 */

public class RewardPurchase {
    private Long date;
    private Integer minute;

    public RewardPurchase(Long date, Integer minute) {
        this.date = date;
        this.minute = minute;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }
}
