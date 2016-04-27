package io.ipoli.android.tutorial;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickQuestsAdapter extends RecyclerView.Adapter<PickQuestsAdapter.ViewHolder> {

    private Context context;
    private List<Quest> quests;
    private Set<Quest> selectedQuests;


    public PickQuestsAdapter(Context context, List<Quest> quests) {
        this.context = context;
        this.quests = quests;
        selectedQuests = new HashSet<>();
    }

    @Override
    public PickQuestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Quest q = quests.get(holder.getAdapterPosition());

        QuestContext ctx = Quest.getContext(q);
        GradientDrawable drawable = (GradientDrawable) holder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, ctx.resLightColor));
        holder.contextIndicatorImage.setImageResource(ctx.whiteImage);

        holder.name.setText(q.getRawText());

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(false);
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedQuests.add(q);
            } else {
                selectedQuests.remove(q);
            }
        });
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public List<Quest> getSelectedQuests() {
        return new ArrayList<>(selectedQuests);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.quest_check)
        CheckBox check;

        @Bind(R.id.quest_text)
        TextView name;

        @Bind(R.id.quest_context_indicator_background)
        public View contextIndicatorBackground;

        @Bind(R.id.quest_context_indicator_image)
        public ImageView contextIndicatorImage;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}