package io.ipoli.android.tutorial.fragments;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.tutorial.PickQuestViewModel;
import io.ipoli.android.tutorial.adapters.BasePickQuestAdapter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/28/16.
 */
public abstract class BasePickQuestsFragment<T> extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.quests_list)
    RecyclerView questList;

    protected BasePickQuestAdapter<T> pickQuestsAdapter;
    protected List<PickQuestViewModel<T>> viewModels = new ArrayList<>();
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pick_quests, container, false);
        unbinder = ButterKnife.bind(this, v);
        toolbar.setTitle(getTitleRes());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        initViewModels();
        initAdapter();
        questList.setAdapter(pickQuestsAdapter);
        return v;
    }

    protected abstract void initAdapter();


    protected abstract void initViewModels();

    @StringRes
    protected abstract int getTitleRes();

    public List<T> getSelectedQuests() {
        return pickQuestsAdapter.getSelectedQuests();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
