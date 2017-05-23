package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import io.ipoli.android.store.StoreItemType;
import io.ipoli.android.store.adapters.StoreAdapter;
import io.ipoli.android.store.viewmodels.StoreViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class StoreFragment extends BaseFragment implements StoreAdapter.ItemSelectedListener {

    public static final String START_ITEM_TYPE = "start-item-type";
    private StoreItemType startStoreItemType;

    @Inject
    Bus eventBus;

    @BindView(R.id.items_list)
    RecyclerView itemList;

    private StoreAdapter adapter;
    private Unbinder unbinder;

    public static StoreFragment newInstance() {
        return newInstance(null);
    }

    public static StoreFragment newInstance(StoreItemType storeItemType) {
        StoreFragment fragment = new StoreFragment();
        if(storeItemType != null) {
            Bundle args = new Bundle();
            args.putString(START_ITEM_TYPE, storeItemType.name());
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey(START_ITEM_TYPE)) {
            startStoreItemType = StoreItemType.valueOf(getArguments().getString(START_ITEM_TYPE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragement_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        itemList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<StoreViewModel> storeViewModels = new ArrayList<>();
        storeViewModels.add(new StoreViewModel(StoreItemType.COINS, "Coins", R.drawable.pet_1));
        storeViewModels.add(new StoreViewModel(StoreItemType.UPGRADES, "Upgrades", R.drawable.pet_2));
        storeViewModels.add(new StoreViewModel(StoreItemType.AVATARS, "Avatars", R.drawable.avatar_01));
        storeViewModels.add(new StoreViewModel(StoreItemType.PETS, "Pets", R.drawable.pet_3));
        adapter = new StoreAdapter(storeViewModels, this);
        itemList.setAdapter(adapter);

        if(startStoreItemType != null) {
            changeCurrentItem(startStoreItemType);
        }

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

    @Override
    public void onItemSelected(StoreItemType type) {
        changeCurrentItem(type);
    }

    private void changeCurrentItem(StoreItemType type) {
        switch (type) {
            case COINS:
                changeCurrentFragment(new BuyCoinsFragment());
            case UPGRADES:
            case AVATARS:
            case PETS:
                break;
        }
    }

    private void changeCurrentFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .add(R.id.content_container, fragment)
                .addToBackStack(fragment.getClass().getName())
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
