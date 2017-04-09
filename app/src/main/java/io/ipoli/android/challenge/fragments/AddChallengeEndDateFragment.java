package io.ipoli.android.challenge.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.dialogs.DatePickerFragment;
import io.ipoli.android.challenge.events.NewChallengeEndDatePickedEvent;
import io.ipoli.android.quest.adapters.QuestOptionsAdapter;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddChallengeEndDateFragment extends BaseFragment {

    @BindView(R.id.new_quest_date_options)
    RecyclerView dateOptions;

    @BindView(R.id.new_quest_date_image)
    ImageView image;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_challenge_end_date, container, false);
        unbinder = ButterKnife.bind(this, view);

        dateOptions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        dateOptions.setHasFixedSize(true);

        List<Pair<String, View.OnClickListener>> options = new ArrayList<>();

        LocalDate today = LocalDate.now();
        options.add(new Pair<>(getString(R.string.one_month), v -> {
            postEvent(new NewChallengeEndDatePickedEvent(today.plusMonths(1)));
        }));

        options.add(new Pair<>(getString(R.string.one_week), v ->
                postEvent(new NewChallengeEndDatePickedEvent(today.plusWeeks(1)))));

        options.add(new Pair<>(getString(R.string.ten_days), v ->
                postEvent(new NewChallengeEndDatePickedEvent(today.plusDays(10)))));

        options.add(new Pair<>(getString(R.string.two_weeks), v ->
                postEvent(new NewChallengeEndDatePickedEvent(today.plusWeeks(2)))));

        options.add(new Pair<>(getString(R.string.three_months), v ->
                postEvent(new NewChallengeEndDatePickedEvent(today.plusMonths(3)))));

        options.add(new Pair<>(getString(R.string.fifteen_days),
                v -> postEvent(new NewChallengeEndDatePickedEvent(today.plusDays(15)))));

        options.add(new Pair<>(getString(R.string.exact_date), v -> {
            DatePickerFragment fragment = DatePickerFragment.newInstance(LocalDate.now(), true, false,
                    date -> postEvent(new NewChallengeEndDatePickedEvent(date)));
            fragment.show(getFragmentManager());
        }));



        dateOptions.setAdapter(new QuestOptionsAdapter(options));


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
