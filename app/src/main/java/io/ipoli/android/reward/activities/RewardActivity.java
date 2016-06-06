package io.ipoli.android.reward.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.quest.persistence.RewardPersistenceService;
import io.ipoli.android.reward.data.Reward;
import io.ipoli.android.reward.events.NewRewardSavedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class RewardActivity extends BaseActivity {
    private static final int MIN_PRICE = 100;
    private static final int MAX_PRICE = 1000;
    private static final int PRICE_STEP = 50;

    @Inject
    Bus eventBus;

    @Inject
    RewardPersistenceService rewardPersistenceService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.reward_name)
    TextInputEditText rewardName;

    @BindView(R.id.reward_price)
    Spinner rewardPrice;

    private boolean isEdit = false;
    private Reward reward;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null && getIntent().getStringExtra(Constants.REWARD_ID_EXTRA_KEY) != null) {
            isEdit = true;
            eventBus.post(new ScreenShownEvent(EventSource.EDIT_REWARD));
            setTitle(getString(R.string.reward_activity_edit_title));
            String rewardId = getIntent().getStringExtra(Constants.REWARD_ID_EXTRA_KEY);
            rewardPersistenceService.findById(rewardId).compose(bindToLifecycle()).subscribe(r -> {
                reward = r;
                initUI();
            });
        } else {
            eventBus.post(new ScreenShownEvent(EventSource.ADD_REWARD));
            initUI();
        }
    }

    private void initUI() {
        List<String> prices = new ArrayList<>();
        int i = MIN_PRICE;
        do {
            prices.add(i + "");
            i += PRICE_STEP;
        } while (i <= MAX_PRICE);


        rewardPrice.setAdapter(new ArrayAdapter<>(this, R.layout.reward_price_item, R.id.reward_price, prices));
        rewardPrice.setSelection(0, false);
        rewardPrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (reward != null) {
            rewardName.setText(reward.getName());
            int p = (reward.getPrice() - MIN_PRICE) / PRICE_STEP;
            rewardPrice.setSelection(p);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reward_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null && !isEdit) {
            menu.removeItem(R.id.action_delete);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveReward();
                return true;
            case R.id.action_delete:
                AlertDialog d = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_delete_reward_title))
                        .setMessage(getString(R.string.dialog_delete_reward_message)).create();
                d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_it), (dialogInterface, i) -> {
                    rewardPersistenceService.delete(reward).compose(bindToLifecycle()).subscribe(rewardId -> {
                        Toast.makeText(RewardActivity.this, R.string.reward_removed, Toast.LENGTH_SHORT).show();
                        setResult(Constants.RESULT_REMOVED);
                        finish();
                    });
                });
                d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> {
                });
                d.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveReward() {
        String name = rewardName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.reward_name_validation, Toast.LENGTH_SHORT).show();
            return;
        }
        int price = Integer.parseInt((String) rewardPrice.getSelectedItem());
        if (reward == null) {
            reward = new Reward(name, price);
        } else {
            reward.setName(name);
            reward.setPrice(price);
        }

        rewardPersistenceService.save(reward).compose(bindToLifecycle()).subscribe(reward -> {
            eventBus.post(new NewRewardSavedEvent(reward));
            Toast.makeText(this, "Reward saved", Toast.LENGTH_SHORT);
            finish();
        });
    }
}
