package io.ipoli.android.player.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.NumberFormat;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.ui.animations.ProgressBarAnimation;
import io.ipoli.android.app.ui.dialogs.DateTimePickerFragment;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.data.Profile;
import io.ipoli.android.feed.events.AddQuestFromPostEvent;
import io.ipoli.android.feed.events.DeletePostEvent;
import io.ipoli.android.feed.events.FollowPlayerEvent;
import io.ipoli.android.feed.events.GiveKudosEvent;
import io.ipoli.android.feed.events.ShowProfileEvent;
import io.ipoli.android.feed.events.UnfollowPlayerEvent;
import io.ipoli.android.feed.fragments.PostListFragment;
import io.ipoli.android.feed.fragments.ProfileListFragment;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.CredentialStatus;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.PlayerCredentialChecker;
import io.ipoli.android.player.PlayerCredentialsHandler;
import io.ipoli.android.player.events.AddKudosEvent;
import io.ipoli.android.player.events.CreateQuestFromPostEvent;
import io.ipoli.android.player.events.EditProfileEvent;
import io.ipoli.android.player.events.PlayerFollowedEvent;
import io.ipoli.android.player.events.PlayerUnfollowedEvent;
import io.ipoli.android.player.events.RemoveKudosEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.NewQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/27/17.
 */
public class ProfileActivity extends BaseActivity implements OnDataChangedListener<Profile> {

    @Inject
    FeedPersistenceService feedPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    PlayerCredentialsHandler playerCredentialsHandler;

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.player_avatar)
    ImageView playerAvatar;

    @BindView(R.id.pet_avatar)
    ImageView petAvatar;

    @BindView(R.id.pet_state)
    ImageView petState;

    @BindView(R.id.pet_name)
    TextView petName;

    @BindView(R.id.player_display_name)
    TextView playerDisplayName;

    @BindView(R.id.player_display_name_edit)
    EditText playerDisplayNameEdit;

    @BindView(R.id.player_username)
    TextView playerUsername;

    @BindView(R.id.player_level)
    TextView playerLevel;

    @BindView(R.id.player_bio)
    TextView playerBio;

    @BindView(R.id.player_bio_edit)
    EditText playerBioEdit;

    @BindView(R.id.player_experience)
    ProgressBar playerExperienceProgress;

    @BindView(R.id.xp_level_start)
    TextView xpLevelStart;

    @BindView(R.id.xp_level_end)
    TextView xpLevelEnd;

    @BindView(R.id.player_current_experience)
    TextView playerCurrentExperience;

    @BindView(R.id.follow)
    Button follow;

    @BindView(R.id.profile_tabs)
    TabLayout tabContainer;

    @BindView(R.id.profile_pager)
    ViewPager pagerContainer;

    private String profileId;

    private NumberFormat numberFormatter;

    private class TabPagerAdapter extends FragmentPagerAdapter {

        TabPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return PostListFragment.newInstance(profileId);
                case 1:
                    return ProfileListFragment.newInstance(profileId, ProfileListFragment.LIST_TYPE_FOLLOWING);
                default:
                    return ProfileListFragment.newInstance(profileId, ProfileListFragment.LIST_TYPE_FOLLOWERS);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        profileId = getIntent().getStringExtra(Constants.PLAYER_ID_EXTRA_KEY);

        if (StringUtils.isEmpty(profileId)) {
            throw new IllegalArgumentException("Player id is required");
        }

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitleEnabled(false);
        getSupportActionBar().setTitle(R.string.activity_player_profile_title);

        tabContainer.setupWithViewPager(pagerContainer);

        TabPagerAdapter fragmentPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        pagerContainer.setAdapter(fragmentPagerAdapter);

        numberFormatter = NumberFormat.getNumberInstance();

        tabContainer.getTabAt(0).setCustomView(getLayoutInflater().inflate(R.layout.view_profile_tab, null));
        tabContainer.getTabAt(1).setCustomView(getLayoutInflater().inflate(R.layout.view_profile_tab, null));
        tabContainer.getTabAt(2).setCustomView(getLayoutInflater().inflate(R.layout.view_profile_tab, null));

        eventBus.post(new ScreenShownEvent(this, EventSource.PROFILE));
    }

    @Override
    public void onDataChanged(Profile profile) {
        playerAvatar.setImageResource(profile.getPlayerAvatar().picture);
        petAvatar.setImageResource(profile.getPetAvatar().headPicture);
        petState.setVisibility(View.VISIBLE);
        GradientDrawable drawable = (GradientDrawable) petState.getBackground();
        drawable.setColor(ContextCompat.getColor(this, Pet.PetState.valueOf(profile.getPetState()).color));
        petName.setText(profile.getPetName());
        playerDisplayName.setText(profile.getDisplayName());
        playerUsername.setText("@" + profile.getUsername());
        String description = StringUtils.isEmpty(profile.getBio()) ? getString(R.string.profile_default_bio) : profile.getBio();
        playerBio.setText(description);

        String[] playerTitles = getResources().getStringArray(R.array.player_titles);
        String playerTitle = playerTitles[Math.min(profile.getLevel() / 10, playerTitles.length - 1)];
        playerLevel.setText(getString(R.string.player_profile_level, profile.getLevel(), playerTitle));

        View postsTabView = tabContainer.getTabAt(0).getCustomView();
        bindTabView(postsTabView, profile.getPosts().size(), R.string.posts);

        View followingTabView = tabContainer.getTabAt(1).getCustomView();
        bindTabView(followingTabView, profile.getFollowing().size(), R.string.following);

        View followersTabView = tabContainer.getTabAt(2).getCustomView();
        bindTabView(followersTabView, profile.getFollowers().size(), R.string.followers);

        updateExperienceProgress(profile);
        updateFollow(profile);
    }

    public void bindTabView(View tabView, int count, int labelRes) {
        TextView itemCount = (TextView) tabView.findViewById(R.id.item_count);
        TextView itemLabel = (TextView) tabView.findViewById(R.id.item_label);
        itemCount.setText(String.valueOf(count));
        itemLabel.setText(labelRes);
    }

    private void updateFollow(Profile profile) {
        String playerId = getPlayerId();
        if (profile.getId().equals(playerId)) {
            follow.setVisibility(View.GONE);
            return;
        }
        follow.setVisibility(View.VISIBLE);
        boolean following = profile.isFollowedBy(playerId);
        if (following) {
            follow.setText(R.string.following);
        } else {
            follow.setText(R.string.follow);
        }

        follow.setOnClickListener(null);
        follow.setOnClickListener(v ->
                onFollowPlayer(profileId, getPlayerId(), following));
    }

    public void onFollowPlayer(String profileId, String playerId, boolean following) {
        if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.enable_internet_to_do_action, Toast.LENGTH_LONG).show();
            return;
        }

        Player player = getPlayer();
        CredentialStatus credentialStatus = PlayerCredentialChecker.checkStatus(player);
        if (credentialStatus != CredentialStatus.AUTHORIZED) {
            playerCredentialsHandler.authorizeAccess(player, credentialStatus, PlayerCredentialsHandler.Action.FOLLOW_PLAYER,
                    this, rootContainer);
            return;
        }

        if (following) {
            feedPersistenceService.unfollow(profileId, playerId);
            eventBus.post(new PlayerUnfollowedEvent(playerId, profileId));
        } else {
            feedPersistenceService.follow(profileId, playerId);
            eventBus.post(new PlayerFollowedEvent(playerId, profileId));
        }
    }

    private void updateExperienceProgress(Profile profile) {
        int currentLevel = profile.getLevel();
        BigInteger requiredXPForCurrentLevel = ExperienceForLevelGenerator.forLevel(currentLevel);
        BigInteger requiredXPForNextLevel = ExperienceForLevelGenerator.forLevel(currentLevel + 1);
        BigDecimal xpForNextLevel = new BigDecimal(requiredXPForNextLevel.subtract(requiredXPForCurrentLevel));
        BigDecimal currentXP = new BigDecimal(new BigInteger(profile.getExperience()).subtract(requiredXPForCurrentLevel));
        int levelProgress = (int) (currentXP.divide(xpForNextLevel, 2, RoundingMode.HALF_UP).doubleValue() * Constants.XP_BAR_MAX_VALUE);
        xpLevelStart.setText(numberFormatter.format(requiredXPForCurrentLevel));
        xpLevelEnd.setText(numberFormatter.format(requiredXPForNextLevel));
        playerCurrentExperience.setText(getString(R.string.player_profile_current_xp, numberFormatter.format(new BigInteger(profile.getExperience()))));
        playerExperienceProgress.setMax(Constants.XP_BAR_MAX_VALUE);

        if (playerExperienceProgress.getTag() == null) {
            playerExperienceProgress.setTag(true);
            ProgressBarAnimation anim = new ProgressBarAnimation(playerExperienceProgress, 0, levelProgress);
            anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            playerExperienceProgress.startAnimation(anim);
        } else {
            playerExperienceProgress.setProgress(levelProgress);
        }

    }

    @Subscribe
    public void onAddQuest(AddQuestFromPostEvent event) {
        if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.enable_internet_to_do_action, Toast.LENGTH_LONG).show();
            return;
        }

        Player player = getPlayer();
        CredentialStatus credentialStatus = PlayerCredentialChecker.checkStatus(player);
        if (credentialStatus != CredentialStatus.AUTHORIZED) {
            playerCredentialsHandler.authorizeAccess(player, credentialStatus, PlayerCredentialsHandler.Action.ADD_QUEST,
                    this, rootContainer);
            return;
        }

        Post post = event.post;

        if (!post.isAddedByPlayer(player.getId())) {
            feedPersistenceService.addPostToPlayer(post, player.getId());
        }
        DateTimePickerFragment.newInstance(player.getUse24HourFormat(), (date, time) -> {
            Quest quest = new Quest(post.getTitle(), date, post.getCategoryType());
            quest.setDuration(post.getDuration());
            quest.setStartTime(time);
            eventBus.post(new NewQuestEvent(quest, EventSource.PROFILE));
            Toast.makeText(this, R.string.quest_from_post_added, Toast.LENGTH_LONG).show();
        }).show(getSupportFragmentManager());
        eventBus.post(new CreateQuestFromPostEvent(post, EventSource.PROFILE));
    }

    @Subscribe
    public void onGiveKudos(GiveKudosEvent event) {
        if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.enable_internet_to_do_action, Toast.LENGTH_LONG).show();
            return;
        }

        Player player = getPlayer();
        CredentialStatus credentialStatus = PlayerCredentialChecker.checkStatus(player);
        if (credentialStatus != CredentialStatus.AUTHORIZED) {
            playerCredentialsHandler.authorizeAccess(player, credentialStatus, PlayerCredentialsHandler.Action.GIVE_KUDOS,
                    this, rootContainer);
            return;
        }

        Post post = event.post;

        if (post.isGivenKudosByPlayer(player.getId())) {
            feedPersistenceService.removeKudos(post, player.getId());
            eventBus.post(new RemoveKudosEvent(post, EventSource.PROFILE));
        } else {
            feedPersistenceService.addKudos(post, player.getId());
            eventBus.post(new AddKudosEvent(post, EventSource.PROFILE));
        }
    }

    @Subscribe
    public void onShowProfile(ShowProfileEvent event) {
        if (profileId.equals(event.playerId)) {
            return;
        }
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(Constants.PLAYER_ID_EXTRA_KEY, event.playerId);
        startActivity(intent);
    }

    @Subscribe
    public void onFollowPlayer(FollowPlayerEvent event) {
        String playerId = getPlayerId();
        feedPersistenceService.follow(event.profileId, playerId);
        eventBus.post(new PlayerFollowedEvent(playerId, event.profileId));
    }

    @Subscribe
    public void onUnfollowPlayer(UnfollowPlayerEvent event) {
        String playerId = getPlayerId();
        feedPersistenceService.unfollow(event.profileId, playerId);
        eventBus.post(new PlayerUnfollowedEvent(playerId, event.profileId));
    }

    @Subscribe
    public void onDeletePost(DeletePostEvent e) {
        feedPersistenceService.deletePost(e.post);
    }

    @Override
    public void onResume() {
        super.onResume();
        feedPersistenceService.listenForProfile(profileId, this);
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        feedPersistenceService.removeAllListeners();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        MenuItem editItem = menu.findItem(R.id.action_edit);
        if (!profileId.equals(getPlayerId())) {
            editItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_edit:
                Player player = getPlayer();
                if (isInEditMode()) {
                    playerBioEdit.setTag(true);
                    playerBio.setVisibility(View.INVISIBLE);

                    playerBioEdit.setText(StringUtils.isEmpty(player.getBio()) ? "" : player.getBio());
                    playerBioEdit.setSelection(StringUtils.isEmpty(player.getBio()) ? 0 : playerBio.getText().length());
                    playerBioEdit.setVisibility(View.VISIBLE);

                    playerDisplayName.setVisibility(View.INVISIBLE);
                    playerDisplayNameEdit.setVisibility(View.VISIBLE);
                    playerDisplayNameEdit.setText(playerDisplayName.getText());

                    item.setTitle(R.string.save);
                    playerDisplayNameEdit.requestFocus();
                    KeyboardUtils.showKeyboard(this, playerDisplayNameEdit);
                    eventBus.post(new EditProfileEvent(profileId));
                } else {

                    String newDisplayName = playerDisplayNameEdit.getText().toString();
                    if (StringUtils.isEmpty(newDisplayName)) {
                        Toast.makeText(this, "Your name can't be empty, right?", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    playerBioEdit.setTag(null);
                    item.setTitle(R.string.edit_menu);
                    playerBio.setVisibility(View.VISIBLE);
                    playerBioEdit.setVisibility(View.GONE);
                    playerDisplayName.setVisibility(View.VISIBLE);
                    playerDisplayNameEdit.setVisibility(View.GONE);
                    String newBio = playerBioEdit.getText().toString();

                    if (StringUtils.isEmpty(newBio)) {
                        playerBio.setText(R.string.profile_default_bio);
                    } else {
                        playerBio.setText(newBio.trim());
                    }
                    playerDisplayName.setText(newDisplayName);
                    KeyboardUtils.hideKeyboard(this);
                    player.setDisplayName(newDisplayName);
                    player.setBio(newBio);
                    playerPersistenceService.save(player);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isInEditMode() {
        return playerBioEdit.getTag() == null;
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }
}