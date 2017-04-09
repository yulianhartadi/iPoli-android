package io.ipoli.android.challenge.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.data.Difficulty;
import io.ipoli.android.challenge.events.ChangeChallengeEndDateRequestEvent;
import io.ipoli.android.challenge.events.ChangeChallengeExpectedResultsRequestEvent;
import io.ipoli.android.challenge.events.ChangeChallengeNameRequestEvent;
import io.ipoli.android.challenge.events.ChangeChallengeQuestsRequestEvent;
import io.ipoli.android.challenge.events.ChangeChallengeReasonsRequestEvent;
import io.ipoli.android.challenge.events.NewChallengeDifficultyPickedEvent;
import io.ipoli.android.challenge.events.NewChallengeEndDatePickedEvent;
import io.ipoli.android.challenge.events.NewChallengeQuestsPickedEvent;
import io.ipoli.android.challenge.events.NewChallengeReasonsPickedEvent;
import io.ipoli.android.challenge.events.NewChallengeResultsPickedEvent;
import io.ipoli.android.challenge.fragments.AddChallengeEndDateFragment;
import io.ipoli.android.challenge.fragments.AddChallengeQuestsFragment;
import io.ipoli.android.challenge.fragments.AddChallengeReasonsFragment;
import io.ipoli.android.challenge.fragments.AddChallengeResultsFragment;
import io.ipoli.android.challenge.fragments.AddChallengeSummaryFragment;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CategoryChangedEvent;
import io.ipoli.android.quest.events.NameAndCategoryPickedEvent;
import io.ipoli.android.quest.fragments.AddNameFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddChallengeActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final int CHALLENGE_NAME_FRAGMENT_INDEX = 0;
    public static final int CHALLENGE_RESULTS_FRAGMENT_INDEX = 1;
    public static final int CHALLENGE_REASONS_FRAGMENT_INDEX = 2;
    public static final int CHALLENGE_END_FRAGMENT_INDEX = 3;
    public static final int CHALLENGE_QUESTS_FRAGMENT_INDEX = 4;
    private static final int CHALLENGE_SUMMARY_FRAGMENT_INDEX = 5;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.wizard_pager)
    ViewPager fragmentPager;

    private Challenge challenge;
    private List<Quest> quests = new ArrayList<>();
    private List<RepeatingQuest> repeatingQuests = new ArrayList<>();

    private AddChallengeResultsFragment resultsFragment;
    private AddChallengeReasonsFragment reasonsFragment;
    private AddChallengeQuestsFragment questsFragment;
    private AddChallengeEndDateFragment endDateFragment;
    private AddChallengeSummaryFragment summaryFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
        }

        WizardFragmentPagerAdapter adapterViewPager = new WizardFragmentPagerAdapter(getSupportFragmentManager());
        fragmentPager.setAdapter(adapterViewPager);
        fragmentPager.addOnPageChangeListener(this);
        setTitle(R.string.title_fragment_wizard_challenge_name);
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (fragmentPager.getCurrentItem() == CHALLENGE_NAME_FRAGMENT_INDEX) {
                    finish();
                } else {
                    fragmentPager.setCurrentItem(fragmentPager.getCurrentItem() - 1);
                }
                return true;
            case R.id.action_save:
                onSaveChallenge();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveChallenge() {
        KeyboardUtils.hideKeyboard(this);
        challengePersistenceService.save(challenge);
        String challengeId = challenge.getId();
        for(Quest q : quests) {
            q.setChallengeId(challengeId);
        }
        for(RepeatingQuest rq : repeatingQuests) {
            rq.setChallengeId(challengeId);
        }

        if(!quests.isEmpty()) {
            questPersistenceService.save(quests);
        }
        if(!repeatingQuests.isEmpty()) {
            repeatingQuestPersistenceService.addToChallenge(repeatingQuests, challengeId);
        }

        Toast.makeText(this, R.string.challenge_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onCategoryChanged(CategoryChangedEvent e) {
        colorLayout(e.category);
    }

    @Subscribe
    public void onNameAndCategoryPicked(NameAndCategoryPickedEvent e) {
        challenge = new Challenge(e.name);
        challenge.setCategoryType(e.category);
        challenge.setDifficultyType(Difficulty.NORMAL);
        KeyboardUtils.hideKeyboard(this);
        goToNextPage();
    }

    @Subscribe
    public void onNewChallengeResultsPicked(NewChallengeResultsPickedEvent e) {
        challenge.setExpectedResult1(e.result1);
        challenge.setExpectedResult2(e.result2);
        challenge.setExpectedResult3(e.result3);
        goToNextPage();
    }

    @Subscribe
    public void onNewChallengeReasonsPicked(NewChallengeReasonsPickedEvent e) {
        challenge.setReason1(e.reason1);
        challenge.setReason2(e.reason2);
        challenge.setReason3(e.reason3);
        goToNextPage();
    }

    @Subscribe
    public void onNewChallengeEndDatePicked(NewChallengeEndDatePickedEvent e) {
        challenge.setEndDate(e.date);
        goToNextPage();
    }

    @Subscribe
    public void onNewChallengeQuestsPicked(NewChallengeQuestsPickedEvent e) {
        quests = e.quests;
        repeatingQuests = e.repeatingQuests;
        goToNextPage();
    }

    @Subscribe
    public void onNewChallengeDifficultyPicked(NewChallengeDifficultyPickedEvent e) {
        challenge.setDifficultyType(e.difficulty);
    }

    private void goToNextPage() {
        fragmentPager.postDelayed(() -> fragmentPager.setCurrentItem(fragmentPager.getCurrentItem() + 1),
                getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    @Subscribe
    public void onChangeChallengeNameRequest(ChangeChallengeNameRequestEvent e) {
        fragmentPager.setCurrentItem(CHALLENGE_NAME_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeExpectedResultsRequest(ChangeChallengeExpectedResultsRequestEvent e) {
        fragmentPager.setCurrentItem(CHALLENGE_RESULTS_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeReasonsRequest(ChangeChallengeReasonsRequestEvent e) {
        fragmentPager.setCurrentItem(CHALLENGE_REASONS_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeEndDateRequest(ChangeChallengeEndDateRequestEvent e) {
        fragmentPager.setCurrentItem(CHALLENGE_END_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeQuestsRequest(ChangeChallengeQuestsRequestEvent e) {
        fragmentPager.setCurrentItem(CHALLENGE_QUESTS_FRAGMENT_INDEX);
    }

    private void colorLayout(Category category) {
        toolbar.setBackgroundResource(category.color500);
        findViewById(R.id.root_container).setBackgroundResource(category.color500);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        String title = "";
        switch (position) {
            case CHALLENGE_NAME_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_challenge_name);
                break;
            case CHALLENGE_RESULTS_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_challenge_results);
                resultsFragment.setCategory(challenge.getCategoryType());
                break;
            case CHALLENGE_REASONS_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_challenge_reasons);
                reasonsFragment.setCategory(challenge.getCategoryType());
                break;
            case CHALLENGE_END_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_challenge_end_date);
                endDateFragment.setCategory(challenge.getCategoryType());
                break;
            case CHALLENGE_QUESTS_FRAGMENT_INDEX:
                title = getString(R.string.title_pick_challenge_quests_activity);
                questsFragment.setSelectedQuests(quests, repeatingQuests);
                break;
            case CHALLENGE_SUMMARY_FRAGMENT_INDEX:
                summaryFragment.setData(challenge, quests, repeatingQuests);
                break;
        }
        setTitle(title);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class WizardFragmentPagerAdapter extends FragmentPagerAdapter {

        WizardFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case CHALLENGE_NAME_FRAGMENT_INDEX:
                    return AddNameFragment.newInstance(R.string.add_challenge_name_hint);
                case CHALLENGE_RESULTS_FRAGMENT_INDEX:
                    return new AddChallengeResultsFragment();
                case CHALLENGE_REASONS_FRAGMENT_INDEX:
                    return new AddChallengeReasonsFragment();
                case CHALLENGE_END_FRAGMENT_INDEX:
                    return new AddChallengeEndDateFragment();
                case CHALLENGE_QUESTS_FRAGMENT_INDEX:
                    return new AddChallengeQuestsFragment();
                default:
                    return new AddChallengeSummaryFragment();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            if (position == CHALLENGE_RESULTS_FRAGMENT_INDEX) {
                resultsFragment = (AddChallengeResultsFragment) createdFragment;
            } else if (position == CHALLENGE_REASONS_FRAGMENT_INDEX) {
                reasonsFragment = (AddChallengeReasonsFragment) createdFragment;
            } else if (position == CHALLENGE_END_FRAGMENT_INDEX) {
                endDateFragment = (AddChallengeEndDateFragment) createdFragment;
            } else if (position == CHALLENGE_QUESTS_FRAGMENT_INDEX) {
                questsFragment = (AddChallengeQuestsFragment) createdFragment;
            } else if (position == CHALLENGE_SUMMARY_FRAGMENT_INDEX) {
                summaryFragment = (AddChallengeSummaryFragment) createdFragment;
            }
            return createdFragment;
        }
    }
}
