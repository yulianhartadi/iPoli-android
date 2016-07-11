package io.ipoli.android.quest.fragments;


import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.adapters.SubquestListAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.UpdateQuestEvent;
import io.ipoli.android.quest.events.subquests.AddSubquestTappedEvent;
import io.ipoli.android.quest.events.subquests.NewSubquestEvent;
import io.ipoli.android.quest.events.subquests.SaveSubquestsRequestEvent;
import io.ipoli.android.quest.persistence.OnSingleDatabaseObjectChangedListener;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;


public class SubquestListFragment extends BaseFragment implements View.OnFocusChangeListener, OnSingleDatabaseObjectChangedListener<Quest> {

    @Inject
    Bus eventBus;

    @BindView(R.id.add_subquest)
    TextInputEditText addSubquest;

    @BindView(R.id.list_container)
    ViewGroup listContainer;

    @BindView(R.id.subquest_list)
    EmptyStateRecyclerView subquestList;

    private SubquestListAdapter adapter;

    QuestPersistenceService questPersistenceService;

    private Unbinder unbinder;
    private String questId;
    private Quest quest;

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

        questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());
        questId = getActivity().getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId, this);

        adapter = new SubquestListAdapter(getContext(), eventBus, new ArrayList<>());
        subquestList.setAdapter(adapter);

        hideUnderline(addSubquest);
        addSubquest.setOnFocusChangeListener(this);

        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_quest, R.string.help_dialog_quest_title, "quest").show(getActivity().getSupportFragmentManager());
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
        if(getUserVisibleHint()) {
            saveSubquests();
        }
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onSaveSubquestsRequest(SaveSubquestsRequestEvent e) {
        saveSubquests();
    }

    private void saveSubquests() {
        eventBus.post(new UpdateQuestEvent(quest, adapter.getSubQuests(), null, EventSource.SUBQUESTS));
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        if(addSubquest == null) {
            return;
        }
        String text = addSubquest.getText().toString();
        if (isFocused) {
            showUnderline(addSubquest);
            if (text.equals(getString(R.string.add_sub_quest))) {
                setAddSubquestInEditMode();
            }
            addSubquest.requestFocus();
            eventBus.post(new AddSubquestTappedEvent(EventSource.SUBQUESTS));
        } else {
            hideUnderline(addSubquest);
            if (StringUtils.isEmpty(text)) {
                setAddSubquestInViewMode();
            }
        }
    }

    private void setAddSubquestInViewMode() {
        addSubquest.setText(getString(R.string.add_sub_quest));
        addSubquest.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }

    private void setAddSubquestInEditMode() {
        addSubquest.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
        addSubquest.setText("");
    }

    private void showUnderline(View view) {
        view.getBackground().clearColorFilter();
    }

    private void hideUnderline(View view) {
        view.getBackground().setColorFilter(ContextCompat.getColor(getContext(), android.R.color.transparent), PorterDuff.Mode.SRC_IN);
    }

    @OnEditorAction(R.id.add_subquest)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            addSubquest();
            return true;
        } else {
            return false;
        }
    }

    private void addSubquest() {
        String name = addSubquest.getText().toString();
        if(StringUtils.isEmpty(name)) {
            return;
        }

        SubQuest sq = new SubQuest(name);
        adapter.addSubquest(sq);
        eventBus.post(new NewSubquestEvent(sq, EventSource.SUBQUESTS));
        setAddSubquestInEditMode();
    }

    @Override
    public void onDatabaseObjectChanged(Quest result) {
        quest = result;
        adapter.setSubQuests(result.getSubQuests());
    }
}
