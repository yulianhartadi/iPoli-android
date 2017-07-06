package io.ipoli.android.app.tutorial.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.tutorial.events.TutorialSkippedEvent;
import io.ipoli.android.app.ui.typewriter.TypewriterView;

import static io.ipoli.android.app.utils.AnimationUtils.fadeIn;
import static io.ipoli.android.app.utils.AnimationUtils.fadeOut;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/31/17.
 */
public class TutorialIntroFragment extends Fragment {

    @Inject
    Bus eventBus;

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
                .pause().type(getString(R.string.journey_start)).pause().clear()
                .type(getString(R.string.embrace_journey)).run(() -> {
            positiveAnswer.setText(R.string.i_accept);
            negativeAnswer.setText(R.string.nah_not_me);
            fadeIn(positiveAnswer);
            fadeIn(negativeAnswer);
            positiveAnswer.setOnClickListener(acceptChallengeListener);
            negativeAnswer.setOnClickListener(skipChallengeListener);
        });
    };

    private View.OnClickListener continueWithTutorialListener = v -> {
        prepareForNextState();
        tutorialText.type(getString(R.string.want_to_skip_tutorial)).run(() -> {
            positiveAnswer.setText(R.string.show_me);
            negativeAnswer.setText(R.string.no_skip_tutorial);
            fadeIn(positiveAnswer);
            fadeIn(negativeAnswer);
            positiveAnswer.setOnClickListener(improveListener);
            negativeAnswer.setOnClickListener(v1 -> onTutorialSkipped());
        });
    };

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View v = inflater.inflate(R.layout.fragment_tutorial_intro, container, false);
        unbinder = ButterKnife.bind(this, v);

        fadeIn(v.findViewById(R.id.tutorial_logo), android.R.integer.config_longAnimTime, 1000);

        tutorialText.pause(1000).type(getString(R.string.why_are_you_here)).run(() -> {
            positiveAnswer.setText(R.string.improve_myself);
            negativeAnswer.setText(R.string.no_idea);
            positiveAnswer.setOnClickListener(continueWithTutorialListener);
            negativeAnswer.setOnClickListener(v1 ->
                    Toast.makeText(getContext(), R.string.then_what_are_you_doing_here, Toast.LENGTH_LONG).show());
            fadeIn(positiveAnswer);
            fadeIn(negativeAnswer);
        });
        eventBus.post(new ScreenShownEvent(getActivity(), EventSource.TUTORIAL_INTRO));
        return v;
    }

    @Override
    public void onDestroyView() {
        tutorialText.stop();
        unbinder.unbind();
        super.onDestroyView();
    }

    @OnClick(R.id.tutorial_skip)
    public void onSkipTutorialClick(View view) {
        onTutorialSkipped();
    }

    private void onIntroDone() {
        if (getActivity() != null) {
            ((TutorialActivity) getActivity()).onIntroDone();
        }
    }

    private void onTutorialSkipped() {
        tutorialText.stop();
        eventBus.post(new TutorialSkippedEvent());
        if (getActivity() != null) {
            ((TutorialActivity) getActivity()).onTutorialSkipped();
        }
    }

    private void prepareForNextState() {
        fadeOut(positiveAnswer);
        fadeOut(negativeAnswer);
        tutorialText.setText("");
    }
}