package io.ipoli.android.app.tutorial;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FinishTutorialActivityEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.tutorial.events.TutorialSkippedEvent;
import io.ipoli.android.app.tutorial.fragments.SyncAndroidCalendarFragment;
import io.ipoli.android.app.tutorial.fragments.TutorialFragment;
import io.ipoli.android.quest.data.Category;
import pub.devrel.easypermissions.EasyPermissions;

public class TutorialActivity extends AppIntro2 implements EasyPermissions.PermissionCallbacks {
    private static final int RC_CALENDAR_PERM = 102;

    @Inject
    Bus eventBus;

    private SyncAndroidCalendarFragment syncAndroidCalendarFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(this).inject(this);

        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_welcome_title),
                getString(R.string.tutorial_hero_desc),
                R.drawable.tutorial_welcome,
                R.color.md_blue_700));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_challenges_title),
                getString(R.string.tutorial_challenges_desc),
                R.drawable.tutorial_challenge,
                R.color.md_light_blue_500));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_pet_title),
                getString(R.string.tutorial_pet_desc),
                R.drawable.tutorial_pet,
                R.color.md_orange_700));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_schedule_title),
                getString(R.string.tutorial_schedule_desc),
                R.drawable.tutorial_schedule,
                R.color.md_green_500));

        syncAndroidCalendarFragment = new SyncAndroidCalendarFragment();
        addSlide(syncAndroidCalendarFragment);

        setImmersiveMode(true, true);
        setColorTransitionsEnabled(true);
        showSkipButton(false);
    }

    @Override
    public void onDonePressed(Fragment fragment) {
        doneButton.setVisibility(View.GONE);
        if (syncAndroidCalendarFragment.isSyncCalendarChecked()) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_CALENDAR)) {
                requestSynCalendar();
            } else {
                EasyPermissions.requestPermissions(this, "", RC_CALENDAR_PERM, Manifest.permission.READ_CALENDAR);
            }
        } else {
            onFinish();
        }
    }

    private void requestSynCalendar() {
        Map<Long, Category> selectedCalendars = syncAndroidCalendarFragment.getSelectedCalendars();
        if(!selectedCalendars.isEmpty()) {
            eventBus.post(new SyncCalendarRequestEvent(selectedCalendars, EventSource.TUTORIAL));
        }
        onFinish();
    }

    @Override
    public void onBackPressed() {
        eventBus.post(new TutorialSkippedEvent());
        onFinish();
    }

    private void onFinish() {
        eventBus.post(new FinishTutorialActivityEvent());
        finish();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if(requestCode == RC_CALENDAR_PERM) {
            requestSynCalendar();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if(requestCode == RC_CALENDAR_PERM) {
            onFinish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}
