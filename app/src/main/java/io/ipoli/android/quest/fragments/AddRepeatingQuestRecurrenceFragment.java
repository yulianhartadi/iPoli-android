package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.quest.adapters.QuestOptionsAdapter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.events.NewRepeatingQuestRecurrencePickedEvent;
import io.ipoli.android.quest.ui.dialogs.RecurrencePickerFragment;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/15/17.
 */

public class AddRepeatingQuestRecurrenceFragment extends BaseFragment {

    @BindView(R.id.new_repeating_quest_recurrence_options)
    RecyclerView dateOptions;

    @BindView(R.id.new_repeating_quest_recurrence_image)
    ImageView image;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_repeating_quest_recurrence, container, false);
        unbinder = ButterKnife.bind(this, view);

        dateOptions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        dateOptions.setHasFixedSize(true);

        List<Pair<String, View.OnClickListener>> options = new ArrayList<>();

        options.add(new Pair<>(getString(R.string.once_a_week), v ->
                postEvent(new NewRepeatingQuestRecurrencePickedEvent(createTimesAWeekRecurrence(1)))));

        options.add(new Pair<>(getString(R.string.twice_a_week), v ->
                postEvent(new NewRepeatingQuestRecurrencePickedEvent(createTimesAWeekRecurrence(2)))));

        options.add(new Pair<>(getString(R.string.three_times_a_week), v ->
                postEvent(new NewRepeatingQuestRecurrencePickedEvent(createTimesAWeekRecurrence(3)))));

        options.add(new Pair<>(getString(R.string.five_times_a_week), v ->
                postEvent(new NewRepeatingQuestRecurrencePickedEvent(createTimesAWeekRecurrence(5)))));

        options.add(new Pair<>(getString(R.string.once_a_month), v ->
                postEvent(new NewRepeatingQuestRecurrencePickedEvent(createTimesAMonthRecurrence(1)))));

        options.add(new Pair<>(getString(R.string.every_day), v ->
                postEvent(new NewRepeatingQuestRecurrencePickedEvent(createEveryDayRecurrence()))));

        options.add(new Pair<>(getString(R.string.more_options), v -> {
            RecurrencePickerFragment fragment = RecurrencePickerFragment.newInstance(recurrence -> {
                postEvent(new NewRepeatingQuestRecurrencePickedEvent(recurrence));
            });
            fragment.show(getFragmentManager());
        }));

        dateOptions.setAdapter(new QuestOptionsAdapter(options));

        return view;
    }

    @NonNull
    private Recurrence createTimesAWeekRecurrence(int flexibleCount) {
        Recurrence recurrence = Recurrence.create();
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(flexibleCount);
        return recurrence;
    }

    @NonNull
    private Recurrence createTimesAMonthRecurrence(int flexibleCount) {
        Recurrence recurrence = Recurrence.create();
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        recurrence.setFlexibleCount(flexibleCount);
        return recurrence;
    }

    @NonNull
    private Recurrence createEveryDayRecurrence() {
        Recurrence recurrence = Recurrence.create();
        Recur recur = new Recur(Recur.DAILY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        recur.getDayList().add(WeekDay.SA);
        recur.getDayList().add(WeekDay.SU);
        recur.setFrequency(Recur.WEEKLY);
        recurrence.setRecurrenceType(Recurrence.RepeatType.DAILY);
        recurrence.setRrule(recur.toString());
        recurrence.setFlexibleCount(0);
        return recurrence;
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
