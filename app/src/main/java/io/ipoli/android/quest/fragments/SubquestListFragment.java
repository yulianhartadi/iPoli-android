package io.ipoli.android.quest.fragments;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
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
import io.ipoli.android.quest.adapters.SubquestListAdapter;
import io.ipoli.android.quest.data.Subquest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;

public class SubquestListFragment extends BaseFragment{

    @Inject
    Bus eventBus;

    @BindView(R.id.subquest_list)
    EmptyStateRecyclerView subquestList;

    private SubquestListAdapter adapter;

    QuestPersistenceService questPersistenceService;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_subquest_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        subquestList.setLayoutManager(layoutManager);

        List<Subquest> subquests = new ArrayList<>();
        subquests.add(new Subquest("Socks"));
        subquests.add(new Subquest("Bananas"));
        subquests.add(new Subquest("Crocodile"));
        subquests.add(new Subquest("Apples"));
        subquests.add(new Subquest("Cunka"));
        subquests.add(new Subquest("Tomatoes"));
        subquests.add(new Subquest("Ice cream"));
        subquests.add(new Subquest("Pencil"));
        subquests.add(new Subquest("Books"));
        subquests.add(new Subquest("Chocolate"));
        subquests.add(new Subquest("Milk"));

        adapter = new SubquestListAdapter(getContext(), eventBus, subquests);
        subquestList.setAdapter(adapter);

        questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());
        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
//        HelpDialog.newInstance(R.layout.fragment_help_dialog_inbox, R.string.help_dialog_inbox_title, "inbox").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onDestroyView() {
        questPersistenceService.removeAllListeners();
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
}
