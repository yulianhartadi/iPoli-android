package io.ipoli.android.app.tutorial.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.TypewriterView;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/31/17.
 */
public class TipsFragment extends Fragment {

    @BindView(R.id.tutorial_text)
    TypewriterView tutorialText;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_name_prompt, container, false);
        unbinder = ButterKnife.bind(this, v);
        tutorialText.type("Let's start by adding your first quest! Do you see the button below? Will you do me the honor?");
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
