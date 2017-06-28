package io.ipoli.android.player.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
import io.ipoli.android.app.activities.SignInActivity;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.ui.animations.ProgressBarAnimation;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.data.PlayerProfile;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.feed.ui.PostBinder;
import io.ipoli.android.feed.ui.PostViewHolder;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/27/17.
 */
public class PlayerProfileActivity extends BaseActivity implements OnDataChangedListener<PlayerProfile> {

    @Inject
    FeedPersistenceService feedPersistenceService;

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

    @BindView(R.id.player_name)
    TextView playerName;

    @BindView(R.id.player_username)
    TextView playerUsername;

    @BindView(R.id.player_level)
    TextView playerLevel;

    @BindView(R.id.player_desc)
    TextView playerDesc;

    @BindView(R.id.player_experience)
    ProgressBar playerExperienceProgress;

    @BindView(R.id.xp_level_start)
    TextView xpLevelStart;

    @BindView(R.id.xp_level_end)
    TextView xpLevelEnd;

    @BindView(R.id.player_current_experience)
    TextView playerCurrentExperience;

    @BindView(R.id.post_count)
    TextView postCount;

    @BindView(R.id.follower_count)
    TextView followerCount;

    @BindView(R.id.following_count)
    TextView followingCount;

    @BindView(R.id.follow)
    Button follow;

    @BindView(R.id.player_post_list)
    EmptyStateRecyclerView postList;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;

    private String playerId;
    private NumberFormat numberFormatter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_profile);
        ButterKnife.bind(this);

        playerId = getIntent().getStringExtra(Constants.PLAYER_ID_EXTRA_KEY);

        if (StringUtils.isEmpty(playerId)) {
            throw new IllegalArgumentException("Player id is required");
        }

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitleEnabled(false);
        getSupportActionBar().setTitle(R.string.activity_player_profile_title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        postList.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/posts");
        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class,
                R.layout.feed_post_item,
                PostViewHolder.class,
                ref.orderByChild("playerId").equalTo(getPlayerId()).limitToLast(100)) {
            @Override
            protected void populateViewHolder(PostViewHolder holder, Post post, int position) {
                PostBinder.bind(holder, post, getPlayerId());
                holder.likePost.setOnClickListener(v -> onLikePost(post));
                holder.addQuest.setOnClickListener(v -> onAddQuest(post));
            }
        };

        postList.setAdapter(adapter);

        numberFormatter = NumberFormat.getNumberInstance();
    }


    @Override
    public void onDataChanged(PlayerProfile playerProfile) {
        playerAvatar.setImageResource(playerProfile.getPlayerAvatar().picture);
        petAvatar.setImageResource(playerProfile.getPetAvatar().headPicture);
        GradientDrawable drawable = (GradientDrawable) petState.getBackground();
        drawable.setColor(ContextCompat.getColor(this, Pet.PetState.valueOf(playerProfile.getPetState()).color));
        petName.setText(playerProfile.getPetName());
        playerName.setText(playerProfile.getDisplayName());
        playerUsername.setText("@" + playerProfile.getUsername());
        playerDesc.setText(playerProfile.getDescription());
        playerLevel.setText(getString(R.string.player_profile_level, playerProfile.getLevel(), playerProfile.getTitle()));
        postCount.setText(String.valueOf(playerProfile.getPostedQuests().size()));
        followerCount.setText(String.valueOf(playerProfile.getFollowers().size()));
        followingCount.setText(String.valueOf(playerProfile.getFollowings().size()));
        updateExperienceProgress(playerProfile);
        if (!playerProfile.getId().equals(getPlayerId())) {
            follow.setVisibility(View.VISIBLE);
            follow.setOnClickListener(v -> {
                if(getPlayer().isGuest()) {
                    Snackbar snackbar = Snackbar.make(rootContainer, R.string.sign_in_to_follow_message, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.sign_in_button, view -> startActivity(new Intent(this, SignInActivity.class)));
                    snackbar.show();
                    return;
                }
            });
        }
    }

    private void updateExperienceProgress(PlayerProfile playerProfile) {
        int currentLevel = playerProfile.getLevel();
        BigInteger requiredXPForCurrentLevel = ExperienceForLevelGenerator.forLevel(currentLevel);
        BigInteger requiredXPForNextLevel = ExperienceForLevelGenerator.forLevel(currentLevel + 1);
        BigDecimal xpForNextLevel = new BigDecimal(requiredXPForNextLevel.subtract(requiredXPForCurrentLevel));
        BigDecimal currentXP = new BigDecimal(new BigInteger(playerProfile.getExperience()).subtract(requiredXPForCurrentLevel));
        int levelProgress = (int) (currentXP.divide(xpForNextLevel, 2, RoundingMode.HALF_UP).doubleValue() * Constants.XP_BAR_MAX_VALUE);
        xpLevelStart.setText(numberFormatter.format(requiredXPForCurrentLevel));
        xpLevelEnd.setText(numberFormatter.format(requiredXPForNextLevel));
        playerCurrentExperience.setText(getString(R.string.player_profile_current_xp, numberFormatter.format(new BigInteger(playerProfile.getExperience()))));
        playerExperienceProgress.setMax(Constants.XP_BAR_MAX_VALUE);
        ProgressBarAnimation anim = new ProgressBarAnimation(playerExperienceProgress, 0, levelProgress);
        anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        playerExperienceProgress.startAnimation(anim);
    }

    private void onAddQuest(Post post) {
        Player player = getPlayer();
        if(player.isGuest()) {
            Snackbar snackbar = Snackbar.make(rootContainer, R.string.sign_in_to_add_post_as_quest_message, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.sign_in_button, view -> startActivity(new Intent(this, SignInActivity.class)));
            snackbar.show();
            return;
        }
        if (!post.isAddedByPlayer(player.getId())) {
            feedPersistenceService.addPostToPlayer(post, player.getId());
        }
        // @TODO show schedule dialog
    }

    private void onLikePost(Post post) {
        Player player = getPlayer();
        if(player.isGuest()) {
            Snackbar snackbar = Snackbar.make(rootContainer, R.string.sign_in_to_like_post_message, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.sign_in_button, view -> startActivity(new Intent(this, SignInActivity.class)));
            snackbar.show();
            return;
        }
        if (post.isLikedByPlayer(player.getId())) {
            feedPersistenceService.removeLike(post, player.getId());
        } else {
            feedPersistenceService.addLike(post, player.getId());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        feedPersistenceService.listenForPlayerProfile(playerId, this);
    }

    @Override
    protected void onStop() {
        feedPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }
}
