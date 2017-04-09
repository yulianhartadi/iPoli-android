package io.ipoli.android.challenge.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.data.Difficulty;
import io.ipoli.android.challenge.events.ChangeChallengeEndDateRequestEvent;
import io.ipoli.android.challenge.events.ChangeChallengeExpectedResultsRequestEvent;
import io.ipoli.android.challenge.events.ChangeChallengeNameRequestEvent;
import io.ipoli.android.challenge.events.ChangeChallengeQuestsRequestEvent;
import io.ipoli.android.challenge.events.ChangeChallengeReasonsRequestEvent;
import io.ipoli.android.challenge.events.NewChallengeDifficultyPickedEvent;
import io.ipoli.android.challenge.ui.dialogs.DifficultyPickerFragment;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */
public class AddChallengeSummaryFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    private Unbinder unbinder;

    @BindView(R.id.add_challenge_summary_name)
    TextView name;

    @BindView(R.id.add_challenge_summary_result1)
    TextView result1;

    @BindView(R.id.add_challenge_summary_result2)
    TextView result2;

    @BindView(R.id.add_challenge_summary_result3)
    TextView result3;

    @BindView(R.id.add_challenge_summary_reason1)
    TextView reason1;

    @BindView(R.id.add_challenge_summary_reason2)
    TextView reason2;

    @BindView(R.id.add_challenge_summary_reason3)
    TextView reason3;

    @BindView(R.id.add_challenge_summary_difficulty)
    TextView difficultyText;

    @BindView(R.id.add_challenge_summary_date)
    TextView endDate;

    @BindView(R.id.add_challenge_summary_quests_container)
    ViewGroup questsContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_challenge_summary, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_challenge_wizard_summary_menu, menu);
    }


    @OnClick(R.id.add_challenge_summary_name)
    public void onNameClicked(View v) {
        postEvent(new ChangeChallengeNameRequestEvent());
    }

    @OnClick(R.id.add_challenges_summary_results)
    public void onExpectedResultsClicked(View v) {
        postEvent(new ChangeChallengeExpectedResultsRequestEvent());
    }

    @OnClick(R.id.add_challenges_summary_reasons)
    public void onReasonsClicked(View v) {
        postEvent(new ChangeChallengeReasonsRequestEvent());
    }

    @OnClick(R.id.add_challenge_summary_difficulty_container)
    public void onDifficultyClicked(View v) {
        DifficultyPickerFragment fragment = DifficultyPickerFragment.newInstance((Difficulty) difficultyText.getTag(), difficulty -> {
            postEvent(new NewChallengeDifficultyPickedEvent(difficulty));
            showDifficulty(difficulty);
        });
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.add_challenge_summary_date_container)
    public void onDateClicked(View v) {
        postEvent(new ChangeChallengeEndDateRequestEvent());
    }

    @OnClick(R.id.add_challenge_summary_quests)
    public void onQuestsClicked(View v) {
        postEvent(new ChangeChallengeQuestsRequestEvent());
    }

    public void setData(Challenge challenge, List<Quest> quests, List<RepeatingQuest> repeatingQuests) {
        name.setText(challenge.getName());
        showExpectedResults(challenge);
        showReasons(challenge);
        showDifficulty(Difficulty.getByValue(challenge.getDifficulty()));
        showEndDate(challenge);
        showQuests(quests, repeatingQuests);
    }

    private void showQuests(List<Quest> quests, List<RepeatingQuest> repeatingQuests) {
        questsContainer.removeAllViews();

        for(Quest q : quests) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.add_challenge_quest_item, questsContainer, false);
            populateQuestView(q.getName(), false, v);
            questsContainer.addView(v);

        }
        for(RepeatingQuest rq : repeatingQuests) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.add_challenge_quest_item, questsContainer, false);
            populateQuestView(rq.getName(), true, v);
            questsContainer.addView(v);
        }
    }

    private void populateQuestView(String name, boolean isRepeating, View view) {
        TextView nameView = (TextView) view.findViewById(R.id.quest_name);
        nameView.setText(name);
        view.findViewById(R.id.repeating_quest_indicator).setVisibility(isRepeating ? View.VISIBLE : View.GONE);
    }

    private void showReasons(Challenge challenge) {
        if (!StringUtils.isEmpty(challenge.getReason1())) {
            reason1.setText(challenge.getReason1());
            reason1.setVisibility(View.VISIBLE);
        }
        if (!StringUtils.isEmpty(challenge.getReason2())) {
            reason2.setText(challenge.getReason2());
            reason2.setVisibility(View.VISIBLE);
        }
        if (!StringUtils.isEmpty(challenge.getReason3())) {
            reason3.setText(challenge.getReason3());
            reason3.setVisibility(View.VISIBLE);
        }
    }

    private void showExpectedResults(Challenge challenge) {
        if (!StringUtils.isEmpty(challenge.getExpectedResult1())) {
            result1.setText(challenge.getExpectedResult1());
            result1.setVisibility(View.VISIBLE);
        }
        if (!StringUtils.isEmpty(challenge.getExpectedResult2())) {
            result2.setText(challenge.getExpectedResult2());
            result2.setVisibility(View.VISIBLE);
        }
        if (!StringUtils.isEmpty(challenge.getExpectedResult3())) {
            result3.setText(challenge.getExpectedResult3());
            result3.setVisibility(View.VISIBLE);
        }
    }

    private void showEndDate(Challenge challenge) {
        LocalDate byDate = challenge.getEndDate();
        String dayNumberSuffix = DateUtils.getDayNumberSuffix(byDate.getDayOfMonth());
        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.agenda_daily_journey_format, dayNumberSuffix));
        endDate.setText(getString(R.string.add_quest_by_date, dateFormat.format(DateUtils.toStartOfDay(byDate))));
    }

    private void showDifficulty(Difficulty difficulty) {
        difficultyText.setText(StringUtils.capitalize(difficulty.name()));
        difficultyText.setTag(difficulty);
    }
}
