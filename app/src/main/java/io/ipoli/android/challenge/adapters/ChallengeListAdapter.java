package io.ipoli.android.challenge.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.events.ShowChallengeEvent;
import io.ipoli.android.challenge.ui.events.CancelDeleteChallengeEvent;
import io.ipoli.android.challenge.ui.events.CompleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.DeleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.EditChallengeRequestEvent;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class ChallengeListAdapter extends RecyclerView.Adapter<ChallengeListAdapter.ViewHolder> {

    private final Context context;
    private final List<Challenge> challenges;
    private final Bus eventBus;

    public ChallengeListAdapter(Context context, List<Challenge> challenges, Bus eventBus) {
        this.context = context;
        this.challenges = challenges;
        this.eventBus = eventBus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.challenge_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Challenge challenge = challenges.get(position);

        Category category = Challenge.getCategory(challenge);

        holder.contentLayout.setOnClickListener(view ->
                eventBus.post(new ShowChallengeEvent(challenge, EventSource.CHALLENGES)));

        holder.contextIndicatorImage.setImageResource(category.colorfulImage);

        holder.name.setText(challenge.getName());
        holder.endDate.setText(DateFormatter.format(challenge.getEndDate()));

        holder.moreMenu.setOnClickListener(v -> {
            eventBus.post(new ItemActionsShownEvent(EventSource.CHALLENGES));
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.challenge_actions_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.complete_challenge:
                        eventBus.post(new CompleteChallengeRequestEvent(challenge, EventSource.CHALLENGES));
                        return true;
                    case R.id.edit_challenge:
                        eventBus.post(new EditChallengeRequestEvent(challenge, EventSource.CHALLENGES));
                        return true;
                    case R.id.delete_challenge:
                        AlertDialog d = new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.dialog_delete_challenge_title))
                                .setMessage(context.getString(R.string.dialog_delete_challenge_message))
                                .create();
                        d.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.dialog_yes), (dialogInterface, i) -> {
                            eventBus.post(new DeleteChallengeRequestEvent(challenge, true, EventSource.EDIT_CHALLENGE));
                            Toast.makeText(context, R.string.challenge_with_quests_deleted, Toast.LENGTH_SHORT).show();
                        });
                        d.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.dialog_no), (dialog, which) -> {
                            eventBus.post(new DeleteChallengeRequestEvent(challenge, false, EventSource.EDIT_CHALLENGE));
                            Toast.makeText(context, R.string.challenge_deleted, Toast.LENGTH_SHORT).show();
                        });
                        d.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.cancel), (dialogInterface, i) -> {
                            eventBus.post(new CancelDeleteChallengeEvent(challenge, EventSource.EDIT_CHALLENGE));
                        });
                        d.show();
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });
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

        @BindView(R.id.challenge_category_indicator_image)
        ImageView contextIndicatorImage;

        @BindView(R.id.challenge_more_menu)
        ImageButton moreMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
