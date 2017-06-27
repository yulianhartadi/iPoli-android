package io.ipoli.android.feed.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.feed.data.PlayerProfile;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.feed.ui.PostViewHolder;
import io.ipoli.android.player.Avatar;
import io.ipoli.android.quest.activities.QuestPickerActivity;

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

        // @TODO remove this
        feedPersistenceService.findPlayerProfile(App.getPlayerId(), profile -> {
            if (profile == null) {
                feedPersistenceService.createPlayerProfile(new PlayerProfile(getPlayer()));
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/posts");
        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class,
                R.layout.feed_post_item,
                PostViewHolder.class,
                ref.limitToLast(100)) {
            @Override
            protected void populateViewHolder(PostViewHolder holder, Post post, int position) {
                int marginBottom = position == 0 ? 92 : 4;
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                lp.bottomMargin = (int) ViewUtils.dpToPx(marginBottom, getResources());
                holder.itemView.setLayoutParams(lp);

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

    @OnClick(R.id.add_quest_to_feed)
    public void onAddQuestToFeed(View view) {
        startActivity(new Intent(this, QuestPickerActivity.class));
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
