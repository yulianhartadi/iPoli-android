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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.ChangeQuestDateRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestNameRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestTimeRequestEvent;
import io.ipoli.android.quest.events.NewQuestCategoryChangedEvent;
import io.ipoli.android.quest.events.NewQuestDatePickedEvent;
import io.ipoli.android.quest.events.NewQuestDurationPickedEvent;
import io.ipoli.android.quest.events.NewQuestNameAndCategoryPickedEvent;
import io.ipoli.android.quest.events.NewQuestPriorityPickedEvent;
import io.ipoli.android.quest.events.NewQuestTimePickedEvent;
import io.ipoli.android.quest.fragments.AddQuestDateFragment;
import io.ipoli.android.quest.fragments.AddQuestNameFragment;
import io.ipoli.android.quest.fragments.AddQuestPriorityFragment;
import io.ipoli.android.quest.fragments.AddQuestSummaryFragment;
import io.ipoli.android.quest.fragments.AddQuestTimeFragment;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddQuestActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final int QUEST_NAME_FRAGMENT_INDEX = 0;
    public static final int QUEST_DATE_FRAGMENT_INDEX = 1;
    public static final int QUEST_TIME_FRAGMENT_INDEX = 2;
    public static final int QUEST_PRIORITY_FRAGMENT_INDEX = 3;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.wizard_pager)
    ViewPager fragmentPager;

    private Quest quest;

    private AddQuestDateFragment dateFragment;
    private AddQuestTimeFragment timeFragment;

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
        setTitle(R.string.title_fragment_wizard_quest_name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (fragmentPager.getCurrentItem() == QUEST_NAME_FRAGMENT_INDEX) {
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
    public void onNewQuestCategoryChanged(NewQuestCategoryChangedEvent e) {
        colorLayout(e.category);
    }

    @Subscribe
    public void onNewQuestNameAndCategoryPicked(NewQuestNameAndCategoryPickedEvent e) {
        quest = new Quest(e.name);
        quest.setCategoryType(e.category);
        KeyboardUtils.hideKeyboard(this);
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestDatePicked(NewQuestDatePickedEvent e) {
        quest.setStartDate(e.start.toDate());
        quest.setEndDate(e.end.toDate());
        goToNextPage();
    }

    private void goToNextPage() {
        fragmentPager.postDelayed(() -> fragmentPager.setCurrentItem(fragmentPager.getCurrentItem() + 1),
                getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    @Subscribe
    public void onNewQuestTimePicked(NewQuestTimePickedEvent e) {
        quest.setStartTime(e.time);
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestPriorityPicked(NewQuestPriorityPickedEvent e) {
        quest.setPriority(e.priority);
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestDurationPicked(NewQuestDurationPickedEvent e) {
        quest.setDuration(e.duration);
    }

    @Subscribe
    public void onChangeQuestNameRequest(ChangeQuestNameRequestEvent e) {
        fragmentPager.setCurrentItem(QUEST_NAME_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeDateRequest(ChangeQuestDateRequestEvent e) {
        fragmentPager.setCurrentItem(QUEST_DATE_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeTimeRequest(ChangeQuestTimeRequestEvent e) {
        fragmentPager.setCurrentItem(QUEST_TIME_FRAGMENT_INDEX);
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
            case QUEST_NAME_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_name);
                break;
            case QUEST_DATE_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_date);
                dateFragment.setCategory(quest.getCategoryType());
                break;
            case QUEST_TIME_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_time);
                timeFragment.setCategory(quest.getCategoryType());
                break;
            case QUEST_PRIORITY_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_priority);
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
                case QUEST_NAME_FRAGMENT_INDEX:
                    return new AddQuestNameFragment();
                case QUEST_DATE_FRAGMENT_INDEX:
                    return new AddQuestDateFragment();
                case QUEST_TIME_FRAGMENT_INDEX:
                    return new AddQuestTimeFragment();
                case QUEST_PRIORITY_FRAGMENT_INDEX:
                    return new AddQuestPriorityFragment();
                default:
                    return new AddQuestSummaryFragment();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            if (position == QUEST_DATE_FRAGMENT_INDEX) {
                dateFragment = (AddQuestDateFragment) createdFragment;
            } else if (position == QUEST_TIME_FRAGMENT_INDEX) {
                timeFragment = (AddQuestTimeFragment) createdFragment;
            }
            return createdFragment;
        }
    }
}
