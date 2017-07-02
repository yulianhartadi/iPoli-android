package io.ipoli.android.feed.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/3/17.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder {

    public ProfileViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
