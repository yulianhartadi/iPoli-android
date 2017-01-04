package io.ipoli.android.quest.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.viewmodels.AgendaViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class AgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<AgendaViewModel> viewModels;

    public AgendaAdapter(List<AgendaViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new AgendaViewHolder(inflater.inflate(R.layout.agenda_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AgendaViewHolder vh = (AgendaViewHolder) holder;
        AgendaViewModel vm = viewModels.get(position);
        vh.name.setText(vm.getName());
        if (vm.isCompleted()) {
            vh.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_black_24dp, 0, 0, 0);
        } else {
            vh.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        vh.categoryIndicatorImage.setImageResource(vm.getCategoryImage());
        vh.startEnd.setText(vm.getScheduleText());
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    static class AgendaViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.quest_start_end_time)
        TextView startEnd;

        @BindView(R.id.quest_text)
        TextView name;

        @BindView(R.id.quest_category_indicator_image)
        ImageView categoryIndicatorImage;

        AgendaViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
