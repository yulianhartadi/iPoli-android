package io.ipoli.android.challenge.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class ChallengesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Challenge> challenges;

    public ChallengesAdapter(List<Challenge> challenges) {
        this.challenges = challenges;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.challenge_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
