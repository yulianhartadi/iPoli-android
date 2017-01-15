package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
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
import io.ipoli.android.quest.events.NewQuestCategoryChangedEvent;
import io.ipoli.android.quest.events.NewQuestNameAndCategoryPickedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddQuestNameFragment extends BaseFragment implements CategoryView.OnCategoryChangedListener {

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.quest_name)
    TextInputEditText name;

    @BindView(R.id.quest_category)
    CategoryView category;

    private Unbinder unbinder;

    private Category currentCategory;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_name, container, false);
        unbinder = ButterKnife.bind(this, view);
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
                    Toast.makeText(getContext(), R.string.quest_name_validation, Toast.LENGTH_LONG).show();
                } else {
                    postEvent(new NewQuestNameAndCategoryPickedEvent(name.getText().toString(), currentCategory));
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
        postEvent(new NewQuestCategoryChangedEvent(category));
    }

    public String getName() {
        return name.getText().toString();
    }
}