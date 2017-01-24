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

import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.events.NewChallengeReasonsPickedEvent;
import io.ipoli.android.challenge.events.NewChallengeResultsPickedEvent;
import io.ipoli.android.challenge.fragments.AddChallengeReasonsFragment;
import io.ipoli.android.challenge.fragments.AddChallengeResultsFragment;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.events.CategoryChangedEvent;
import io.ipoli.android.quest.events.ChangeQuestDateRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestNameRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestPriorityRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestTimeRequestEvent;
import io.ipoli.android.quest.events.NameAndCategoryPickedEvent;
import io.ipoli.android.quest.fragments.AddNameFragment;
import io.ipoli.android.quest.fragments.AddQuestPriorityFragment;
import io.ipoli.android.quest.fragments.AddQuestSummaryFragment;

import static io.ipoli.android.quest.activities.AddQuestActivity.QUEST_PRIORITY_FRAGMENT_INDEX;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddChallengeActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final int CHALLENGE_NAME_FRAGMENT_INDEX = 0;
    public static final int CHALLENGE_RESULTS_FRAGMENT_INDEX = 1;
    public static final int CHALLENGE_REASONS_FRAGMENT_INDEX = 2;
    public static final int CHALLENGE_QUESTS_FRAGMENT_INDEX = 3;
    private static final int CHALLENGE_SUMMARY_FRAGMENT_INDEX = 4;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.wizard_pager)
    ViewPager fragmentPager;

    private Challenge challenge;

    private AddChallengeResultsFragment resultsFragment;
    private AddChallengeReasonsFragment reasonsFragment;
    private AddQuestSummaryFragment summaryFragment;

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
    protected boolean useOptionsMenu() {
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
//        eventBus.post(new NewQuestEvent(challenge, EventSource.ADD_QUEST));
//        if (challenge.getScheduled() != null) {
//            Toast.makeText(this, R.string.quest_saved, Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, R.string.quest_moved_to_inbox, Toast.LENGTH_SHORT).show();
//        }
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

    private void goToNextPage() {
        fragmentPager.postDelayed(() -> fragmentPager.setCurrentItem(fragmentPager.getCurrentItem() + 1),
                getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    @Subscribe
    public void onChangeQuestNameRequest(ChangeQuestNameRequestEvent e) {
        fragmentPager.setCurrentItem(CHALLENGE_NAME_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeDateRequest(ChangeQuestDateRequestEvent e) {
        fragmentPager.setCurrentItem(CHALLENGE_RESULTS_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeTimeRequest(ChangeQuestTimeRequestEvent e) {
        fragmentPager.setCurrentItem(CHALLENGE_REASONS_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangePriorityRequest(ChangeQuestPriorityRequestEvent e) {
        fragmentPager.setCurrentItem(QUEST_PRIORITY_FRAGMENT_INDEX);
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
            case CHALLENGE_SUMMARY_FRAGMENT_INDEX:
//                summaryFragment.setQuest(challenge);
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
            return 5;
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
                case CHALLENGE_QUESTS_FRAGMENT_INDEX:
                    return new AddQuestPriorityFragment();
                default:
                    return new AddQuestSummaryFragment();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            if (position == CHALLENGE_RESULTS_FRAGMENT_INDEX) {
                resultsFragment = (AddChallengeResultsFragment) createdFragment;
            } else if (position == CHALLENGE_REASONS_FRAGMENT_INDEX) {
                reasonsFragment = (AddChallengeReasonsFragment) createdFragment;
            } else if (position == CHALLENGE_SUMMARY_FRAGMENT_INDEX) {
                summaryFragment = (AddQuestSummaryFragment) createdFragment;
            }
            return createdFragment;
        }
    }
}
