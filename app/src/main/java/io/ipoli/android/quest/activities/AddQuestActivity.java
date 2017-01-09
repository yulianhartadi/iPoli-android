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
import io.ipoli.android.quest.events.NewQuestCategoryChangedEvent;
import io.ipoli.android.quest.events.NewQuestDatePickedEvent;
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

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Quest quest;

    private ViewPager fragmentPager;

    private AddQuestDateFragment dateFragment;
    private AddQuestTimeFragment timeFragment;
    private AddQuestNameFragment nameFragment;

    private Category category;

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

        fragmentPager = (ViewPager) findViewById(R.id.wizard_pager);
        WizardFragmentPagerAdapter adapterViewPager = new WizardFragmentPagerAdapter(getSupportFragmentManager());
        fragmentPager.setAdapter(adapterViewPager);
        fragmentPager.addOnPageChangeListener(this);
        category = Category.LEARNING;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_next:
                KeyboardUtils.hideKeyboard(this);
                fragmentPager.setCurrentItem(1, true);
                this.quest = new Quest(nameFragment.getName());
                this.quest.setCategoryType(category);
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
        this.category = e.category;
        colorLayout(e.category);
    }

    @Subscribe
    public void onNewQuestDatePicked(NewQuestDatePickedEvent e) {
        quest.setStartDate(e.start.toDate());
        quest.setEndDate(e.end.toDate());
        fragmentPager.postDelayed(() -> fragmentPager.setCurrentItem(fragmentPager.getCurrentItem() + 1),
                getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    @Subscribe
    public void onNewQuestTimePicked(NewQuestTimePickedEvent e) {
        quest.setStartTime(e.time);
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
            case 0:
                title = getString(R.string.title_activity_add_quest);
                break;
            case 1:
                title = "When will you do it?";
                dateFragment.setCategory(category);
                break;
            case 2:
                title = "At what time?";
                timeFragment.setCategory(category);
                break;
            case 3:
                title = "How important is it?";
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
                case 0:
                    return new AddQuestNameFragment();
                case 1:
                    return new AddQuestDateFragment();
                case 2:
                    return new AddQuestTimeFragment();
                case 3:
                    return new AddQuestPriorityFragment();
                default:
                    return new AddQuestSummaryFragment();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            if (position == 0) {
                nameFragment = (AddQuestNameFragment) createdFragment;
            } else if (position == 1) {
                dateFragment = (AddQuestDateFragment) createdFragment;
            } else if (position == 2) {
                timeFragment = (AddQuestTimeFragment) createdFragment;
            }
            return createdFragment;
        }
    }
}
