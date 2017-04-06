package io.ipoli.android.app.tutorial.fragments;


import android.Manifest;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import me.everything.providers.android.calendar.Calendar;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SyncAndroidCalendarFragment extends Fragment implements ISlideBackgroundColorHolder, EasyPermissions.PermissionCallbacks {
    private static final int RC_CALENDAR_PERM = 101;
    @Inject
    Bus eventBus;

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
        syncCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->{});
//                eventBus.post(new SyncCalendarCheckTappedEvent(isChecked)));


        return contentView;
    }

    @AfterPermissionGranted(RC_CALENDAR_PERM)
    @OnClick(R.id.choose_google_calendars)
    public void onChooseCalendars(View v) {
        Log.d("AAAA", "Click");
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_CALENDAR)) {
            Log.d("AAAA", "yes");
            getCalendars();
        } else {
            EasyPermissions.requestPermissions(this, "", RC_CALENDAR_PERM, Manifest.permission.READ_CALENDAR);
        }
    }

    private void getCalendars() {
        List<Calendar> calendars = new SyncAndroidCalendarProvider(getContext()).getAndroidCalendars();
        for(Calendar c : calendars) {
            //visible?
            if(c.syncEvents == 1) {
                Log.d("AAA", c.displayName);
            }
        }
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
