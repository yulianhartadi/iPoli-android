package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.quest.adapters.QuestDateOptionsAdapter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */

public class QuestTimeFragment extends BaseFragment {

    @BindView(R.id.new_quest_time_options)
    RecyclerView timeOptions;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_date, container, false);
        unbinder = ButterKnife.bind(this, view);

        timeOptions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        timeOptions.setHasFixedSize(true);
        timeOptions.setAdapter(new QuestDateOptionsAdapter());

        return view;
    }


    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }
}
