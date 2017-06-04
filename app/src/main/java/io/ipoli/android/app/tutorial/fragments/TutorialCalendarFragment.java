package io.ipoli.android.app.tutorial.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/17.
 */
public class TutorialCalendarFragment extends Fragment {

    @BindView(R.id.tutorial_calendar)
    CalendarDayView calendarDayView;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_calendar, container, false);
        unbinder = ButterKnife.bind(this, v);
        calendarDayView.hideTimeLine();
        calendarDayView.smoothScrollToTime(Time.atHours(13));
        boolean use24HourFormat = DateFormat.is24HourFormat(getContext());
        calendarDayView.setTimeFormat(use24HourFormat);

        new MaterialIntroView.Builder(getActivity())
                .enableDotAnimation(true)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.MINIMUM)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .performClick(true)
                .setInfoText("Hi There! Click this card and see what happens.")
                .setShape(ShapeType.RECTANGLE)
                .setTarget(v.findViewById(R.id.tutorial_quest_container))
                .setTargetPadding(10)
                .setListener(s -> {
                    PreferencesManager manager = new PreferencesManager(getContext());
                    manager.resetAll();
                })
                .show();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
