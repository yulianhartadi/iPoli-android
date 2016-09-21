package io.ipoli.android.app.tutorial.fragments;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.quest.adapters.BasePickQuestAdapter;
import io.ipoli.android.app.tutorial.PickQuestViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/28/16.
 */
public abstract class BaseTutorialPickQuestsFragment<T> extends Fragment implements ISlideBackgroundColorHolder {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.quests_list)
    RecyclerView questList;

    protected BasePickQuestAdapter pickQuestsAdapter;
    protected List<PickQuestViewModel> viewModels = new ArrayList<>();
    private Unbinder unbinder;
    private View contentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_pick_quests, container, false);
        unbinder = ButterKnife.bind(this, contentView);
        toolbar.setTitle(getTitleRes());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        initViewModels();
        initAdapter();
        questList.setAdapter(pickQuestsAdapter);
        return contentView;
    }

    protected abstract void initAdapter();


    protected abstract void initViewModels();

    @StringRes
    protected abstract int getTitleRes();

    public List<T> getSelectedQuests() {
        return (List<T>) pickQuestsAdapter.getSelectedBaseQuests();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        contentView.setBackgroundColor(backgroundColor);
    }
}
