package io.ipoli.android.tutorial.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.tutorial.events.SyncCalendarCheckTappedEvent;

public class SyncAndroidCalendarFragment extends Fragment {
    @Inject
    Bus eventBus;

    @BindView(R.id.sync_calendar)
    CheckBox syncCheckBox;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync_google_calendar, container, false);
        App.getAppComponent(getContext()).inject(this);
        unbinder = ButterKnife.bind(this, v);
        syncCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                eventBus.post(new SyncCalendarCheckTappedEvent(isChecked)));
        return v;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    public boolean isSyncCalendarChecked() {
        return syncCheckBox.isChecked();
    }
}
