package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.viewmodels.AgendaViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class AgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final Bus eventBus;
    private final List<AgendaViewModel> viewModels;

    public AgendaAdapter(Context context, Bus eventBus, List<AgendaViewModel> viewModels) {
        this.context = context;
        this.eventBus = eventBus;
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
        AgendaViewModel vm = viewModels.get(holder.getAdapterPosition());
        vh.name.setText(vm.getName());
        if (vm.isCompleted()) {
            vh.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_black_24dp, 0, 0, 0);
        } else {
            vh.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        vh.itemView.setOnClickListener(v -> {
            if (vm.isCompleted()) {
                Toast.makeText(context, R.string.cannot_edit_completed_quests, Toast.LENGTH_SHORT).show();
            } else {
                eventBus.post(new ShowQuestEvent(vm.getQuest(), EventSource.AGENDA_CALENDAR));
            }
        });
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
