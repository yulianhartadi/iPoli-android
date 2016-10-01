package io.ipoli.android.challenge.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.challenge.viewmodels.PredefinedChallengeQuestViewModel;
import io.ipoli.android.quest.data.BaseQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/20/16.
 */
public class PredefinedChallengeQuestAdapter extends RecyclerView.Adapter<PredefinedChallengeQuestAdapter.ViewHolder> {
    private final Context context;
    private final Bus eventBus;
    private final List<PredefinedChallengeQuestViewModel> viewModels;

    public PredefinedChallengeQuestAdapter(Context context, Bus eventBus, List<PredefinedChallengeQuestViewModel> viewModels) {
        this.context = context;
        this.eventBus = eventBus;
        this.viewModels = viewModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.predefined_challenge_quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PredefinedChallengeQuestViewModel vm = viewModels.get(holder.getAdapterPosition());

        holder.name.setText(vm.getName());

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(vm.isSelected());
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                vm.select();
            } else {
                vm.deselect();
            }
        });
        holder.itemView.setOnClickListener(view -> {
            CheckBox cb = holder.check;
            cb.setChecked(!cb.isChecked());

        });
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public List<BaseQuest> getSelectedQuests() {
        List<BaseQuest> quests = new ArrayList<>();
        for (PredefinedChallengeQuestViewModel vm : viewModels) {
            if (vm.isSelected()) {
                quests.add(vm.getQuest());
            }
        }
        return quests;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.quest_check)
        CheckBox check;

        @BindView(R.id.quest_text)
        TextView name;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
