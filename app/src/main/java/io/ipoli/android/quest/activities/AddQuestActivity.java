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
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CategoryChangedEvent;
import io.ipoli.android.quest.events.ChangeQuestDateRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestNameRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestPriorityRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestTimeRequestEvent;
import io.ipoli.android.quest.events.NameAndCategoryPickedEvent;
import io.ipoli.android.quest.events.NewQuestChallengePickedEvent;
import io.ipoli.android.quest.events.NewQuestDatePickedEvent;
import io.ipoli.android.quest.events.NewQuestDurationPickedEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewQuestNotePickedEvent;
import io.ipoli.android.quest.events.NewQuestPriorityPickedEvent;
import io.ipoli.android.quest.events.NewQuestRemindersPickedEvent;
import io.ipoli.android.quest.events.NewQuestSubQuestsPickedEvent;
import io.ipoli.android.quest.events.NewQuestTimePickedEvent;
import io.ipoli.android.quest.events.NewQuestTimesADayPickedEvent;
import io.ipoli.android.quest.fragments.AddNameFragment;
import io.ipoli.android.quest.fragments.AddQuestDateFragment;
import io.ipoli.android.quest.fragments.AddQuestPriorityFragment;
import io.ipoli.android.quest.fragments.AddQuestSummaryFragment;
import io.ipoli.android.quest.fragments.AddQuestTimeFragment;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddQuestActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final int QUEST_NAME_FRAGMENT_INDEX = 0;
    public static final int QUEST_DATE_FRAGMENT_INDEX = 1;
    public static final int QUEST_TIME_FRAGMENT_INDEX = 2;
    public static final int QUEST_PRIORITY_FRAGMENT_INDEX = 3;
    private static final int QUEST_SUMMARY_FRAGMENT_INDEX = 4;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.wizard_pager)
    ViewPager fragmentPager;

    private Quest quest;

    private AddQuestDateFragment dateFragment;
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
        setTitle(R.string.title_fragment_wizard_quest_name);
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
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
            case R.id.action_save:
                onSaveQuest();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveQuest() {
        KeyboardUtils.hideKeyboard(this);
        eventBus.post(new NewQuestEvent(quest, EventSource.ADD_QUEST));
        if (quest.getScheduled() != null) {
            Toast.makeText(this, R.string.quest_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.quest_moved_to_inbox, Toast.LENGTH_SHORT).show();
        }
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
        quest = new Quest(e.name);
        quest.setDuration(Constants.QUEST_MIN_DURATION);
        quest.addReminder(new Reminder(0));
        quest.setCategoryType(e.category);
        KeyboardUtils.hideKeyboard(this);
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestDatePicked(NewQuestDatePickedEvent e) {
        if (e.end != null && e.start != null) {
            quest.setStartDate(e.start);
            quest.setEndDate(e.end);
        }
        goToNextPage();
    }

    private void goToNextPage() {
        fragmentPager.postDelayed(() -> fragmentPager.setCurrentItem(fragmentPager.getCurrentItem() + 1),
                getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    @Subscribe
    public void onNewQuestTimePicked(NewQuestTimePickedEvent e) {
        quest.setStartTimePreference(e.timePreference);
        quest.setStartTime(e.time);
        if(e.time != null) {
            quest.setTimesADay(1);
        }
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
    public void onNewQuestRemindersPicked(NewQuestRemindersPickedEvent e) {
        quest.setReminders(e.reminders);
    }

    @Subscribe
    public void onNewQuestTimesADayPicked(NewQuestTimesADayPickedEvent e) {
        quest.setTimesADay(e.timesADay);
    }

    @Subscribe
    public void onNewQuestSubQuestsPicked(NewQuestSubQuestsPickedEvent e) {
        quest.setSubQuests(e.subQuests);
    }

    @Subscribe
    public void onNewQuestChallengePicked(NewQuestChallengePickedEvent e) {
        quest.setChallengeId(e.challenge == null ? null : e.challenge.getId());
    }

    @Subscribe
    public void onNewQuestNotePicked(NewQuestNotePickedEvent e) {
        List<Note> notes = new ArrayList<>();
        String txt = e.text;
        if (!StringUtils.isEmpty(txt)) {
            notes.add(new Note(txt));
        }
        quest.setNotes(notes);
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
            case QUEST_SUMMARY_FRAGMENT_INDEX:
                summaryFragment.setQuest(quest);
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
                case QUEST_NAME_FRAGMENT_INDEX:
                    return AddNameFragment.newInstance(R.string.add_quest_name_hint);
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
            } else if (position == QUEST_SUMMARY_FRAGMENT_INDEX) {
                summaryFragment = (AddQuestSummaryFragment) createdFragment;
            }
            return createdFragment;
        }
    }
}
