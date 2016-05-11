package io.ipoli.android.tutorial.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.tutorial.events.SyncGoogleCalendarEvent;

public class SyncGoogleCalendarFragment extends Fragment {
    @Inject
    Bus eventBus;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync_google_calendar, container, false);
        unbinder = ButterKnife.bind(this, v);
        App.getAppComponent(getContext()).inject(this);

        eventBus.post(new SyncGoogleCalendarEvent());

        return v;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
