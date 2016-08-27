package io.ipoli.android.shop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.pet.persistence.PetPersistenceService;
import io.ipoli.android.shop.events.BuyPetRequestEvent;
import io.ipoli.android.shop.viewmodels.PetViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/26/16.
 */
public class ShopActivity extends BaseActivity {

    @Inject
    Bus eventBus;

    @Inject
    PetPersistenceService petPersistenceService;

    @Inject
    AvatarPersistenceService avatarPersistenceService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.pet_view_pager)
    HorizontalInfiniteCycleViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        petPersistenceService.find(pet -> {
            String[] descriptions = new String[]{"Cute tail", "Fancy ears", "Flying ears", "Crazy teeth", "Chicken invader"};
            List<PetViewModel> petViewModels = new ArrayList<>();
            for (int i = 0; i < descriptions.length; i++) {
                int petIndex = i + 1;
                String pictureName = "pet_" + petIndex;
                if (pet.getPicture().equals(pictureName)) {
                    continue;
                }
                petViewModels.add(new PetViewModel(descriptions[i], 500,
                        ResourceUtils.extractDrawableResource(this, pictureName),
                        ResourceUtils.extractDrawableResource(this, pictureName + "_happy"), pictureName));
            }
            viewPager.setAdapter(new ShopPetAdapter(this, petViewModels, eventBus));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_pick_daily_challenge_quests).setVisible(false);
        menu.findItem(R.id.action_help).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Subscribe
    public void onBuyPetRequest(BuyPetRequestEvent e) {
        avatarPersistenceService.find(avatar -> {
            PetViewModel vm = e.petViewModel;
            if (avatar.getCoins() < vm.getPrice()) {
                Toast.makeText(this, R.string.not_enough_coins_to_buy_pet, Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.logo)
                    .setTitle(R.string.buy_pet_confirm_title)
                    .setMessage(R.string.buy_pet_confirm_message)
                    .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                        avatar.removeCoins(vm.getPrice());
                        avatarPersistenceService.save(avatar, () -> {
                            petPersistenceService.find(pet -> {
                                pet.setPicture(vm.getPictureName());
                                pet.setHealthPointsPercentage(Constants.DEFAULT_PET_HP);
                                petPersistenceService.save(pet, this::finish);
                            });
                        });
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {

                    })
                    .show();
        });

    }
}
