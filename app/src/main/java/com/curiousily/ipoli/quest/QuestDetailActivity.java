package com.curiousily.ipoli.quest;

import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.databinding.ActivityQuestDetailBinding;
import com.curiousily.ipoli.databinding.RecyclerListItemSubQuestBinding;
import com.curiousily.ipoli.quest.events.StartQuestEvent;
import com.curiousily.ipoli.quest.viewmodel.QuestViewModel;
import com.curiousily.ipoli.quest.viewmodel.SubQuestViewModel;
import com.curiousily.ipoli.schedule.ui.QuestDoneDialog;
import com.curiousily.ipoli.ui.dialogs.PromptDialogFragment;
import com.curiousily.ipoli.utils.DataSharingUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/31/15.
 */
public class QuestDetailActivity extends AppCompatActivity {

    @Bind(R.id.quest_details_sub_quests)
    RecyclerView subQuests;


    private Quest quest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityQuestDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_quest_detail);
        ButterKnife.bind(this);

        quest = DataSharingUtils.get(Constants.DATA_SHARING_KEY_QUEST, Quest.class, getIntent());

        QuestViewModel questViewModel = QuestViewModel.from(quest);
        binding.setQuest(questViewModel);

        subQuests.setLayoutManager(new LinearLayoutManager(this));
        subQuests.setHasFixedSize(true);
        subQuests.setAdapter(new SubQuestAdapter(questViewModel.subQuests));

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(questViewModel.backgroundColor));
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overrideExitAnimation();
            }
        });
    }

    @BindingAdapter({"bind:materialIcon"})
    public static void bindIcon(MaterialIconView view, MaterialDrawableBuilder.IconValue icon) {
        view.setIcon(icon);
    }

    @BindingAdapter({"android:background"})
    public static void bindColor(AppBarLayout view, int color) {
        view.setBackgroundResource(color);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overrideExitAnimation();
    }

    private void overrideExitAnimation() {
        overridePendingTransition(R.anim.reverse_slide_in, R.anim.reverse_slide_out);
    }


    @OnClick(R.id.quest_details_add_sub_quest)
    public void onAddSubQuestClick(View view) {
        DialogFragment newFragment = PromptDialogFragment.newInstance(R.string.new_sub_quest);
        newFragment.show(getSupportFragmentManager(), Constants.ALERT_DIALOG_TAG);
    }

    private void showQuestRunningDialog() {
        DialogFragment newFragment = QuestDoneDialog.newInstance();
        newFragment.show(getSupportFragmentManager(), Constants.ALERT_DIALOG_TAG);
    }

    @OnClick(R.id.quest_details_timer_icon)
    public void onTimerClick(View view) {
        showQuestRunningDialog();
    }

    @OnClick(R.id.quest_details_share)
    public void onShareClick(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_dialog_message), quest.name));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_dialog_title)));
    }

    @OnClick(R.id.quest_details_start)
    public void onStartClick(View view) {
        EventBus.post(new StartQuestEvent(quest));
    }

    static class SubQuestAdapter extends RecyclerView.Adapter<SubQuestAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final RecyclerListItemSubQuestBinding binding;

            ViewHolder(final RecyclerListItemSubQuestBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        private final List<SubQuestViewModel> subQuests;
        private LayoutInflater layoutInflater;

        public SubQuestAdapter(List<SubQuestViewModel> subQuests) {
            this.subQuests = subQuests;
        }

        @Override
        public int getItemCount() {
            return subQuests.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int i) {
            viewHolder.binding.setSubQuest(subQuests.get(i));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (layoutInflater == null) {
                layoutInflater = LayoutInflater.from(viewGroup.getContext());
            }
            RecyclerListItemSubQuestBinding binding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.recycler_list_item_sub_quest, viewGroup, false);
            return new ViewHolder(binding);
        }
    }
}
