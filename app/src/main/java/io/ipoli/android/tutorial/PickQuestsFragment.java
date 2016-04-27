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
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickQuestsFragment extends Fragment {
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quests_list)
    RecyclerView habitsList;

    private PickQuestsAdapter pickHabitsAdapter;
    private List<Quest> quests = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pick_quests, container, false);
        ButterKnife.bind(this, v);
        toolbar.setTitle("Pick Quests to start with");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        habitsList.setLayoutManager(layoutManager);

        initQuests();
        pickHabitsAdapter = new PickQuestsAdapter(getContext(), quests);
        habitsList.setAdapter(pickHabitsAdapter);

        return v;
    }

    private void initQuests() {
        Quest trashQuest = new Quest("Throw away the trash", new Date());
        trashQuest.setRawText("Throw away the trash today");
        Quest.setContext(trashQuest, QuestContext.CHORES);
        quests.add(trashQuest);

        Quest defrostQuest = new Quest("Defrost the freezer");
        defrostQuest.setRawText("Defrost the freezer");
        Quest.setContext(defrostQuest, QuestContext.CHORES);
        quests.add(defrostQuest);

        Quest dentistQuest = new Quest("Go to the dentist");
        dentistQuest.setRawText("Go to the dentist");
        Quest.setContext(dentistQuest, QuestContext.PERSONAL);
        quests.add(dentistQuest);
    }

    public List<Quest> getSelectedQuests() {
        return pickHabitsAdapter.getSelectedQuests();
    }
}
