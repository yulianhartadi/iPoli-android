package io.ipoli.android.feed.ui;

import android.content.Context;
import android.view.View;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.data.Profile;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/3/17.
 */
public class ProfileListBinder {

    private final ProfileListViewHolder holder;
    private final Profile profile;
    private final String playerId;

    private ProfileListBinder(ProfileListViewHolder holder, Profile profile, String playerId) {
        this.holder = holder;
        this.profile = profile;
        this.playerId = playerId;
    }

    protected void bind() {
        Context context = holder.itemView.getContext();
        holder.displayName.setText(profile.getDisplayName());
        holder.username.setText("@" + profile.getUsername());
        String desc = StringUtils.isEmpty(profile.getDescription()) ?
                context.getString(R.string.profile_default_bio) :
                profile.getDescription();
        holder.description.setText(desc);

        String[] playerTitles = context.getResources().getStringArray(R.array.player_titles);
        String playerTitle = playerTitles[Math.min(profile.getLevel() / 10, playerTitles.length - 1)];
        holder.level.setText(context.getString(R.string.player_profile_list_level, profile.getLevel(), playerTitle));
        holder.avatar.setImageResource(profile.getPlayerAvatar().picture);

        if (profile.getId().equals(playerId)) {
            holder.follow.setVisibility(View.INVISIBLE);
            holder.following.setVisibility(View.INVISIBLE);
        } else {
            if (profile.getFollowers().containsKey(playerId)) {
                holder.follow.setVisibility(View.INVISIBLE);
                holder.following.setVisibility(View.VISIBLE);
            } else {
                holder.follow.setVisibility(View.VISIBLE);
                holder.following.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void bind(ProfileListViewHolder holder, Profile profile, String playerId) {
        new ProfileListBinder(holder, profile, playerId).bind();
    }
}
