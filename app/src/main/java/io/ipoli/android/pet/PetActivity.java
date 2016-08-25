package io.ipoli.android.pet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.pet.persistence.PetPersistenceService;
import io.ipoli.android.quest.persistence.OnDataChangedListener;
import io.ipoli.android.quest.ui.dialogs.TextPickerFragment;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/16.
 */
public class PetActivity extends BaseActivity implements OnDataChangedListener<Pet>, TextPickerFragment.OnTextPickedListener {

    @Inject
    PetPersistenceService petPersistenceService;

    @Inject
    AvatarPersistenceService avatarPersistenceService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.root_container)
    View backgroundImage;

    @BindView(R.id.pet_avatar)
    View avatar;

    @BindView(R.id.pet_xp_bonus)
    TextView xpBonus;

    @BindView(R.id.pet_coins_bonus)
    TextView coinsBonus;

    @BindView(R.id.pet_state)
    TextView state;

    @BindView(R.id.pet_hp)
    ProgressBar hp;

    @BindView(R.id.revive)
    Button revive;

    private Pet pet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        petPersistenceService.listen(this);
    }

    @Override
    protected void onStop() {
        petPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pet_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rename_pet:
                showRenamePetDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRenamePetDialog() {
        TextPickerFragment.newInstance(pet.getName(), R.string.rename_your_pet, this).show(getSupportFragmentManager());
    }

    @Override
    public void onTextPicked(String name) {
        if (StringUtils.isEmpty(name)) {
            return;
        }
        renamePet(name);
    }

    private void renamePet(String name) {
        pet.setName(name);
        petPersistenceService.save(pet);
    }

    @Override
    public void onDataChanged(Pet pet) {
        this.pet = pet;
        toolbar.setTitle(pet.getName());
        backgroundImage.setBackgroundResource(ResourceUtils.extractDrawableResource(this, pet.getBackgroundPicture()));
        avatar.setBackgroundResource(ResourceUtils.extractDrawableResource(this, pet.getPicture()));
        xpBonus.setText("XP: +" + pet.getExperienceBonusPercentage() + "%");
        coinsBonus.setText("Coins: +" + pet.getCoinsBonusPercentage() + "%");

        if (pet.getState() == Pet.PetState.DEAD) {
            revive.setText("400 coins");
            revive.setVisibility(View.VISIBLE);
            hp.setVisibility(View.GONE);
            state.setVisibility(View.GONE);
        } else {
            revive.setVisibility(View.GONE);
            hp.setVisibility(View.VISIBLE);
            state.setVisibility(View.VISIBLE);
            state.setText(pet.getStateText().toUpperCase());
            hp.setProgress(pet.getHealthPointsPercentage());
        }
    }

    @OnClick(R.id.revive)
    public void onReviveClick(View view) {
        this.pet.addHealthPoints(80);
        petPersistenceService.save(pet);
//        avatarPersistenceService.find(avatar -> {
//            avatar.getCoins()
//        });
    }
}