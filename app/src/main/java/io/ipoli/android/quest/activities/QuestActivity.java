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
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.fragments.SubquestListFragment;
import io.ipoli.android.quest.fragments.TimerFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class QuestActivity extends BaseActivity {
    public static final String ACTION_QUEST_CANCELED = "io.ipoli.android.intent.action.QUEST_CANCELED";
    public static final String ACTION_START_QUEST = "io.ipoli.android.intent.action.START_QUEST";

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

    QuestPersistenceService questPersistenceService;
    private String questId;

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

        initViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        initTabIcons();
        viewPager.setCurrentItem(0);

        questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());
        questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        Quest quest = questPersistenceService.findById(questId);
        ab.setTitle(quest.getName());
        setBackgroundColors(Quest.getCategory(quest));
        eventBus.post(new ScreenShownEvent(EventSource.QUEST));
    }

    private void initTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_timer_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_format_list_bulleted_white_24dp);
        colorNotSelectedTab(tabLayout.getTabAt(1));

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
        adapter.addFragment(new TimerFragment());
        adapter.addFragment(new SubquestListFragment());
        viewPager.setAdapter(adapter);
    }

    private void setBackgroundColors(Category category) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.resDarkerColor));
        tabLayout.setBackgroundColor(ContextCompat.getColor(this, category.resDarkerColor));
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

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        Quest q = questPersistenceService.findById(questId);
        setBackgroundColors(Quest.getCategory(q));

    }

    @Subscribe
    public void onLevelDown(LevelDownEvent e) {
        showLevelDownMessage(e.newLevel);
    }

    @Override
    protected void onDestroy() {
        questPersistenceService.removeAllListeners();
        super.onDestroy();
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
