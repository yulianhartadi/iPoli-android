package io.ipoli.android.pet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.pet.persistence.PetPersistenceService;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/16.
 */
public class PetActivity extends BaseActivity implements OnDataChangedListener<Pet> {

    @Inject
    PetPersistenceService petPersistenceService;

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
    public void onDataChanged(Pet pet) {
        toolbar.setTitle(pet.getName());
        backgroundImage.setBackgroundResource(ResourceUtils.extractDrawableResource(this, pet.getBackgroundPicture()));
        avatar.setBackgroundResource(ResourceUtils.extractDrawableResource(this, pet.getPicture()));
        xpBonus.setText("XP: +" + pet.getExperienceBonusPercentage() + "%");
        coinsBonus.setText("Coins: +" + pet.getCoinsBonusPercentage() + "%");
        state.setText(getStateText(pet.getHealthPoints()).toUpperCase());
        hp.setProgress(pet.getHealthPoints());
    }

    private String getStateText(Integer hp) {
        if(hp >= 90) {
            return "very happy";
        }
        if(hp >= 60) {
            return "happy";
        }
        if(hp >= 35) {
            return "good";
        }
        if(hp > 0) {
            return "sad";
        }
        return "dead";
    }
}
