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

import java.util.HashMap;
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

public class SyncAndroidCalendarFragment extends Fragment implements ISlideBackgroundColorHolder {
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

    private Map<Long, Category> selectedCalendars = new HashMap<>();

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

    @OnClick(R.id.choose_google_calendars)
    public void onChooseCalendars(View v) {
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_CALENDAR)) {
            chooseCalendars();
        } else {
            EasyPermissions.requestPermissions(this, "", RC_CALENDAR_PERM, Manifest.permission.READ_CALENDAR);
        }
    }

    @AfterPermissionGranted(RC_CALENDAR_PERM)
    public void chooseCalendars() {
        AndroidCalendarsPickerFragment fragment = AndroidCalendarsPickerFragment.newInstance(R.string.choose_calendars_title, selectedCalendars -> {
            this.selectedCalendars = selectedCalendars;
        });
        fragment.show(getFragmentManager());
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

    public Map<Long, Category> getSelectedCalendars() {
        return selectedCalendars;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return ContextCompat.getColor(getContext(), R.color.md_green_500);
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        contentView.setBackgroundColor(backgroundColor);
    }

}
