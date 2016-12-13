package io.ipoli.android.reward.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.ui.dialogs.TextPickerFragment;
import io.ipoli.android.reward.data.Reward;
import io.ipoli.android.reward.events.NewRewardSavedEvent;
import io.ipoli.android.reward.formatters.PriceFormatter;
import io.ipoli.android.reward.persistence.RewardPersistenceService;
import io.ipoli.android.reward.ui.dialogs.PricePickerFragment;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class EditRewardActivity extends BaseActivity implements PricePickerFragment.OnPricePickedListener, TextPickerFragment.OnTextPickedListener {

    @Inject
    Bus eventBus;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.reward_name)
    TextInputEditText rewardName;

    @BindView(R.id.reward_description_value)
    TextView descriptionText;

    @BindView(R.id.reward_price_value)
    TextView priceText;

    private boolean isEdit = false;
    private Reward reward;

    @Inject
    RewardPersistenceService rewardPersistenceService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reward);
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
            rewardPersistenceService.findById(rewardId, reward -> {
                this.reward = reward;
                initUI();
            });
        } else {
            eventBus.post(new ScreenShownEvent(EventSource.ADD_REWARD));
            initUI();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initUI() {
        if (reward != null) {
            rewardName.setText(reward.getName());
            rewardName.setSelection(reward.getName().length());
            setPriceText(reward.getPrice());
            setDescriptionText(reward.getDescription());
        } else {
            setDescriptionText("");
            setPriceText(Constants.DEFAULT_MIN_REWARD_PRICE);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                saveReward();
                return true;
            case R.id.action_delete:
                AlertDialog d = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_delete_reward_title))
                        .setMessage(getString(R.string.dialog_delete_reward_message)).create();
                d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_it), (dialogInterface, i) -> {
                    rewardPersistenceService.delete(reward);
                    Toast.makeText(EditRewardActivity.this, R.string.reward_deleted, Toast.LENGTH_SHORT).show();
                    setResult(Constants.RESULT_REMOVED);
                    finish();
                });
                d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> {
                });
                d.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.reward_description_container)
    public void onDescriptionClick(View view) {
        TextPickerFragment.newInstance((String) descriptionText.getTag(), R.string.reward_description_question, this).show(getSupportFragmentManager());
    }

    @OnClick(R.id.reward_price_container)
    public void onPriceClick(View view) {
        int price = priceText.getTag() != null ? (int) priceText.getTag() : -1;
        PricePickerFragment f = PricePickerFragment.newInstance(price, this);
        f.show(this.getSupportFragmentManager());
    }

    @Override
    public void onTextPicked(String text) {
        setDescriptionText(text);
    }

    @Override
    public void onPricePicked(int price) {
        setPriceText(price);
    }

    private void setDescriptionText(String description) {
        descriptionText.setTag(description);
        if (StringUtils.isEmpty(description)) {
            descriptionText.setText(R.string.unknown_choice);
            return;
        }
        descriptionText.setText(description);
    }

    private void setPriceText(int price) {
        priceText.setText(PriceFormatter.formatReadable(price));
        priceText.setTag(price);
    }

    private void saveReward() {
        String name = rewardName.getText().toString();
        if (StringUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.reward_name_validation, Toast.LENGTH_SHORT).show();
            return;
        }
        int price = Math.max((int) priceText.getTag(), Constants.DEFAULT_MIN_REWARD_PRICE);
        if (reward == null) {
            reward = new Reward(name.trim(), price);
        } else {
            reward.setName(name.trim());
            reward.setPrice(price);
        }

        String description = (String) descriptionText.getTag();
        if (!StringUtils.isEmpty(description)) {
            reward.setDescription(description);
        }

        rewardPersistenceService.save(reward);
        eventBus.post(new NewRewardSavedEvent(reward));
        Toast.makeText(this, R.string.reward_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
