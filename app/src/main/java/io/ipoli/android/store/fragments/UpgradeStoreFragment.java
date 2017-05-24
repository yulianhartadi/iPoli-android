package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.player.Upgrade;
import io.ipoli.android.player.UpgradesManager;
import io.ipoli.android.store.adapters.UpgradeStoreAdapter;
import io.ipoli.android.store.events.BuyUpgradeEvent;
import io.ipoli.android.store.viewmodels.UpgradeViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class UpgradeStoreFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    @Inject
    UpgradesManager upgradesManager;

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
        getActivity().setTitle("Buy upgrade");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        upgradeList.setLayoutManager(layoutManager);
        List<UpgradeViewModel> upgrades = new ArrayList<>();

        upgrades.add(new UpgradeViewModel("Challenges", "Challenge yourself",
                "Repeating Quests are called all your repeating tasks. iPoli automatically makes a quest in your calendar for the repeating one if you have to do it on a particular date.",
                500, R.drawable.ic_sword_white_24dp, Upgrade.CHALLENGES, false, null));

        upgrades.add(new UpgradeViewModel("Reminders", "Challenge yourself",
                "Repeating Quests are called all your repeating tasks. iPoli automatically makes a quest in your calendar for the repeating one if you have to do it on a particular date.",
                200, R.drawable.ic_reminders_white_24dp, Upgrade.REMINDERS, false, null));

        upgrades.add(new UpgradeViewModel("Sub Quests", "Challenge yourself",
                "Repeating Quests are called all your repeating tasks. iPoli automatically makes a quest in your calendar for the repeating one if you have to do it on a particular date.",
                100, R.drawable.ic_format_list_bulleted_white_24dp, Upgrade.REMINDERS, false, null));

        upgrades.add(new UpgradeViewModel("Notes", "Challenge yourself",
                "Repeating Quests are called all your repeating tasks. iPoli automatically makes a quest in your calendar for the repeating one if you have to do it on a particular date.",
                400, R.drawable.ic_note_white_24dp, Upgrade.NOTES, true, LocalDate.now().minusDays(1)));

        upgrades.add(new UpgradeViewModel("Repeating Quests", "Create repeating tasks",
                "Repeating Quests are called all your repeating tasks. iPoli automatically makes a quest in your calendar for the repeating one if you have to do it on a particular date.",
                300, R.drawable.ic_repeat_white_24dp, Upgrade.REPEATING_QUESTS, true, LocalDate.now().minusDays(2)));

        adapter = new UpgradeStoreAdapter(getContext(), eventBus, upgrades);
        upgradeList.setAdapter(adapter);

        return view;
    }

    @Subscribe
    public void buyUpgradeEvent(BuyUpgradeEvent e) {
        if(upgradesManager.hasEnoughCoinsForUpgrade(e.upgrade)) {
            upgradesManager.buy(e.upgrade);
            Toast.makeText(getContext(), "You can now enjoy ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Not enough coins to buy", Toast.LENGTH_SHORT).show();
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

    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }
}
