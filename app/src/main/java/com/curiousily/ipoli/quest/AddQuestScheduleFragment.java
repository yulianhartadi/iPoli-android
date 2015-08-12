package com.curiousily.ipoli.quest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.ui.DatePickerFragment;
import com.curiousily.ipoli.ui.TimePickerFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/12/15.
 */
public class AddQuestScheduleFragment extends Fragment {

    @Bind(R.id.add_quest_duration)
    Spinner duration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @OnClick(R.id.add_quest_due_date)
    public void onDueDateClick(Button button) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    @OnClick(R.id.add_quest_start_time)
    public void onStartTimeClick(Button button) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_add_quest_schedule, container, false);
        ButterKnife.bind(this, view);

        List<String> list = new ArrayList<>();
        list.add("1 minute");
        list.add("2 minutes");
        list.add("3 minutes");
        list.add("5 minutes");
        list.add("10 minutes");
        list.add("15 minutes");
        list.add("20 minutes");
        list.add("25 minutes");
        list.add("30 minutes");
        list.add("45 minutes");
        list.add("1 hour");
        list.add("1 hour and 30 minutes");
        list.add("2 hours");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duration.setAdapter(dataAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_quest_schedule, menu);
        menu.findItem(R.id.action_next).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
