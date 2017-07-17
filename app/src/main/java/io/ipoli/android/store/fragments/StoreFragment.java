package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.store.StoreItemType;
import io.ipoli.android.store.activities.StoreActivity;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class StoreFragment extends BaseFragment {

    public static final String START_ITEM_TYPE = "start-item-type";

    private StoreItemType startStoreItemType;

    private Unbinder unbinder;

    public static StoreFragment newInstance() {
        return newInstance(null);
    }

    public static StoreFragment newInstance(StoreItemType storeItemType) {
        StoreFragment fragment = new StoreFragment();
        if (storeItemType != null) {
            Bundle args = new Bundle();
            args.putString(START_ITEM_TYPE, storeItemType.name());
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(START_ITEM_TYPE)) {
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
        ((StoreActivity) getActivity()).getSupportActionBar().setTitle(R.string.fragment_store_title);

        if (startStoreItemType != null) {
            changeCurrentItem(startStoreItemType);
        }

        return view;
    }

    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    @OnClick(R.id.store_coins_container)
    public void onCoinsClicked(View v) {
        changeCurrentItem(StoreItemType.MEMBERSHIP);
    }

    @OnClick(R.id.store_upgrades_container)
    public void onUpgradesClicked(View v) {
        changeCurrentItem(StoreItemType.UPGRADES);
    }

    @OnClick(R.id.store_avatars_container)
    public void onAvatarsClicked(View v) {
        changeCurrentItem(StoreItemType.AVATARS);
    }

    @OnClick(R.id.store_pets_container)
    public void onPetsClicked(View v) {
        changeCurrentItem(StoreItemType.PETS);
    }

    private void changeCurrentItem(StoreItemType type) {
        switch (type) {
            case MEMBERSHIP:
                changeCurrentFragment(new MembershipStoreFragment());
                break;
            case UPGRADES:
                changeCurrentFragment(new UpgradeStoreFragment());
                break;
            case AVATARS:
                changeCurrentFragment(new AvatarStoreFragment());
                break;
            default:
                changeCurrentFragment(new PetStoreFragment());
        }
    }

    private void changeCurrentFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .add(R.id.content_container, fragment)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }
}
