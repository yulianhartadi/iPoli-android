package io.ipoli.android.feed.ui;

import android.text.format.DateUtils;

import io.ipoli.android.R;
import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/27/17.
 */
public class PostBinder {

    private final PostViewHolder holder;
    private final Post post;
    private final String playerId;

    private PostBinder(PostViewHolder holder, Post post, String playerId) {
        this.holder = holder;
        this.post = post;
        this.playerId = playerId;
    }

    protected void bind() {
        holder.playerUsername.setText(post.getPlayerUsername());
        String[] playerTitles = holder.playerTitle.getResources().getStringArray(R.array.player_titles);
        String playerTitle = playerTitles[Math.min(post.getPlayerLevel() / 10, playerTitles.length - 1)];
        holder.playerTitle.setText("Level " + post.getPlayerLevel() + ": " + playerTitle);
        holder.postTitle.setText(post.getTitle());
        holder.postMessage.setText(post.getMessage());
        holder.postImage.setImageResource(post.getCategoryType().colorfulImage);
        holder.playerAvatar.setImageResource(post.getPlayerAvatar().picture);
        holder.postLikesCount.setText(String.valueOf(post.getLikes().size()));
        holder.postAddedCount.setText(String.valueOf(post.getAddedBy().size()));
        holder.questCoins.setText(post.getCoins().toString());
        holder.questRewardPoints.setText(post.getRewardPoints().toString());
        holder.questExperience.setText(post.getExperience().toString() + " XP");
        holder.postCreatedAt.setText(DateUtils.getRelativeTimeSpanString(post.getCreatedAt(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));
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
    }

    public static void bind(PostViewHolder holder, Post post, String playerId) {
        new PostBinder(holder, post, playerId).bind();
    }
}
