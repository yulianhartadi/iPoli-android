package io.ipoli.android.quest.activities;

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

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CategoryChangedEvent;
import io.ipoli.android.quest.events.NameAndCategoryPickedEvent;
import io.ipoli.android.quest.events.NewQuestPriorityPickedEvent;
import io.ipoli.android.quest.events.NewQuestTimePickedEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestRecurrencePickedEvent;
import io.ipoli.android.quest.fragments.AddNameFragment;
import io.ipoli.android.quest.fragments.AddQuestPriorityFragment;
import io.ipoli.android.quest.fragments.AddQuestSummaryFragment;
import io.ipoli.android.quest.fragments.AddQuestTimeFragment;
import io.ipoli.android.quest.fragments.AddRepeatingQuestRecurrenceFragment;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddRepeatingQuestActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final int REPEATING_QUEST_NAME_FRAGMENT_INDEX = 0;
    public static final int REPEATING_QUEST_FREQUENCY_FRAGMENT_INDEX = 1;
    public static final int REPEATING_QUEST_TIME_FRAGMENT_INDEX = 2;
    public static final int REPEATING_QUEST_PRIORITY_FRAGMENT_INDEX = 3;
    private static final int REPEATING_QUEST_SUMMARY_FRAGMENT_INDEX = 4;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.wizard_pager)
    ViewPager fragmentPager;

    private RepeatingQuest repeatingQuest;

    private AddRepeatingQuestRecurrenceFragment frequencyFragment;
    private AddQuestTimeFragment timeFragment;
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
        setTitle(R.string.title_fragment_wizard_repeating_quest_name);
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (fragmentPager.getCurrentItem() == REPEATING_QUEST_NAME_FRAGMENT_INDEX) {
                    finish();
                } else {
                    fragmentPager.setCurrentItem(fragmentPager.getCurrentItem() - 1);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onNewQuestCategoryChanged(CategoryChangedEvent e) {
        colorLayout(e.category);
    }

    @Subscribe
    public void onNewQuestNameAndCategoryPicked(NameAndCategoryPickedEvent e) {
        repeatingQuest = new RepeatingQuest(e.name);
        repeatingQuest.setName(e.name);
        repeatingQuest.addReminder(new Reminder(0, new Random().nextInt()));
        repeatingQuest.setCategoryType(e.category);
        KeyboardUtils.hideKeyboard(this);
        goToNextPage();
    }

    @Subscribe
    public void onNewRepeatingQuestRecurrencePicked(NewRepeatingQuestRecurrencePickedEvent e) {
        repeatingQuest.setRecurrence(e.recurrence);
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestTimePicked(NewQuestTimePickedEvent e) {
        repeatingQuest.setStartTime(e.time);
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestPriorityPicked(NewQuestPriorityPickedEvent e) {
        repeatingQuest.setPriority(e.priority);
        goToNextPage();
    }

    private void goToNextPage() {
        fragmentPager.postDelayed(() -> fragmentPager.setCurrentItem(fragmentPager.getCurrentItem() + 1),
                getResources().getInteger(android.R.integer.config_shortAnimTime));
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
            case REPEATING_QUEST_NAME_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_name);
                break;
            case REPEATING_QUEST_FREQUENCY_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_date);
                frequencyFragment.setCategory(repeatingQuest.getCategoryType());
                break;
            case REPEATING_QUEST_TIME_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_time);
                timeFragment.setCategory(repeatingQuest.getCategoryType());
                break;
            case REPEATING_QUEST_PRIORITY_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_priority);
                break;
            case REPEATING_QUEST_SUMMARY_FRAGMENT_INDEX:
                summaryFragment.setRepeatingQuest(repeatingQuest);
                break;
            default:
                title = "";
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
                case REPEATING_QUEST_NAME_FRAGMENT_INDEX:
                    return AddNameFragment.newInstance(R.string.add_quest_name_hint);
                case REPEATING_QUEST_FREQUENCY_FRAGMENT_INDEX:
                    return new AddRepeatingQuestRecurrenceFragment();
                case REPEATING_QUEST_TIME_FRAGMENT_INDEX:
                    return new AddQuestTimeFragment();
                case REPEATING_QUEST_PRIORITY_FRAGMENT_INDEX:
                    return new AddQuestPriorityFragment();
                default:
                    return new AddQuestSummaryFragment();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            if (position == REPEATING_QUEST_FREQUENCY_FRAGMENT_INDEX) {
                frequencyFragment = (AddRepeatingQuestRecurrenceFragment) createdFragment;
            } else if (position == REPEATING_QUEST_TIME_FRAGMENT_INDEX) {
                timeFragment = (AddQuestTimeFragment) createdFragment;
            } else if (position == REPEATING_QUEST_SUMMARY_FRAGMENT_INDEX)  {
                summaryFragment = (AddQuestSummaryFragment) createdFragment;
            }
            return createdFragment;
        }
    }
}
