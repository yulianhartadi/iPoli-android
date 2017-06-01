package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.UpgradeManager;
import io.ipoli.android.store.Upgrade;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.adapters.UpgradeStoreAdapter;
import io.ipoli.android.store.events.BuyUpgradeEvent;
import io.ipoli.android.store.events.UpgradeUnlockedEvent;
import io.ipoli.android.store.viewmodels.UpgradeViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class UpgradeStoreFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    @Inject
    UpgradeManager upgradeManager;

    private Unbinder unbinder;

    @BindView(R.id.upgrade_list)
    RecyclerView upgradeList;
    private UpgradeStoreAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragement_upgrade_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((StoreActivity) getActivity()).getSupportActionBar().setTitle(R.string.fragment_upgrade_store_title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        upgradeList.setLayoutManager(layoutManager);

        List<UpgradeViewModel> upgrades = createUpgradeViewModels();

        adapter = new UpgradeStoreAdapter(getContext(), eventBus, upgrades);
        upgradeList.setAdapter(adapter);

        eventBus.post(new ScreenShownEvent(EventSource.STORE_UPGRADES));

        return view;
    }

    @NonNull
    private List<UpgradeViewModel> createUpgradeViewModels() {
        List<UpgradeViewModel> upgrades = new ArrayList<>();
        List<Upgrade> lockedUpgrades = new ArrayList<>();
        List<Upgrade> unlockedUpgrades = new ArrayList<>();

        for (Upgrade upgrade : Upgrade.values()) {
            if (upgradeManager.isUnlocked(upgrade)) {
                unlockedUpgrades.add(upgrade);
            } else {
                lockedUpgrades.add(upgrade);
            }
        }

        Collections.sort(unlockedUpgrades, ((u1, u2) ->
                -Long.compare(upgradeManager.getUnlockDate(u1), upgradeManager.getUnlockDate(u2))));

        for (Upgrade upgrade : lockedUpgrades) {
            upgrades.add(new UpgradeViewModel(getContext(), upgrade));
        }

        for (Upgrade upgrade : unlockedUpgrades) {
            upgrades.add(new UpgradeViewModel(getContext(), upgrade, DateUtils.fromMillis(upgradeManager.getUnlockDate(upgrade))));
        }
        return upgrades;
    }

    @Subscribe
    public void buyUpgradeEvent(BuyUpgradeEvent e) {
        Upgrade upgrade = e.upgrade;
        String title = getString(upgrade.title);
        String message;
        if (upgradeManager.hasEnoughCoinsForUpgrade(upgrade)) {
            upgradeManager.unlock(upgrade);
            message = getString(R.string.upgrade_successfully_bought, title);
            adapter.setViewModels(createUpgradeViewModels());
            eventBus.post(new UpgradeUnlockedEvent(upgrade));
        } else {
            message = getString(R.string.upgrade_too_expensive, title);
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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

    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }
}
