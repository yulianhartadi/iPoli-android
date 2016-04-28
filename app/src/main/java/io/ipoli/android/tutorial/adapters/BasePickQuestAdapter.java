package io.ipoli.android.tutorial.adapters;

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

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.tutorial.PickQuestViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/28/16.
 */
public abstract class BasePickQuestAdapter<T> extends  RecyclerView.Adapter<BasePickQuestAdapter.ViewHolder>{
    protected Context context;
    protected final Bus evenBus;
    protected List<PickQuestViewModel<T>> viewModels;

    public BasePickQuestAdapter(Context context, Bus evenBus, List<PickQuestViewModel<T>> viewModels) {
        this.context = context;
        this.evenBus = evenBus;
        this.viewModels = viewModels;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PickQuestViewModel vm = viewModels.get(holder.getAdapterPosition());

        QuestContext ctx = getQuestContext(holder.getAdapterPosition());
        GradientDrawable drawable = (GradientDrawable) holder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, ctx.resLightColor));
        holder.contextIndicatorImage.setImageResource(ctx.whiteImage);

        holder.name.setText(vm.getText());

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(vm.isSelected());
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                vm.select();
                sendQuestSelectedEvent(holder.getAdapterPosition());
            } else {
                vm.deselect();
                sendQuestDeselectEvent(holder.getAdapterPosition());
                
            }
        });
        holder.itemView.setOnClickListener(view -> {
            CheckBox cb = holder.check;
            cb.setChecked(!cb.isChecked());

        });
    }

    protected abstract void sendQuestDeselectEvent(int adapterPosition);

    protected abstract void sendQuestSelectedEvent(int adapterPosition);

    protected abstract QuestContext getQuestContext(int adapterPosition);

    public List<T> getSelectedQuests() {
        List<T> selectedQuests = new ArrayList<>();
        for(PickQuestViewModel vm : viewModels) {
            if(vm.isSelected()) {
                selectedQuests.add((T) vm.getQuest());
            }
        }
        return selectedQuests;
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
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
