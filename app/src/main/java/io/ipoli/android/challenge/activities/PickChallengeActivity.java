package io.ipoli.android.challenge.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.challenge.adapters.PickChallengeAdapter;
import io.ipoli.android.challenge.data.PredefinedChallenge;
import io.ipoli.android.challenge.events.PersonalizeChallengeEvent;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/13/16.
 */
public class PickChallengeActivity extends BaseActivity {

    public static final String TITLE = "title";

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.challenge_view_pager)
    HorizontalInfiniteCycleViewPager viewPager;

    private boolean finishAfterChoosingChallenge = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pick_challenge);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            if (IntentUtils.hasExtra(getIntent(), TITLE)) {
                ab.setTitle(getIntent().getStringExtra(TITLE));
                finishAfterChoosingChallenge = true;
            }
        }

        List<PredefinedChallenge> challenges = App.getPredefinedChallenges();
        setBackgroundColors(challenges.get(0).challenge.getCategoryType());

        PickChallengeAdapter pickChallengeAdapter = new PickChallengeAdapter(this, challenges, eventBus);
        viewPager.setAdapter(pickChallengeAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                setBackgroundColors(challenges.get(viewPager.getRealItem()).challenge.getCategoryType());
            }
        });

        eventBus.post(new ScreenShownEvent(EventSource.PICK_CHALLENGE));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_help).setVisible(false);
        menu.findItem(R.id.action_pick_daily_challenge_quests).setVisible(false);
        return true;
    }

    private void setBackgroundColors(Category category) {
        rootContainer.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
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
    public void onPersonalizeChallenge(PersonalizeChallengeEvent e) {
        Intent intent = new Intent(this, PersonalizeChallengeActivity.class);
        intent.putExtra(Constants.PREDEFINED_CHALLENGE_INDEX, e.index);
        startActivity(intent);
        if (finishAfterChoosingChallenge) {
            supportFinishAfterTransition();
        }
    }
}
