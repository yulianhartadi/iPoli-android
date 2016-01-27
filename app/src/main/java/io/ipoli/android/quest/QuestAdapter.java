package io.ipoli.android.quest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.QuestCompleteRequestEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.StartQuestEvent;
import io.ipoli.android.quest.events.StopQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {

    private final Context context;

    private List<Quest> quests;
    private final Bus eventBus;

    public QuestAdapter(Context context, List<Quest> quests, Bus eventBus) {
        this.context = context;
        this.quests = quests;
        this.eventBus = eventBus;
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

        TextView nameView = (TextView) holder.itemView.findViewById(R.id.quest_name);
        nameView.setText(q.getName());

        final Button startBtn = (Button) holder.itemView.findViewById(R.id.quest_start);
        if (Status.valueOf(q.getStatus()) == Status.STARTED) {
            startBtn.setText(context.getString(R.string.stop));
        } else if (Status.valueOf(q.getStatus()) == Status.PLANNED) {
            startBtn.setText(context.getString(R.string.start));

        }
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Status status = Status.valueOf(q.getStatus());
                if (status == Status.PLANNED) {
                    updateQuestStatus(q, Status.STARTED);
                } else if (status == Status.STARTED) {
                    updateQuestStatus(q, Status.PLANNED);
                }
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        holder.itemView.findViewById(R.id.quest_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new EditQuestRequestEvent(q.getId(), holder.getAdapterPosition()));
            }
        });

        CheckBox doneBox = (CheckBox) holder.itemView.findViewById(R.id.quest_done);
        doneBox.setChecked(false);
        doneBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    int position = holder.getAdapterPosition();
                    removeQuest(position);
                    eventBus.post(new QuestCompleteRequestEvent(q, position));
                }
            }
        });
    }

    public void updateQuestStatus(Quest quest, Status status) {
        int questIndex = getQuestIndex(quest);
        if (questIndex < 0) {
            return;
        }
        Quest q = quests.get(questIndex);
        Status oldStatus = Status.valueOf(q.getStatus());
        q.setStatus(status.name());
        if (status == Status.COMPLETED) {
            removeQuest(questIndex);
            eventBus.post(new QuestCompleteRequestEvent(q, questIndex));
            return;
        }
        if (status == Status.PLANNED && oldStatus == Status.STARTED) {
            eventBus.post(new StopQuestEvent(q));
            notifyItemChanged(questIndex);
        } else if (status == Status.STARTED && oldStatus == Status.PLANNED) {
            stopOtherRunningQuests(q);
            eventBus.post(new StartQuestEvent(q));
            notifyItemChanged(questIndex);
        }
        eventBus.post(new QuestUpdatedEvent(q));
    }

    private void stopOtherRunningQuests(Quest q) {
        for (int i = 0; i < quests.size(); i++) {
            Quest cq = quests.get(i);
            if (cq != q && Status.valueOf(cq.getStatus()) == Status.STARTED) {
                cq.setStatus(Status.PLANNED.name());
                eventBus.post(new StopQuestEvent(cq));
                notifyItemChanged(i);
                eventBus.post(new QuestUpdatedEvent(cq));
            }
        }
    }

    private int getQuestIndex(Quest quest) {
        for (int i = 0; i < quests.size(); i++) {
            Quest q = quests.get(i);
            if (q.getId().equals(quest.getId())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public void putQuest(int position, Quest quest) {
        quests.add(position, quest);
        notifyItemInserted(position);
    }

    public void updateQuest(int position, Quest quest) {
        quests.set(position, quest);
        notifyItemChanged(position);
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public void addQuest(Quest quest) {
        quests.add(quest);
        notifyItemInserted(quests.size() - 1);
    }

    public void removeQuest(int position) {
        quests.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v) {
            super(v);
        }
    }
}