package io.ipoli.android.achievement.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.achievement.Achievement;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/28/17.
 */
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private final Set<Integer> unlockedAchievementCodes;

    public AchievementAdapter(Set<Integer> unlockedAchievementCodes) {
        this.unlockedAchievementCodes = unlockedAchievementCodes;
    }

    @Override
    public AchievementViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.achievement_item, viewGroup, false);
        return new AchievementViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AchievementViewHolder holder, int position) {
        Achievement achievement = Achievement.values()[holder.getAdapterPosition()];
        holder.icon.setImageResource(achievement.icon);
        holder.name.setText(achievement.name);
        holder.description.setText(achievement.description);
        if (unlockedAchievementCodes.contains(achievement.code)) {
            holder.itemView.setBackgroundResource(R.color.md_white);
            holder.icon.setImageAlpha(255);
            holder.name.setEnabled(true);
            holder.description.setEnabled(true);
        } else {
            holder.itemView.setBackgroundResource(R.color.md_grey_100);
            holder.icon.setImageAlpha(90);
            holder.name.setEnabled(false);
            holder.description.setEnabled(false);
        }
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
