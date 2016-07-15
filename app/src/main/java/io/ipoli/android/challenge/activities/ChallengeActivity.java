package io.ipoli.android.challenge.activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.fragments.ChallengeQuestListFragment;
import io.ipoli.android.challenge.fragments.ChallengeOverviewFragment;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.persistence.RealmChallengePersistenceService;
import io.ipoli.android.quest.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class ChallengeActivity extends BaseActivity {
    private static final int STATS_TAB_POSITION = 0;
    private static final int QUESTS_TAB_POSITION = 1;

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @Inject
    Bus eventBus;

    ChallengePersistenceService challengePersistenceService;
    private String challengeId;
    private Challenge challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || TextUtils.isEmpty(getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY))) {
            finish();
            return;
        }
        setContentView(R.layout.activity_challenge);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        challengeId = getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY);
        initViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        initTabIcons();

        challengePersistenceService = new RealmChallengePersistenceService(eventBus, getRealm());
        eventBus.post(new ScreenShownEvent(EventSource.CHALLENGE));
    }

    private void initTabIcons() {
        tabLayout.getTabAt(STATS_TAB_POSITION).setIcon(R.drawable.ic_bar_chart_white_24dp);
        tabLayout.getTabAt(QUESTS_TAB_POSITION).setIcon(R.drawable.ic_format_list_bulleted_white_24dp);
        colorNotSelectedTab(tabLayout.getTabAt(QUESTS_TAB_POSITION));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                colorSelectedTab(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                colorNotSelectedTab(tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void colorSelectedTab(TabLayout.Tab tab) {
        colorTab(tab, R.color.md_white);
    }

    private void colorNotSelectedTab(TabLayout.Tab tab) {
        colorTab(tab, R.color.md_light_text_54);
    }

    private void colorTab(TabLayout.Tab tab, @ColorRes int color) {
        int tabIconColor = ContextCompat.getColor(this, color);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    private void initViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ChallengeOverviewFragment.newInstance());
        adapter.addFragment(ChallengeQuestListFragment.newInstance(challengeId));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(STATS_TAB_POSITION);
    }

    private void setBackgroundColors(Category category) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        tabLayout.setBackgroundColor(ContextCompat.getColor(this, category.color500));

        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));

    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
        challenge = challengePersistenceService.findById(challengeId);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(challenge.getName());
        }
        setBackgroundColors(challenge.getCategory());
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    public Challenge getChallenge() {
        return challenge;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}