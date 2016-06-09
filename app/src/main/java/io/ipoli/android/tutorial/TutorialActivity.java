package io.ipoli.android.tutorial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.squareup.otto.Bus;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.CalendarPermissionResponseEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScheduleRepeatingQuestsEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.tutorial.events.TutorialDoneEvent;
import io.ipoli.android.tutorial.events.TutorialSkippedEvent;
import io.ipoli.android.tutorial.fragments.PickQuestsFragment;
import io.ipoli.android.tutorial.fragments.PickRepeatingQuestsFragment;
import io.ipoli.android.tutorial.fragments.SyncAndroidCalendarFragment;
import io.ipoli.android.tutorial.fragments.TutorialFragment;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class TutorialActivity extends AppIntro2 {
    private static final int SYNC_CALENDAR_SLIDE_INDEX = 3;


    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    private PickRepeatingQuestsFragment pickRepeatingQuestsFragment;
    private PickQuestsFragment pickQuestsFragment;
    private SyncAndroidCalendarFragment syncAndroidCalendarFragment;

    private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

    private int previousSlide = -1;

    private QuestParser questParser = new QuestParser(new PrettyTimeParser());

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_welcome_title), getString(R.string.tutorial_welcome_desc), R.drawable.tutorial_welcome, false));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_hero_title), getString(R.string.tutorial_hero_desc), R.drawable.tutorial_hero));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_rewards_title), getString(R.string.tutorial_reward_desc), R.drawable.tutorial_reward));
        syncAndroidCalendarFragment = new SyncAndroidCalendarFragment();
        addSlide(syncAndroidCalendarFragment);
        pickQuestsFragment = new PickQuestsFragment();
        addSlide(pickQuestsFragment);
        pickRepeatingQuestsFragment = new PickRepeatingQuestsFragment();
        addSlide(pickRepeatingQuestsFragment);

        int[] colors = new int[]{
                R.color.md_indigo_500,
                R.color.md_blue_500,
                R.color.md_purple_500,
                R.color.md_green_500,
                R.color.md_blue_500,
                R.color.md_blue_500
        };
        ArrayList<Integer> c = new ArrayList<>();
        for (int color : colors) {
            c.add(ContextCompat.getColor(this, color));
        }

        setAnimationColors(c);
        lifecycleSubject.onNext(ActivityEvent.CREATE);
    }

    @Override
    public void onDonePressed() {
        List<Quest> selectedQuests = pickQuestsFragment.getSelectedQuests();
        List<RepeatingQuest> selectedRepeatingQuests = pickRepeatingQuestsFragment.getSelectedQuests();

        List<RepeatingQuest> parsedRepeatingQuests = new ArrayList<>();
        for (RepeatingQuest rq : selectedRepeatingQuests) {
            RepeatingQuest parsedRepeatingQuest = questParser.parseRepeatingQuest(rq.getRawText());
            parsedRepeatingQuest.setContext(rq.getContext());
            parsedRepeatingQuests.add(parsedRepeatingQuest);
        }

        Observable.concat(questPersistenceService.saveRemoteObjects(selectedQuests),
                repeatingQuestPersistenceService.saveRemoteObjects(parsedRepeatingQuests))
                .compose(RxLifecycle.bindActivity(lifecycleSubject)).subscribe(ignored -> {
        }, error -> finish(), () -> {
            eventBus.post(new ScheduleRepeatingQuestsEvent());
            eventBus.post(new TutorialDoneEvent());
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifecycleSubject.onNext(ActivityEvent.START);
    }

    @Override
    public void onNextPressed() {
    }

    @Override
    public void onSlideChanged() {
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
        } else {
            eventBus.post(new SyncCalendarRequestEvent(EventSource.TUTORIAL));
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

}
