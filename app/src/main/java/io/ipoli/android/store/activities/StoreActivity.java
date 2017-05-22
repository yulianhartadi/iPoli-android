package io.ipoli.android.store.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.store.adapters.StoreAdapter;
import io.ipoli.android.store.viewmodels.StoreViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class StoreActivity extends BaseActivity {

    @Inject
    Bus eventBus;

    @BindView(R.id.root_layout)
    ViewGroup rootLayout;

    @BindView(R.id.items_list)
    EmptyStateRecyclerView itemList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private StoreAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        itemList.setLayoutManager(new GridLayoutManager(this, 2));
        itemList.setEmptyView(rootLayout, R.string.empty_store_items, R.drawable.ic_coins_grey_24dp);

        List<StoreViewModel> storeViewModels = new ArrayList<>();
        storeViewModels.add(new StoreViewModel("Coins", R.drawable.pet_1));
        storeViewModels.add(new StoreViewModel("Upgrades", R.drawable.pet_2));
        storeViewModels.add(new StoreViewModel("Avatars", R.drawable.avatar_01));
        storeViewModels.add(new StoreViewModel("Pets", R.drawable.pet_3));
        adapter = new StoreAdapter(storeViewModels);
        itemList.setAdapter(adapter);


        eventBus.post(new ScreenShownEvent(EventSource.STORE));
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }
}
