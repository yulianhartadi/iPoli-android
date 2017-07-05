package io.ipoli.android.feed.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/27/17.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.player_avatar)
    public ImageView playerAvatar;

    @BindView(R.id.player_display_name)
    public TextView playerDisplayName;

    @BindView(R.id.player_username)
    public TextView playerUsername;

    @BindView(R.id.player_title)
    public TextView playerTitle;

    @BindView(R.id.post_image)
    public ImageView postImage;

    @BindView(R.id.post_title)
    public TextView postTitle;

    @BindView(R.id.post_message)
    public TextView postMessage;

    @BindView(R.id.post_created_at)
    public TextView postCreatedAt;

    @BindView(R.id.post_kudos_count)
    public TextView postKudosCount;

    @BindView(R.id.post_added_count)
    public TextView postAddedCount;

    @BindView(R.id.quest_coins)
    public TextView questCoins;

    @BindView(R.id.quest_reward_points)
    public TextView questRewardPoints;

    @BindView(R.id.quest_experience)
    public TextView questExperience;

    @BindView(R.id.post_give_kudos)
    public ImageView giveKudos;

    @BindView(R.id.post_give_kudos_container)
    public ViewGroup giveKudosContainer;

    @BindView(R.id.post_add_quest)
    public ImageView addQuest;

    @BindView(R.id.post_add_quest_container)
    public ViewGroup addQuestContainer;

    @BindView(R.id.post_delete)
    public Button delete;

    public PostViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}