package io.ipoli.android.quest.activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.ColorRes;
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
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.subquests.SaveSubQuestsRequestEvent;
import io.ipoli.android.quest.fragments.SubQuestListFragment;
import io.ipoli.android.quest.fragments.TimerFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class QuestActivity extends BaseActivity {
    public static final String ACTION_QUEST_CANCELED = "io.ipoli.android.intent.action.QUEST_CANCELED";
    public static final String ACTION_START_QUEST = "io.ipoli.android.intent.action.START_QUEST";
    private static final int TIMER_TAB_POSITION = 0;
    private static final int SUB_QUESTS_TAB_POSITION = 1;

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || TextUtils.isEmpty(getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY))) {
            finish();
            return;
        }
        setContentView(R.layout.activity_quest);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);

        questPersistenceService.listenById(questId, quest -> {
            getSupportActionBar().setTitle(quest.getName());
            setBackgroundColors(Quest.getCategory(quest));
            eventBus.post(new ScreenShownEvent(EventSource.QUEST));
        });

        initViewPager(viewPager, questId);
        tabLayout.setupWithViewPager(viewPager);
        initTabIcons();
    }

    private void initTabIcons() {
        tabLayout.getTabAt(TIMER_TAB_POSITION).setIcon(R.drawable.ic_timer_white_24dp);
        tabLayout.getTabAt(SUB_QUESTS_TAB_POSITION).setIcon(R.drawable.ic_format_list_bulleted_white_24dp);
        colorNotSelectedTab(tabLayout.getTabAt(SUB_QUESTS_TAB_POSITION));

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

    private void initViewPager(ViewPager viewPager, String questId) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(TimerFragment.newInstance(questId));
        adapter.addFragment(SubQuestListFragment.newInstance(questId));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(TIMER_TAB_POSITION);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position != SUB_QUESTS_TAB_POSITION) {
                    eventBus.post(new SaveSubQuestsRequestEvent());
                }
                hideKeyboard();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setBackgroundColors(Category category) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color800));
        tabLayout.setBackgroundColor(ContextCompat.getColor(this, category.color800));
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        questPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Subscribe
    public void onLevelDown(LevelDownEvent e) {
        showLevelDownMessage(e.newLevel);
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