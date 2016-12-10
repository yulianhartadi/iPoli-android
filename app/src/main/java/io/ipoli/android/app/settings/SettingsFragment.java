package io.ipoli.android.app.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.CalendarPermissionResponseEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.settings.events.DailyChallengeDaysOfWeekChangedEvent;
import io.ipoli.android.app.settings.events.DailyChallengeReminderChangeEvent;
import io.ipoli.android.app.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.app.settings.events.OngoingNotificationChangeEvent;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.tutorial.events.ShowTutorialEvent;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.player.events.PickAvatarRequestEvent;
import io.ipoli.android.quest.ui.dialogs.DaysOfWeekPickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/21/16.
 */
public class SettingsFragment extends BaseFragment implements TimePickerFragment.OnTimePickedListener, DaysOfWeekPickerFragment.OnDaysOfWeekPickedListener {

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.ongoing_notification)
    Switch ongoingNotification;

    @BindView(R.id.daily_challenge_notification)
    Switch dailyChallengeNotification;

    @BindView(R.id.daily_challenge_start_time)
    TextView dailyChallengeStartTime;

    @BindView(R.id.daily_challenge_days)
    TextView dailyChallengeDays;

    @BindView(R.id.daily_challenge_start_time_hint)
    TextView dailyChallengeStartTimeHint;

    @BindView(R.id.app_version)
    TextView appVersion;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.title_fragment_settings);

        ongoingNotification.setChecked(localStorage.readBool(Constants.KEY_ONGOING_NOTIFICATION_ENABLED, Constants.DEFAULT_ONGOING_NOTIFICATION_ENABLED));
        ongoingNotification.setOnCheckedChangeListener((compoundButton, b) -> {
            localStorage.saveBool(Constants.KEY_ONGOING_NOTIFICATION_ENABLED, ongoingNotification.isChecked());
            eventBus.post(new OngoingNotificationChangeEvent(ongoingNotification.isChecked()));
        });


        boolean isReminderEnabled = localStorage.readBool(Constants.KEY_DAILY_CHALLENGE_ENABLE_REMINDER, Constants.DEFAULT_DAILY_CHALLENGE_ENABLE_REMINDER);
        dailyChallengeNotification.setChecked(isReminderEnabled);
        int startMinute = localStorage.readInt(Constants.KEY_DAILY_CHALLENGE_REMINDER_START_MINUTE, Constants.DEFAULT_DAILY_CHALLENGE_REMINDER_START_MINUTE);
        dailyChallengeStartTime.setText(Time.of(startMinute).toString());
        dailyChallengeNotification.setOnCheckedChangeListener((compoundButton, b) -> {
            localStorage.saveBool(Constants.KEY_DAILY_CHALLENGE_ENABLE_REMINDER, dailyChallengeNotification.isChecked());
            onDailyChallengeNotificationChanged();
        });
        onDailyChallengeNotificationChanged();
        Set<Integer> selectedDays = localStorage.readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
        populateDaysOfWeekText(selectedDays);

        appVersion.setText(BuildConfig.VERSION_NAME);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_pick_daily_challenge_quests).setVisible(false);
        menu.findItem(R.id.action_help).setVisible(false);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @OnClick(R.id.pick_avatar_container)
    public void onPickAvatarClicked(View view) {
        eventBus.post(new PickAvatarRequestEvent(EventSource.SETTINGS));
    }

    @OnClick(R.id.ongoing_notification_container)
    public void onOngoingNotificationClicked(View view) {
        ongoingNotification.setChecked(!ongoingNotification.isChecked());
    }

    @OnClick(R.id.show_tutorial_container)
    public void onShowTutorialClicked(View view) {
        eventBus.post(new ShowTutorialEvent());
        Intent intent = new Intent(getContext(), TutorialActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.sync_calendars_container)
    public void onSyncCalendarsClicked(View view) {
        checkForCalendarPermission();
    }

    @OnClick(R.id.daily_challenge_start_time_container)
    public void onDailyChallengeStartTimeClicked(View view) {
        if (dailyChallengeNotification.isChecked()) {
            TimePickerFragment fragment = TimePickerFragment.newInstance(false, this);
            fragment.show(getFragmentManager());
        }
    }

    @OnClick(R.id.daily_challenge_notification_container)
    public void onDailyChallengeNotificationClicked(View view) {
        dailyChallengeNotification.setChecked(!dailyChallengeNotification.isChecked());
        localStorage.saveBool(Constants.KEY_DAILY_CHALLENGE_ENABLE_REMINDER, dailyChallengeNotification.isChecked());
        onDailyChallengeNotificationChanged();
        eventBus.post(new DailyChallengeReminderChangeEvent(dailyChallengeNotification.isChecked()));
    }

    @OnClick(R.id.daily_challenge_days_container)
    public void onDailyChallengeDaysClicked(View view) {
        Set<Integer> selectedDays = localStorage.readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
        DaysOfWeekPickerFragment fragment = DaysOfWeekPickerFragment.newInstance(selectedDays, this);
        fragment.show(getFragmentManager());
    }

    private void onDailyChallengeNotificationChanged() {
        if (dailyChallengeNotification.isChecked()) {
            dailyChallengeStartTimeHint.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_87));
            dailyChallengeStartTime.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        } else {
            dailyChallengeStartTimeHint.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_26));
            dailyChallengeStartTime.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_26));
        }
    }

    @Override
    public void onTimePicked(Time time) {
        dailyChallengeStartTime.setText(time.toString());
        localStorage.saveInt(Constants.KEY_DAILY_CHALLENGE_REMINDER_START_MINUTE, time.toMinutesAfterMidnight());
        eventBus.post(new DailyChallengeStartTimeChangedEvent(time));
    }

    @Override
    public void onDaysOfWeekPicked(Set<Integer> selectedDays) {
        populateDaysOfWeekText(selectedDays);
        localStorage.saveIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, selectedDays);
        eventBus.post(new DailyChallengeDaysOfWeekChangedEvent(selectedDays));
    }

    private void populateDaysOfWeekText(Set<Integer> selectedDays) {
        List<String> dayNames = new ArrayList<>();
        for (Constants.DaysOfWeek dayOfWeek : Constants.DaysOfWeek.values()) {
            if (selectedDays.contains(dayOfWeek.getIsoOrder())) {
                dayNames.add(StringUtils.capitalize(dayOfWeek.name()).substring(0, 3));
            }
        }
        if (dayNames.isEmpty()) {
            dailyChallengeDays.setText(R.string.no_challenge_days);
        } else {
            dailyChallengeDays.setText(TextUtils.join(", ", dayNames));
        }
    }

    private void checkForCalendarPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CALENDAR},
                    Constants.READ_CALENDAR_PERMISSION_REQUEST_CODE);
        } else {
            eventBus.post(new SyncCalendarRequestEvent(EventSource.SETTINGS));
            Toast.makeText(getActivity(), R.string.import_calendar_events_started, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.READ_CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                eventBus.post(new CalendarPermissionResponseEvent(CalendarPermissionResponseEvent.Response.GRANTED, EventSource.OPTIONS_MENU));
                eventBus.post(new SyncCalendarRequestEvent(EventSource.TUTORIAL));
                Toast.makeText(getActivity(), R.string.import_calendar_events_started, Toast.LENGTH_SHORT).show();
            } else if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                eventBus.post(new CalendarPermissionResponseEvent(CalendarPermissionResponseEvent.Response.DENIED, EventSource.OPTIONS_MENU));
            }
        }
    }

    @OnClick(R.id.rate_container)
    public void onRateClicked(View v) {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent linkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(linkToMarket);
    }
}