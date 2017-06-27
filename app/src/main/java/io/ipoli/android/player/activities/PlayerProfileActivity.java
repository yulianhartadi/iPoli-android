package io.ipoli.android.player.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.ui.PostViewHolder;
import io.ipoli.android.player.Avatar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/27/17.
 */
public class PlayerProfileActivity extends BaseActivity {

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

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
                holder.playerUsername.setText(post.getPlayerUsername());
                String[] playerTitles = getResources().getStringArray(R.array.player_titles);
                String playerTitle = playerTitles[Math.min(post.getPlayerLevel() / 10, playerTitles.length - 1)];
                holder.playerTitle.setText("Level " + post.getPlayerLevel() + ": " + playerTitle);
                holder.postTitle.setText(post.getTitle());
                holder.postMessage.setText(post.getMessage());
                holder.postImage.setImageResource(post.getCategoryType().colorfulImage);
                holder.playerAvatar.setImageResource(Avatar.get(Integer.parseInt(post.getPlayerAvatar())).picture);
                holder.postLikesCount.setText(String.valueOf(post.getLikes().size()));
                holder.postAddedCount.setText(String.valueOf(post.getAddedBy().size()));
                holder.questCoins.setText(post.getCoins().toString());
                holder.questRewardPoints.setText(post.getRewardPoints().toString());
                holder.questExperience.setText(post.getExperience().toString() + " XP");
                holder.postCreatedAt.setText(DateUtils.getRelativeTimeSpanString(post.getCreatedAt(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));
                String playerId = App.getPlayerId();
                if (post.isLikedByPlayer(playerId)) {
                    holder.likePost.setImageResource(R.drawable.ic_favorite_accent_24dp);
                } else {
                    holder.likePost.setImageResource(R.drawable.ic_favorite_outline_black_24dp);
                }

                if (post.isAddedByPlayer(playerId)) {
                    holder.addQuest.setImageResource(R.drawable.ic_playlist_add_accent_24dp);
                } else {
                    holder.addQuest.setImageResource(R.drawable.ic_playlist_add_black_24dp);
                }

//                holder.likePost.setOnClickListener(v -> onLikePost(post));
//                holder.addQuest.setOnClickListener(v -> onAddQuest(post));
            }
        };

        postList.setAdapter(adapter);
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }
}
