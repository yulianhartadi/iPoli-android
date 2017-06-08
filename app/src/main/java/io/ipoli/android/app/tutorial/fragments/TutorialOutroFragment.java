package io.ipoli.android.app.tutorial.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import io.ipoli.android.app.ui.typewriter.TypewriterView;

import static io.ipoli.android.app.utils.AnimationUtils.fadeIn;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
public class TutorialOutroFragment extends Fragment {

    @Inject
    Bus eventBus;

    @BindView(R.id.tutorial_text)
    TypewriterView tutorialText;

    private Unbinder unbinder;

    private String playerName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View v = inflater.inflate(R.layout.fragment_tutorial_outro, container, false);
        unbinder = ButterKnife.bind(this, v);

        fadeIn(v.findViewById(R.id.tutorial_logo), android.R.integer.config_longAnimTime, 1000);
        fadeIn(v.findViewById(R.id.tutorial_accept), 6000);
        tutorialText.pause(1000).type(getString(R.string.outro_text, playerName));
        eventBus.post(new ScreenShownEvent(getActivity(), EventSource.TUTORIAL_OUTRO));
        return v;
    }

    @OnClick(R.id.tutorial_accept)
    public void onAcceptChallengeClick(View v) {
        if (getActivity() != null) {
            ((TutorialActivity) getActivity()).onTutorialDone();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
