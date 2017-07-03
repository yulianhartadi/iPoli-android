package io.ipoli.android.feed.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/3/17.
 */
public class ProfileListViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.player_display_name)
    TextView displayName;

    @BindView(R.id.player_username)
    TextView username;

    @BindView(R.id.player_description)
    TextView description;


    @BindView(R.id.player_level)
    TextView level;

    @BindView(R.id.player_avatar)
    ImageView avatar;

    @BindView(R.id.follow)
    Button follow;

    @BindView(R.id.following)
    Button following;

    public ProfileListViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
