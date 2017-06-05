package io.ipoli.android.app.tutorial.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import io.ipoli.android.app.ui.TypewriterView;
import io.ipoli.android.app.utils.KeyboardUtils;

import static io.ipoli.android.app.utils.AnimationUtils.fadeIn;
import static io.ipoli.android.app.utils.AnimationUtils.fadeOut;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/31/17.
 */
public class TutorialNamePromptFragment extends Fragment {

    @BindView(R.id.tutorial_text)
    TypewriterView tutorialText;

    @BindView(R.id.tutorial_name_container)
    ViewGroup nameContainer;

    @BindView(R.id.tutorial_name)
    EditText name;

    @BindView(R.id.tutorial_ready)
    Button ready;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_name_prompt, container, false);
        unbinder = ButterKnife.bind(this, v);
        tutorialText.pause().type("First, let's get to know you a bit better. How should I call you?").run(() -> {
            fadeIn(nameContainer);
            fadeIn(ready);
        });
        return v;
    }

    @OnClick(R.id.tutorial_ready)
    public void onReadyClicked(View v) {
        onReady();
    }

    @OnEditorAction(R.id.tutorial_name)
    public boolean onDoneClicked(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onReady();
            return true;
        }

        return false;
    }

    private void onReady() {
        KeyboardUtils.hideKeyboard(getActivity());
        String nameText = name.getText().toString();
        if (nameText.length() < 2) {
            ObjectAnimator
                    .ofFloat(name, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                    .start();
            name.setError("Please, tell me your name");
            return;
        }
        fadeOut(nameContainer);
        fadeOut(ready);
        tutorialText.setText("");
        tutorialText.type("Welcome, " + nameText + "!").pause().run(() ->
                ((OnboardingActivity) getActivity()).onNamePromptDone(nameText));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}