package io.ipoli.android.reward.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class AddRewardActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.reward_price)
    NumberPicker rewardPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reward);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }


        List<String> prices = new ArrayList<>();
        int priceCount = 1000 / 25;
        for (int i = 0; i < priceCount; i++) {
            prices.add(i + "");
        }

        rewardPrice.setDisplayedValues(null);


        rewardPrice.setMinValue(0);
        rewardPrice.setMaxValue(prices.size() - 1);
        rewardPrice.setValue(4);

        rewardPrice.setDisplayedValues(prices.toArray(new String[prices.size()]));
    }
}
