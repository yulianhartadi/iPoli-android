package io.ipoli.android.shop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.pet.persistence.PetPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/26/16.
 */
public class ShopActivity extends BaseActivity {

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

        viewPager.setAdapter(new ShopPetAdapter(this));
    }
}
