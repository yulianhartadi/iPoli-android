package io.ipoli.android.reward.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.persistence.RewardPersistenceService;
import io.ipoli.android.reward.data.Reward;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class AddRewardActivity extends BaseActivity {

    @Inject
    Bus eventBus;

    @Inject
    RewardPersistenceService rewardPersistenceService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.reward_name)
    TextInputEditText rewardName;

    @BindView(R.id.reward_price)
    NumberPicker rewardPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reward);
        App.getAppComponent(this).inject(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_quest_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveReward();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveReward() {
        String name = rewardName.getText().toString();
        int price = rewardPrice.getValue();
        Reward r = new Reward(name, price);

        rewardPersistenceService.save(r).subscribe(reward -> {
                    Toast.makeText(this, "Reward saved", Toast.LENGTH_SHORT);
                });
    }
}
