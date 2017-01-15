package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/15/17.
 */

public class AddRepeatingQuestRecurrenceFragment extends BaseFragment {

    private Unbinder unbinder;

    @BindView(R.id.recurrence_frequency)
    Spinner recurrenceFrequency;

    @BindView(R.id.new_repeating_quest_recurrence_image)
    ImageView image;

    private List<String> frequencies = Arrays.asList("Daily", "Weekly", "Monthly");
    private List<String> flexibleFrequencies = Arrays.asList("Weekly", "Monthly");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_repeating_quest_recurrence, container, false);
        unbinder = ButterKnife.bind(this, view);

        recurrenceFrequency.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, frequencies));

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
