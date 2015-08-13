package com.curiousily.ipoli.quest.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.AddQuestActivity;
import com.curiousily.ipoli.quest.Quest;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/13/15.
 */
public class AddQuestInfoFragment extends Fragment {

    @Bind(R.id.add_quest_context)
    Spinner spinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_add_quest_info, container, false);
        ButterKnife.bind(this, view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.md_blue_700));
        }
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        final AddQuestActivity activity = (AddQuestActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(R.string.add_quest_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
                activity.overrideExitAnimation();
            }
        });

        List<String> list = new ArrayList<>();
        for (Quest.Context context : Quest.Context.values()) {
            list.add(context.toString());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_quest_info, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                ((AddQuestActivity) getActivity()).onNextClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
