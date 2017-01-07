package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ipoli.android.R;
import io.ipoli.android.app.BaseFragment;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class QuestDateFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_quest_date, container, false);
        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }
}
