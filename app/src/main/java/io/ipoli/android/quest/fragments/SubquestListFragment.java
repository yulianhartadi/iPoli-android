package io.ipoli.android.quest.fragments;


import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.adapters.SubquestListAdapter;
import io.ipoli.android.quest.data.Subquest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;


public class SubquestListFragment extends BaseFragment implements View.OnFocusChangeListener {

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
        subquests.add(new Subquest("Ball"));
        subquests.add(new Subquest("Table"));

        adapter = new SubquestListAdapter(getContext(), eventBus, subquests);
        subquestList.setAdapter(adapter);

        hideUnderline(addSubquest);
        addSubquest.setOnFocusChangeListener(this);

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
        } else {
            hideUnderline(addSubquest);
            if (StringUtils.isEmpty(text)) {
                setAddSubquestInViewMode();
            }
            addSubquest.clearFocus();
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
        adapter.addSubquest(new Subquest(name));
        setAddSubquestInViewMode();
        listContainer.requestFocus();
        hideKeyboard();
    }

    protected void showKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    protected void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
