package io.ipoli.android.feed.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.player.Avatar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/17.
 */
public class FeedActivity extends BaseActivity {

    @Inject
    FeedPersistenceService feedPersistenceService;

    @BindView(R.id.feed_list)
    RecyclerView feedList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        feedList.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/posts");
        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class,
                R.layout.feed_post_item,
                PostViewHolder.class,
                ref) {
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

                holder.likePost.setOnClickListener(v -> onLikePost(post));
                holder.addQuest.setOnClickListener(v -> onAddQuest(post));
            }
        };

        feedList.setAdapter(adapter);
    }

    private void onAddQuest(Post post) {
        String playerId = App.getPlayerId();
        if (!post.isAddedByPlayer(playerId)) {
            post.addAddedBy(playerId);
            feedPersistenceService.updatePost(post);
        }
        // @TODO show schedule dialog
    }

    private void onLikePost(Post post) {
        String playerId = App.getPlayerId();
        if (post.isLikedByPlayer(playerId)) {
            post.removeLike(playerId);
        } else {
            post.addLike(playerId);
        }
        feedPersistenceService.updatePost(post);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.player_avatar)
        ImageView playerAvatar;

        @BindView(R.id.player_username)
        TextView playerUsername;

        @BindView(R.id.player_title)
        TextView playerTitle;

        @BindView(R.id.post_image)
        ImageView postImage;

        @BindView(R.id.post_title)
        TextView postTitle;

        @BindView(R.id.post_message)
        TextView postMessage;

        @BindView(R.id.post_created_at)
        TextView postCreatedAt;

        @BindView(R.id.post_like_count)
        TextView postLikesCount;

        @BindView(R.id.post_added_count)
        TextView postAddedCount;

        @BindView(R.id.quest_coins)
        TextView questCoins;

        @BindView(R.id.quest_reward_points)
        TextView questRewardPoints;

        @BindView(R.id.quest_experience)
        TextView questExperience;

        @BindView(R.id.post_like)
        ImageButton likePost;

        @BindView(R.id.post_add_quest)
        ImageButton addQuest;

        public PostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }
}
