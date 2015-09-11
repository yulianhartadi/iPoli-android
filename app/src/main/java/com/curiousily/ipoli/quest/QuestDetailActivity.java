package com.curiousily.ipoli.quest;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.app.BaseActivity;
import com.curiousily.ipoli.databinding.ActivityQuestDetailBinding;
import com.curiousily.ipoli.databinding.ListItemSubQuestBinding;
import com.curiousily.ipoli.quest.events.StartQuestEvent;
import com.curiousily.ipoli.quest.services.events.UpdateQuestEvent;
import com.curiousily.ipoli.quest.viewmodel.QuestViewModel;
import com.curiousily.ipoli.quest.viewmodel.SubQuestViewModel;
import com.curiousily.ipoli.schedule.DailyScheduleActivity;
import com.curiousily.ipoli.schedule.ui.QuestDoneDialog;
import com.curiousily.ipoli.ui.dialogs.InputDialogFragment;
import com.curiousily.ipoli.ui.events.UserInputEvent;
import com.curiousily.ipoli.utils.DataSharingUtils;
import com.squareup.otto.Subscribe;

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
public class QuestDetailActivity extends BaseActivity {

    @Bind(R.id.quest_details_sub_quests)
    ListView subQuests;

    private Quest quest;
    private SubQuestAdapter subQuestAdapter;
    private QuestViewModel questViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityQuestDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_quest_detail);
        ButterKnife.bind(this);

        quest = DataSharingUtils.get(Constants.DATA_SHARING_KEY_QUEST, Quest.class, getIntent());

        questViewModel = QuestViewModel.from(quest);
        binding.setQuest(questViewModel);

        subQuestAdapter = new SubQuestAdapter(this, questViewModel.subQuests);
        subQuests.setAdapter(subQuestAdapter);

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
        DialogFragment newFragment = InputDialogFragment.newInstance(R.string.new_sub_quest);
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
    public void onStartClick(FloatingActionButton button) {
        boolean isRunning = questViewModel.isRunning.get();
        questViewModel.isRunning.set(!isRunning);
        if (isRunning) {
            Intent notificationIntent = new Intent(this, DailyScheduleActivity.class);
            notificationIntent.setAction(Constants.ACTION_QUEST_DONE);
            notificationIntent.putExtra("id", quest.id);
            startActivity(notificationIntent);
            finish();
        } else {
            quest.status = Quest.Status.RUNNING;
            Snackbar.make(findViewById(R.id.quest_details_content), R.string.quest_started, Snackbar.LENGTH_LONG).show();
            EventBus.post(new UpdateQuestEvent(quest));
            EventBus.post(new StartQuestEvent(quest));
        }
    }

    @Subscribe
    public void onUserInput(UserInputEvent e) {
        quest.subQuests.add(new SubQuest(e.input));
        subQuestAdapter.add(new SubQuestViewModel(e.input, false));
        subQuestAdapter.notifyDataSetChanged();
    }

    static class SubQuestAdapter extends ArrayAdapter<SubQuestViewModel> {

        public SubQuestAdapter(Context context, List<SubQuestViewModel> subQuests) {
            super(context, R.layout.list_item_sub_quest, subQuests);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemSubQuestBinding binding;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                binding = ListItemSubQuestBinding.inflate(inflater, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemSubQuestBinding) convertView.getTag();
            }
            binding.setSubQuest(getItem(position));
            return convertView;
        }
    }
}
