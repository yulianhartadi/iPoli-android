package io.ipoli.android.tutorial;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.NoInternetActivity;
import io.ipoli.android.app.events.CalendarPermissionResponseEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.NoNetworkConnectionEvent;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.tutorial.events.TutorialDoneEvent;
import io.ipoli.android.tutorial.events.TutorialSkippedEvent;
import io.ipoli.android.tutorial.fragments.PickTutorailQuestsFragment;
import io.ipoli.android.tutorial.fragments.PickTutorialRepeatingQuestsFragment;
import io.ipoli.android.tutorial.fragments.SyncAndroidCalendarFragment;
import io.ipoli.android.tutorial.fragments.TutorialFragment;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class TutorialActivity extends AppIntro2 {

    private static final int SYNC_CALENDAR_SLIDE_INDEX = 4;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    private PickTutorialRepeatingQuestsFragment pickTutorialRepeatingQuestsFragment;
    private PickTutorailQuestsFragment pickTutorailQuestsFragment;
    private SyncAndroidCalendarFragment syncAndroidCalendarFragment;

    private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

    private int previousSlide = -1;

    private QuestParser questParser = new QuestParser(new PrettyTimeParser());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(this).inject(this);

        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_welcome_title),
                getString(R.string.tutorial_welcome_desc),
                R.drawable.tutorial_welcome,
                R.color.md_indigo_500,
                false));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_hero_title),
                getString(R.string.tutorial_hero_desc),
                R.drawable.tutorial_hero,
                R.color.md_blue_500));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_rewards_title),
                getString(R.string.tutorial_reward_desc),
                R.drawable.tutorial_reward,
                R.color.md_purple_500));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_pet_title),
                getString(R.string.tutorial_pet_desc),
                R.drawable.tutorial_pet,
                R.color.md_orange_500));
        syncAndroidCalendarFragment = new SyncAndroidCalendarFragment();
        addSlide(syncAndroidCalendarFragment);
        pickTutorailQuestsFragment = new PickTutorailQuestsFragment();
        addSlide(pickTutorailQuestsFragment);
        pickTutorialRepeatingQuestsFragment = new PickTutorialRepeatingQuestsFragment();
        addSlide(pickTutorialRepeatingQuestsFragment);

        lifecycleSubject.onNext(ActivityEvent.CREATE);
        setImmersiveMode(true, true);
        setColorTransitionsEnabled(true);
    }

    @Override
    public void onDonePressed(Fragment fragment) {
        doneButton.setVisibility(View.GONE);
        List<Quest> selectedQuests = pickTutorailQuestsFragment.getSelectedQuests();
        List<RepeatingQuest> selectedRepeatingQuests = pickTutorialRepeatingQuestsFragment.getSelectedQuests();

        Observable.defer(() -> {
            List<RepeatingQuest> parsedRepeatingQuests = new ArrayList<>();
            for (RepeatingQuest rq : selectedRepeatingQuests) {
                RepeatingQuest parsedRepeatingQuest = questParser.parseRepeatingQuest(rq.getRawText());
                parsedRepeatingQuest.setCategory(rq.getCategory());
                parsedRepeatingQuest.setReminders(rq.getReminders());
                parsedRepeatingQuests.add(parsedRepeatingQuest);
            }

            questPersistenceService.save(selectedQuests);
            repeatingQuestPersistenceService.save(parsedRepeatingQuests);
            return Observable.empty();
        }).compose(RxLifecycle.bindActivity(lifecycleSubject))
                .subscribe(ignored -> {
                        }, error -> finish(),
                        () -> {
                            eventBus.post(new TutorialDoneEvent());
                            Toast.makeText(this, R.string.import_calendar_events_started, Toast.LENGTH_SHORT).show();
                            finish();
                        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifecycleSubject.onNext(ActivityEvent.START);
    }

    @Override
    public void onSlideChanged(Fragment oldFragment, Fragment newFragment) {
        if (previousSlide == SYNC_CALENDAR_SLIDE_INDEX && syncAndroidCalendarFragment.isSyncCalendarChecked()) {
            checkCalendarForPermission();
        }
        previousSlide = pager.getCurrentItem();
    }

    @Override
    public void onBackPressed() {
        eventBus.post(new TutorialSkippedEvent());
        super.onBackPressed();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        eventBus.post(new TutorialSkippedEvent());
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
        lifecycleSubject.onNext(ActivityEvent.RESUME);

    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        lifecycleSubject.onNext(ActivityEvent.PAUSE);
        super.onPause();
    }

    @Override
    protected void onStop() {
        lifecycleSubject.onNext(ActivityEvent.STOP);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        lifecycleSubject.onNext(ActivityEvent.DESTROY);
        super.onDestroy();
    }

    private void checkCalendarForPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    Constants.READ_CALENDAR_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.READ_CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                eventBus.post(new CalendarPermissionResponseEvent(CalendarPermissionResponseEvent.Response.GRANTED, EventSource.TUTORIAL));
            } else if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                eventBus.post(new CalendarPermissionResponseEvent(CalendarPermissionResponseEvent.Response.DENIED, EventSource.TUTORIAL));
            }
        }
    }

    @Subscribe
    public void onNoNetworkConnection(NoNetworkConnectionEvent e) {
        showNoInternetActivity();
    }

    private void showNoInternetActivity() {
        Intent intent = new Intent(this, NoInternetActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
