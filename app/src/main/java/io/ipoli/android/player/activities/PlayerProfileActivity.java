package io.ipoli.android.player.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.feed.data.PlayerProfile;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.feed.ui.PostBinder;
import io.ipoli.android.feed.ui.PostViewHolder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/27/17.
 */
public class PlayerProfileActivity extends BaseActivity implements OnDataChangedListener<PlayerProfile> {

    @Inject
    FeedPersistenceService feedPersistenceService;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_profile);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitleEnabled(false);
        getSupportActionBar().setTitle("Profile");

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
    }


    @Override
    public void onDataChanged(PlayerProfile playerProfile) {
        
    }

    private void onAddQuest(Post post) {
        String playerId = App.getPlayerId();
        if (!post.isAddedByPlayer(playerId)) {
            feedPersistenceService.addPostToPlayer(post, playerId);
        }
        // @TODO show schedule dialog
    }

    private void onLikePost(Post post) {
        String playerId = App.getPlayerId();
        if (post.isLikedByPlayer(playerId)) {
            feedPersistenceService.removeLike(post, playerId);
        } else {
            feedPersistenceService.addLike(post, playerId);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        feedPersistenceService.listenForPlayerProfile(getPlayerId(), this);
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
