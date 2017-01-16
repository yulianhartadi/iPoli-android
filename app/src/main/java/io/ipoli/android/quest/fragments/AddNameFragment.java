package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.CategoryView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.events.CategoryChangedEvent;
import io.ipoli.android.quest.events.NameAndCategoryPickedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddNameFragment extends BaseFragment implements CategoryView.OnCategoryChangedListener {

    private static final String HINT_NAME_RES = "hint_name_resource";
    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.wizard_item_name_layout)
    TextInputLayout nameLayout;

    @BindView(R.id.wizard_item_name)
    TextInputEditText name;

    @BindView(R.id.wizard_item_category)
    CategoryView category;

    private Unbinder unbinder;

    private Category currentCategory;

    private int hintName;

    public static AddNameFragment newInstance(@StringRes int hintName) {
        AddNameFragment fragment = new AddNameFragment();
        Bundle args = new Bundle();
        args.putInt(HINT_NAME_RES, hintName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments().containsKey(HINT_NAME_RES)) {
            hintName = getArguments().getInt(HINT_NAME_RES);
        } else {
            hintName = R.string.add_quest_name_hint;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_name, container, false);
        unbinder = ButterKnife.bind(this, view);
        nameLayout.setHint(getString(hintName));
        category.addCategoryChangedListener(this);
        currentCategory = Category.LEARNING;
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_quest_wizard_name_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                if(StringUtils.isEmpty(name.getText().toString())) {
                    Toast.makeText(getContext(), R.string.name_validation, Toast.LENGTH_SHORT).show();
                } else {
                    postEvent(new NameAndCategoryPickedEvent(name.getText().toString(), currentCategory));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        category.removeCategoryChangedListener(this);
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    public void onCategoryChanged(Category category) {
        this.currentCategory = category;
        postEvent(new CategoryChangedEvent(category));
    }

    public String getName() {
        return name.getText().toString();
    }
}