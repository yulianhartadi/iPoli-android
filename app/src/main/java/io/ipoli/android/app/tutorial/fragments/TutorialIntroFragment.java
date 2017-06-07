package io.ipoli.android.app.tutorial.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.ui.typewriter.TypewriterView;

import static io.ipoli.android.app.utils.AnimationUtils.fadeIn;
import static io.ipoli.android.app.utils.AnimationUtils.fadeOut;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/31/17.
 */
public class TutorialIntroFragment extends Fragment {

    @BindView(R.id.tutorial_text)
    TypewriterView tutorialText;

    @BindView(R.id.tutorial_answer_positive)
    Button positiveAnswer;

    @BindView(R.id.tutorial_answer_negative)
    Button negativeAnswer;

    private View.OnClickListener showTipsListener = v -> onIntroDone();

    private View.OnClickListener showBackStoryListener = v -> {
        prepareForNextState();
        tutorialText.type(getString(R.string.daemon_intro))
                .pause(2000)
                .type(getString(R.string.daemon_objective)).pause().clear()
                .type(getString(R.string.daemon_backstory))
                .run(new Runnable() {
                    @Override
                    public void run() {
                        positiveAnswer.setText(R.string.ready_to_go);
                        fadeIn(positiveAnswer);
                        positiveAnswer.setOnClickListener(showTipsListener);
                    }
                });
    };

    private View.OnClickListener skipChallengeListener = v -> {
        prepareForNextState();
        tutorialText.type(getString(R.string.i_believe_in_you)).pause().run(new Runnable() {
            @Override
            public void run() {
                acceptChallengeListener.onClick(v);
            }
        });
    };

    private View.OnClickListener acceptChallengeListener = v -> {
        prepareForNextState();
        tutorialText.type(getString(R.string.you_the_one)).run(new Runnable() {
            @Override
            public void run() {
                positiveAnswer.setText(R.string.ready_to_go);
                negativeAnswer.setText(R.string.the_one);
                fadeIn(positiveAnswer);
                fadeIn(negativeAnswer);

                negativeAnswer.setOnClickListener(showBackStoryListener);
                positiveAnswer.setOnClickListener(showTipsListener);
            }
        });
    };

    private View.OnClickListener improveListener = v -> {
        prepareForNextState();
        tutorialText.type(getString(R.string.another_of_those)).pause().clear()
                .type(getString(R.string.lets_start_over)).pause().clear().type(getString(R.string.welcome_hero)).pause().clear()
                .pause().type(getString(R.string.journey_start)).pause().type(getString(R.string.journey_today)).pause().clear()
                .type(getString(R.string.embrace_journey)).run(() -> {
            positiveAnswer.setText(R.string.i_accept);
            negativeAnswer.setText(R.string.nah_not_me);
            fadeIn(positiveAnswer);
            fadeIn(negativeAnswer);
            positiveAnswer.setOnClickListener(acceptChallengeListener);
            negativeAnswer.setOnClickListener(skipChallengeListener);
        });
    };

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_intro, container, false);
        unbinder = ButterKnife.bind(this, v);

        fadeIn(v.findViewById(R.id.tutorial_logo), android.R.integer.config_longAnimTime, 1000);

        tutorialText.pause(1000).type(getString(R.string.why_are_you_here)).run(() -> {
            positiveAnswer.setText(R.string.improve_myself);
            negativeAnswer.setText(R.string.no_idea);
            positiveAnswer.setOnClickListener(improveListener);
            negativeAnswer.setOnClickListener(v1 ->
                    Toast.makeText(getContext(), R.string.then_what_are_you_doing_here, Toast.LENGTH_LONG).show());
            fadeIn(positiveAnswer);
            fadeIn(negativeAnswer);
        });
        return v;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @OnClick(R.id.tutorial_skip_section)
    public void onSkipSectionClick(View view) {
        tutorialText.stop();
        onIntroDone();
    }

    private void onIntroDone() {
        if (getActivity() != null) {
            ((TutorialActivity) getActivity()).onIntroDone();
        }
    }

    private void prepareForNextState() {
        fadeOut(positiveAnswer);
        fadeOut(negativeAnswer);
        tutorialText.setText("");
    }
}