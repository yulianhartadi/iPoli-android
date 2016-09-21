package io.ipoli.android.app.tutorial.fragments;


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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.tutorial.events.SyncCalendarCheckTappedEvent;

public class SyncAndroidCalendarFragment extends Fragment implements ISlideBackgroundColorHolder {
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
        syncCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                eventBus.post(new SyncCalendarCheckTappedEvent(isChecked)));
        return contentView;
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
}
