package com.curiousily.ipoli.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.app.BaseActivity;
import com.curiousily.ipoli.app.api.events.APIErrorEvent;
import com.curiousily.ipoli.quest.AddQuestActivity;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.QuestDetailActivity;
import com.curiousily.ipoli.quest.services.QuestService;
import com.curiousily.ipoli.quest.services.events.UpdateQuestEvent;
import com.curiousily.ipoli.schedule.ui.DailyScheduleFragment;
import com.curiousily.ipoli.schedule.ui.PostponeQuestDialog;
import com.curiousily.ipoli.schedule.ui.QuestDoneDialog;
import com.curiousily.ipoli.schedule.ui.events.ChangeToolbarTitleEvent;
import com.curiousily.ipoli.schedule.ui.events.FinishQuestEvent;
import com.curiousily.ipoli.schedule.ui.events.PostponeQuestEvent;
import com.curiousily.ipoli.schedule.ui.events.ShowQuestEvent;
import com.curiousily.ipoli.utils.DataSharingUtils;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class DailyScheduleActivity extends BaseActivity {

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;

    @Bind(R.id.nav_view)
    NavigationView navigationView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_schedule);
        ButterKnife.bind(this);
        initUI(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent == null || TextUtils.isEmpty(getIntent().getAction())) {
            return;
        }
        Quest quest = new Quest();
        switch (getIntent().getAction()) {
            case Constants.ACTION_QUEST_DONE:
                cancelUpdates();
                cancelNotification();
                quest.id = getIntent().getStringExtra("id");
                EventBus.post(new FinishQuestEvent(quest));
                break;
            case Constants.ACTION_QUEST_CANCELED:
                cancelUpdates();
                cancelNotification();
                quest.id = getIntent().getStringExtra("id");
                quest.status = Quest.Status.SCHEDULED;
                EventBus.post(new UpdateQuestEvent(quest));
                break;

        }
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(Constants.QUEST_RUNNING_NOTIFICATION_ID);
    }

    private void cancelUpdates() {
        PendingIntent pendingIntent = PendingIntent.getService(this, Constants.QUEST_RUNNING_REQUEST_CODE,
                new Intent(this, QuestService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
    }

    private void initUI(Bundle savedInstanceState) {
        setupActionBar();
        setupDrawerContent();
        if (savedInstanceState != null) {
            return;
        }
        addDailyScheduleFragment();
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle("");
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.add_button)
    public void onAddButtonClick() {
        Intent intent = new Intent(this, AddQuestActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private void addDailyScheduleFragment() {
        DailyScheduleFragment firstFragment = new DailyScheduleFragment();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.add_quest_fragment_container, firstFragment).commit();
    }

    @Subscribe
    public void onShowQuest(ShowQuestEvent e) {
        Intent intent = new Intent(this, QuestDetailActivity.class);
        DataSharingUtils.put(Constants.DATA_SHARING_KEY_QUEST, e.quest, intent);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private void setupDrawerContent() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onPostponeQuest(PostponeQuestEvent e) {
        PostponeQuestDialog newFragment = PostponeQuestDialog.newInstance();
        newFragment.setQuest(e.quest);
        newFragment.show(getSupportFragmentManager(), Constants.ALERT_DIALOG_TAG);
    }

    @Subscribe
    public void onFinishQuest(FinishQuestEvent e) {
        QuestDoneDialog newFragment = QuestDoneDialog.newInstance();
        newFragment.setQuest(e.quest);
        newFragment.show(getSupportFragmentManager(), Constants.ALERT_DIALOG_TAG);
    }


    @Subscribe
    public void onAPIError(APIErrorEvent e) {
        showAlertDialog(R.string.error_server_unreachable_title, e.error.getLocalizedMessage());
    }

    @Subscribe
    public void onChangeToolbarTitle(ChangeToolbarTitleEvent e) {
        toolbarTitle.setText(e.text);
    }
}
