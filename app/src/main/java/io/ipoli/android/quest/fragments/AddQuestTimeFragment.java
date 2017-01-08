package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import io.ipoli.android.quest.adapters.QuestOptionsAdapter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */

public class AddQuestTimeFragment extends BaseFragment {

    @BindView(R.id.new_quest_time_options)
    RecyclerView timeOptions;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_time, container, false);
        unbinder = ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        timeOptions.setLayoutManager(layoutManager);
        timeOptions.setHasFixedSize(true);

        List<Pair<String, View.OnClickListener>> options = new ArrayList<>();

        options.add(new Pair<>("Any reasonable time", v -> {
            Log.d("TEST", "Any reasonable time");
        }));

        options.add(new Pair<>("Work hours", v -> {
            Log.d("TEST", "Work hours");
        }));

        options.add(new Pair<>("Personal hours", v -> {
            Log.d("TEST", "Personal hours");
        }));

        options.add(new Pair<>("Morning", v -> {
            Log.d("TEST", "Morning");
        }));

        options.add(new Pair<>("Afternoon", v -> {
            Log.d("TEST", "Afternoon");
        }));

        options.add(new Pair<>("Evening", v -> {
            Log.d("TEST", "Evening");
        }));

        options.add(new Pair<>("At exactly...", v -> {
            Log.d("TEST", "At exactly");
        }));

        timeOptions.setAdapter(new QuestOptionsAdapter(options));

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
