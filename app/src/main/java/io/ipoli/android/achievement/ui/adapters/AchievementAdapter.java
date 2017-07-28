package io.ipoli.android.achievement.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.achievement.Achievement;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/28/17.
 */
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    @Override
    public AchievementViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.achievement_item, viewGroup, false);
        return new AchievementViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AchievementViewHolder holder, int position) {
        Achievement achievement = Achievement.values()[holder.getAdapterPosition()];

        holder.icon.setImageResource(R.mipmap.ic_launcher);
        holder.name.setText(achievement.name);
        holder.description.setText(achievement.description);
    }

    @Override
    public int getItemCount() {
        return Achievement.values().length;
    }

    class AchievementViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.achievement_icon)
        ImageView icon;

        @BindView(R.id.achievement_name)
        TextView name;

        @BindView(R.id.achievement_description)
        TextView description;

        public AchievementViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
