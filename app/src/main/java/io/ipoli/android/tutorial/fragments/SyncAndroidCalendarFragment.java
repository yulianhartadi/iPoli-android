package io.ipoli.android.tutorial.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.LocalStorage;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;

public class SyncAndroidCalendarFragment extends Fragment {
    private static final int READ_CALENDAR_REQUEST_CODE = 100;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync_google_calendar, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CALENDAR},
                    READ_CALENDAR_REQUEST_CODE);
        } else {
            syncWithCalendar();
        }
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_CALENDAR_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                syncWithCalendar();
            }
        }
    }

    private void syncWithCalendar() {
        CalendarProvider provider = new CalendarProvider(getContext());
        List<Calendar> calendars = provider.getCalendars().getList();
        LocalStorage localStorage = LocalStorage.of(getContext());
        Set<String> calendarIds = new HashSet<>();
        for (Calendar c : calendars) {
            if (c.visible) {
                calendarIds.add(String.valueOf(c.id));
            }
        }
        localStorage.saveStringSet(Constants.KEY_CALENDARS_TO_SYNC, calendarIds);
        localStorage.saveStringSet(Constants.KEY_SELECTED_ANDROID_CALENDARS, calendarIds);
    }
}
