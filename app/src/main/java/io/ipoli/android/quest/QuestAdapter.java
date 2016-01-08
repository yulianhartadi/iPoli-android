package io.ipoli.android.quest;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {

    private List<Quest> quests;

    public QuestAdapter(List<Quest> quests) {
        this.quests = quests;
    }

    @Override
    public QuestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox cb = (CheckBox) holder.itemView.findViewById(R.id.quest_check);
                cb.setChecked(!cb.isChecked());
            }
        });

        CheckBox cb = (CheckBox) holder.itemView.findViewById(R.id.quest_check);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    q.setStatus(Quest.Status.PLANNED.name());
                } else {
                    q.setStatus(Quest.Status.UNPLANNED.name());
                }
            }
        });

        if (Quest.Status.valueOf(q.getStatus()) != Quest.Status.UNPLANNED) {
            cb.setChecked(true);
        }
        TextView tv = (TextView) holder.itemView.findViewById(R.id.quest_name);
        tv.setText(q.getName());

        TextView createdTV = (TextView) holder.itemView.findViewById(R.id.quest_created_at);
        createdTV.setText(new PrettyTime().format(q.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v) {
            super(v);
        }
    }
}