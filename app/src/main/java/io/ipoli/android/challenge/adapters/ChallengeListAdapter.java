package io.ipoli.android.challenge.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.events.ShowChallengeEvent;
import io.ipoli.android.challenge.ui.events.CompleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.DeleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.EditChallengeRequestEvent;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.ui.formatters.DateFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class ChallengeListAdapter extends RecyclerView.Adapter<ChallengeListAdapter.ViewHolder> {

    private final Context context;
    private final List<Challenge> challenges;
    private final Bus eventBus;

    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public ChallengeListAdapter(Context context, List<Challenge> challenges, Bus eventBus) {
        this.context = context;
        this.challenges = challenges;
        this.eventBus = eventBus;
        viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.challenge_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Challenge challenge = challenges.get(position);
        viewBinderHelper.bind(holder.swipeLayout, challenge.getId());

        Category category = challenge.getCategory();
        GradientDrawable drawable = (GradientDrawable) holder.contextIndicatorBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, category.resLightColor));

        holder.contentLayout.setOnClickListener(view ->
                eventBus.post(new ShowChallengeEvent(challenge, EventSource.CHALLENGES)));

        holder.contextIndicatorImage.setImageResource(category.whiteImage);

        holder.name.setText(challenge.getName());
        holder.endDate.setText(DateFormatter.format(challenge.getEndDate()));
        holder.swipeLayout.setSwipeListener(new SwipeRevealLayout.SimpleSwipeListener() {
            @Override
            public void onOpened(SwipeRevealLayout view) {
                super.onOpened(view);
                eventBus.post(new ItemActionsShownEvent(EventSource.CHALLENGES));
            }
        });

        holder.completeChallenge.setOnClickListener(v -> eventBus.post(new CompleteChallengeRequestEvent(challenge, EventSource.CHALLENGES)));

        holder.editChallenge.setOnClickListener(v -> {
            holder.swipeLayout.close(true);
            eventBus.post(new EditChallengeRequestEvent(challenge, EventSource.CHALLENGES));
        });

        holder.deleteChallenge.setOnClickListener(v -> eventBus.post(new DeleteChallengeRequestEvent(challenge, EventSource.CHALLENGES)));
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.content_layout)
        View contentLayout;

        @BindView(R.id.challenge_text)
        TextView name;

        @BindView(R.id.challenge_end_date)
        TextView endDate;

        @BindView(R.id.challenge_category_indicator_background)
        View contextIndicatorBackground;

        @BindView(R.id.challenge_category_indicator_image)
        ImageView contextIndicatorImage;

        @BindView(R.id.swipe_layout)
        SwipeRevealLayout swipeLayout;

        @BindView(R.id.complete_challenge)
        ImageButton completeChallenge;

        @BindView(R.id.edit_challenge)
        ImageButton editChallenge;

        @BindView(R.id.delete_challenge)
        ImageButton deleteChallenge;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
