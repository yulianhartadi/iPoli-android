package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.store.adapters.UpgradeStoreAdapter;
import io.ipoli.android.store.viewmodels.UpgradeViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class UpgradeStoreFragment extends BaseFragment {

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        upgradeList.setLayoutManager(layoutManager);
        List<UpgradeViewModel> upgrades = new ArrayList<>();
        upgrades.add(new UpgradeViewModel("Repeating Quests", "Create repeating tasks",
                "Repeating Quests are called all your repeating tasks. iPoli automatically makes a quest in your calendar for the repeating one if you have to do it on a particular date.",
                300, R.drawable.ic_repeat_black_24dp));

        upgrades.add(new UpgradeViewModel("Challenges", "Challenge yourself",
                "Repeating Quests are called all your repeating tasks. iPoli automatically makes a quest in your calendar for the repeating one if you have to do it on a particular date.",
                500, R.drawable.ic_sword_black_24dp));

        adapter = new UpgradeStoreAdapter(upgrades);
        upgradeList.setAdapter(adapter);

        return view;
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
