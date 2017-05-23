package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.store.adapters.StoreAdapter;
import io.ipoli.android.store.viewmodels.StoreViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class StoreFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    @BindView(R.id.root_layout)
    ViewGroup rootLayout;

    @BindView(R.id.items_list)
    EmptyStateRecyclerView itemList;

    private StoreAdapter adapter;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragement_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        itemList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        itemList.setEmptyView(rootLayout, R.string.empty_store_items, R.drawable.ic_coins_grey_24dp);

        List<StoreViewModel> storeViewModels = new ArrayList<>();
        storeViewModels.add(new StoreViewModel("Coins", R.drawable.pet_1));
        storeViewModels.add(new StoreViewModel("Upgrades", R.drawable.pet_2));
        storeViewModels.add(new StoreViewModel("Avatars", R.drawable.avatar_01));
        storeViewModels.add(new StoreViewModel("Pets", R.drawable.pet_3));
        adapter = new StoreAdapter(storeViewModels);
        itemList.setAdapter(adapter);
        return view;
    }

    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
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
    protected boolean useOptionsMenu() {
        return false;
    }
}
