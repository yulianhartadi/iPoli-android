package io.ipoli.android.app.tutorial.fragments;


import android.Manifest;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.App;
import io.ipoli.android.app.ui.dialogs.AndroidCalendarsPickerFragment;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SyncAndroidCalendarFragment extends Fragment implements ISlideBackgroundColorHolder, EasyPermissions.PermissionCallbacks {
    private static final int RC_CALENDAR_PERM = 101;
    @Inject
    Bus eventBus;

    @Inject
    AndroidCalendarEventParser eventParser;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @BindView(R.id.sync_calendar)
    CheckBox syncCheckBox;

    private Unbinder unbinder;
    private int backgroundColor;
    private View contentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_sync_google_calendar, container, false);
        App.getAppComponent(getContext()).inject(this);
        unbinder = ButterKnife.bind(this, contentView);
        syncCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
        });
//                eventBus.post(new SyncCalendarCheckTappedEvent(isChecked)));


        return contentView;
    }

    @AfterPermissionGranted(RC_CALENDAR_PERM)
    @OnClick(R.id.choose_google_calendars)
    public void onChooseCalendars(View v) {
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_CALENDAR)) {
            getCalendars();
        } else {
            EasyPermissions.requestPermissions(this, "", RC_CALENDAR_PERM, Manifest.permission.READ_CALENDAR);
        }
    }

    private void getCalendars() {
        AndroidCalendarsPickerFragment fragment = AndroidCalendarsPickerFragment.newInstance(R.string.fragment_calendar_title, new AndroidCalendarsPickerFragment.OnCalendarsPickedListener() {
            @Override
            public void onCalendarsPicked(Map<Long, Category> selectedCalendars) {

            }
        });
        fragment.show(getFragmentManager());
//        SyncAndroidCalendarProvider calendarProvider = new SyncAndroidCalendarProvider(getContext());
//        List<Calendar> calendars = calendarProvider.getAndroidCalendars();
//        List<Long> chosenCalendarIds = new ArrayList<>();
//        for (Calendar c : calendars) {
//            if (c.syncEvents == 1) {
//                Log.d("AAA", c.displayName);
//                chosenCalendarIds.add(c.id);
//            }
//        }
//
//        List<Quest> quests = new ArrayList<>();
//        List<RepeatingQuest> repeatingQuests = new ArrayList<>();
//
//        for (long id : chosenCalendarIds) {
//            List<Event> events = calendarProvider.getCalendarEvents(id);
//            Pair<List<Quest>, List<RepeatingQuest>> result = eventParser.parse(events);
//            quests.addAll(result.first);
//            repeatingQuests.addAll(result.second);
//        }
//
//        Log.d("AAA", "single " + quests.size());
//        Log.d("AAA", "repeating " + repeatingQuests.size());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    public boolean isSyncCalendarChecked() {
        return syncCheckBox.isChecked();
    }

    @Override
    public int getDefaultBackgroundColor() {
        return ContextCompat.getColor(getContext(), R.color.md_green_500);
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        contentView.setBackgroundColor(backgroundColor);
    }

    @Override
    public void onPermissionsGranted(int i, List<String> list) {
        getCalendars();
    }

    @Override
    public void onPermissionsDenied(int i, List<String> list) {

    }
}
