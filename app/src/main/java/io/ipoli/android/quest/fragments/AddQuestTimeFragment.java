package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.dialogs.TimePickerFragment;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.adapters.QuestOptionsAdapter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.events.NewQuestTimePickedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */

public class AddQuestTimeFragment extends BaseFragment {

    @BindView(R.id.new_quest_time_options)
    RecyclerView timeOptions;

    @BindView(R.id.new_quest_time_image)
    ImageView image;

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

        options.add(new Pair<>(getString(R.string.any_reasonable_time), v ->
                postEvent(new NewQuestTimePickedEvent(TimePreference.ANY))));

        options.add(new Pair<>(getString(R.string.work_hours), v ->
                postEvent(new NewQuestTimePickedEvent(TimePreference.WORK_HOURS))));

        options.add(new Pair<>(getString(R.string.personal_hours), v ->
                postEvent(new NewQuestTimePickedEvent(TimePreference.PERSONAL_HOURS))));

        options.add(new Pair<>(getString(R.string.morning), v ->
                postEvent(new NewQuestTimePickedEvent(TimePreference.MORNING))));

        options.add(new Pair<>(getString(R.string.afternoon), v ->
                postEvent(new NewQuestTimePickedEvent(TimePreference.AFTERNOON))));

        options.add(new Pair<>(getString(R.string.evening), v ->
                postEvent(new NewQuestTimePickedEvent(TimePreference.EVENING))));

        options.add(new Pair<>(getString(R.string.at_exactly), v -> {
            TimePickerFragment fragment = TimePickerFragment.newInstance(false, time ->
                    postEvent(new NewQuestTimePickedEvent(time)));
            fragment.show(getFragmentManager());
        }));

        timeOptions.setAdapter(new QuestOptionsAdapter(options));

        return view;
    }

    public void setCategory(Category category) {
        switch (category) {
            case LEARNING:
                image.setImageResource(R.drawable.new_learning_quest);
                break;
            case WELLNESS:
                image.setImageResource(R.drawable.new_wellness_quest);
                break;
            case WORK:
                image.setImageResource(R.drawable.new_work_quest);
                break;
            case PERSONAL:
                image.setImageResource(R.drawable.new_personal_quest);
                break;
            case FUN:
                image.setImageResource(R.drawable.new_fun_quest);
                break;
            case CHORES:
                image.setImageResource(R.drawable.new_chores_quest);
                break;
        }
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
