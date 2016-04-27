package io.ipoli.android.tutorial;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.RecurrentQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickHabitsFragment extends Fragment {
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quests_list)
    RecyclerView habitsList;

    private PickHabitsAdapter pickHabitsAdapter;
    private List<RecurrentQuest> recurrentQuests = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pick_quests, container, false);
        ButterKnife.bind(this, v);
        toolbar.setTitle("Pick Habits to start with");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        habitsList.setLayoutManager(layoutManager);

        initHabits();
        pickHabitsAdapter = new PickHabitsAdapter(getContext(), recurrentQuests);
        habitsList.setAdapter(pickHabitsAdapter);

        return v;
    }

    private void initHabits() {
        recurrentQuests = new ArrayList<>();
        RecurrentQuest rq1 = new RecurrentQuest("Drink one glass of water 3 times per day every day");
        RecurrentQuest.setContext(rq1, QuestContext.WELLNESS);
        recurrentQuests.add(rq1);

        RecurrentQuest rq2 = new RecurrentQuest("Say 3 things I'm grateful for every day");
        RecurrentQuest.setContext(rq2, QuestContext.PERSONAL);
        recurrentQuests.add(rq2);
    }

    public List<RecurrentQuest> getSelectedHabits() {
        return pickHabitsAdapter.getSelectedRecurrentQuests();
    }
}
