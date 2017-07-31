package io.ipoli.android.achievement.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.achievement.ui.adapters.AchievementAdapter;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/28/17.
 */
public class AchievementListActivity extends BaseActivity {

    private String profileId;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    FeedPersistenceService feedPersistenceService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.achievement_list)
    RecyclerView achievementList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null) {
            profileId = getIntent().getStringExtra(Constants.PROFILE_ID_EXTRA_KEY);
        }

        achievementList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        achievementList.setHasFixedSize(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (StringUtils.isNotEmpty(profileId)) {
            feedPersistenceService.listenForProfile(profileId, profile ->
                    achievementList.setAdapter(new AchievementAdapter(profile.getAchievements().keySet())));
        } else {
            playerPersistenceService.listen(player ->
                    achievementList.setAdapter(new AchievementAdapter(player.getAchievements().keySet())));
        }
    }

    @Override
    protected void onStop() {
        feedPersistenceService.removeAllListeners();
        playerPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
