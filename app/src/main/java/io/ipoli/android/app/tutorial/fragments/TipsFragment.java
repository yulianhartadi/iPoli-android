package io.ipoli.android.app.tutorial.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.tutorial.OnboardingActivity;
import io.ipoli.android.app.ui.CategoryView;
import io.ipoli.android.app.ui.TypewriterView;
import io.ipoli.android.app.utils.KeyboardUtils;

import static io.ipoli.android.app.utils.AnimationUtils.fadeIn;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/31/17.
 */
public class TipsFragment extends Fragment {

    @BindView(R.id.tutorial_text)
    TypewriterView tutorialText;

    @BindView(R.id.tutorial_add_quest)
    FloatingActionButton addQuest;

    @BindView(R.id.tutorial_quest_name)
    EditText questName;

    @BindView(R.id.tutorial_choose_category)
    Button chooseCategory;

    @BindView(R.id.tutorial_category_picker)
    CategoryView categoryPicker;

    @BindView(R.id.tutorial_choose_time)
    Button chooseTime;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_tips, container, false);
        unbinder = ButterKnife.bind(this, v);
        tutorialText.type("Let's start by adding your first quest! Do you see the button below? Will you do me the honor?");
        addQuest.setVisibility(View.VISIBLE);
        fadeIn(addQuest, 5000);
        return v;
    }

    @OnEditorAction(R.id.tutorial_quest_name)
    public boolean onDoneClicked(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onChooseCategory();
            return true;
        }

        return false;
    }

    private void onChooseCategory() {
        KeyboardUtils.hideKeyboard(getActivity());
        tutorialText.setText("");
        tutorialText.type("Choose a category");
        questName.setVisibility(View.GONE);
        chooseCategory.setVisibility(View.GONE);
        categoryPicker.setVisibility(View.VISIBLE);
        chooseTime.setVisibility(View.VISIBLE);
        fadeIn(categoryPicker, 1000);
        fadeIn(chooseTime, 1000);
    }

    @OnClick(R.id.tutorial_choose_category)
    public void onChooseCategoryClick(View v) {
        onChooseCategory();
    }

    @OnClick(R.id.tutorial_add_quest)
    public void onAddQuestClick(View view) {
        tutorialText.setText("");
        tutorialText.type("Name your first task");
        addQuest.setVisibility(View.GONE);
        questName.setVisibility(View.VISIBLE);
        chooseCategory.setVisibility(View.VISIBLE);
        fadeIn(questName, 1000);
        fadeIn(chooseCategory, 1000);
    }

    @OnClick(R.id.tutorial_choose_time)
    public void onChooseTimeClick(View view) {
        ((OnboardingActivity)getActivity()).onAddQuestDone(questName.getText().toString(), categoryPicker.getSelectedCategory());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
