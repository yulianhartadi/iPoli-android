package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.QuestPickedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/28/16.
 */
public class QuestPickerAdapter extends RecyclerView.Adapter<QuestPickerAdapter.ViewHolder> {
    protected Context context;
    protected final Bus evenBus;
    protected List<Quest> quests;

    public QuestPickerAdapter(Context context, Bus evenBus, List<Quest> quests) {
        this.context = context;
        this.evenBus = evenBus;
        this.quests = quests;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quest_picker_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Quest q = quests.get(holder.getAdapterPosition());

        holder.categoryIndicatorImage.setImageResource(q.getCategoryType().colorfulImage);
        holder.name.setText(q.getName());
        holder.completedAt.setText(DateFormatter.formatWithoutYear(context, DateUtils.fromMillis(q.getCompletedAt())));
        holder.itemView.setOnClickListener(v -> evenBus.post(new QuestPickedEvent(q)));

    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public void setQuests(List<Quest> quests) {
        this.quests = quests;
        notifyDataSetChanged();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.quest_name)
        TextView name;

        @BindView(R.id.quest_category_indicator_image)
        ImageView categoryIndicatorImage;

        @BindView(R.id.quest_completed_at)
        TextView completedAt;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}