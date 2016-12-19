package io.ipoli.android.app.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.settings.events.DailyChallengeDaysOfWeekChangedEvent;
import io.ipoli.android.app.settings.events.DailyChallengeReminderChangeEvent;
import io.ipoli.android.app.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.app.settings.events.MostProductiveTimesChangedEvent;
import io.ipoli.android.app.settings.events.OngoingNotificationChangeEvent;
import io.ipoli.android.app.settings.events.SleepHoursChangedEvent;
import io.ipoli.android.app.settings.events.WorkDaysChangedEvent;
import io.ipoli.android.app.settings.events.WorkHoursChangedEvent;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.tutorial.events.ShowTutorialEvent;
import io.ipoli.android.app.ui.dialogs.DaysOfWeekPickerFragment;
import io.ipoli.android.app.ui.dialogs.TimeIntervalPickerFragment;
import io.ipoli.android.app.ui.dialogs.TimeOfDayPickerFragment;
import io.ipoli.android.app.ui.dialogs.TimePickerFragment;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.avatar.TimeOfDay;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.player.events.PickAvatarRequestEvent;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/21/16.
 */
public class SettingsFragment extends BaseFragment implements
        TimeOfDayPickerFragment.OnTimesOfDayPickedListener,
        TimePickerFragment.OnTimePickedListener,
        DaysOfWeekPickerFragment.OnDaysOfWeekPickedListener, OnDataChangedListener<Avatar> {

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Inject
    AvatarPersistenceService avatarPersistenceService;

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.ongoing_notification)
    Switch ongoingNotification;

    @BindView(R.id.most_productive_time)
    TextView mostProductiveTime;

    @BindView(R.id.work_days)
    TextView workDays;

    @BindView(R.id.work_hours)
    TextView workHours;

    @BindView(R.id.sleep_hours)
    TextView sleepHours;

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
    private Avatar avatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.title_fragment_settings);

        avatarPersistenceService.listen(this);

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
        populateDaysOfWeekText(dailyChallengeDays, new ArrayList<>(selectedDays));

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
        avatarPersistenceService.removeAllListeners();
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

    @OnClick(R.id.most_productive_time_container)
    public void onMostProductiveTimeClicked(View view) {
        TimeOfDayPickerFragment fragment = TimeOfDayPickerFragment.newInstance(R.string.time_of_day_picker_title, avatar.getMostProductiveTimesOfDayList(), this);
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.work_days_container)
    public void onWorkDaysClicked(View view) {
        DaysOfWeekPickerFragment fragment = DaysOfWeekPickerFragment.newInstance(R.string.work_days_picker_title, new HashSet<>(avatar.getWorkDays()),
                selectedDays -> {
                    eventBus.post(new WorkDaysChangedEvent(selectedDays));
                    avatar.setWorkDays(new ArrayList<>(selectedDays));
                    avatarPersistenceService.save(avatar);
                });
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.work_hours_container)
    public void onWorkHoursClicked(View view) {
        TimeIntervalPickerFragment fragment = TimeIntervalPickerFragment.newInstance(R.string.work_hours_dialog_title,
                avatar.getWorkStartTime(), avatar.getWorkEndTime(), (startTime, endTime) -> {
                    eventBus.post(new WorkHoursChangedEvent(startTime, endTime));
                    avatar.setWorkStartTime(startTime);
                    avatar.setWorkEndTime(endTime);
                    avatarPersistenceService.save(avatar);
                });
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.sleep_hours_container)
    public void onSleepHoursClicked(View view) {
        TimeIntervalPickerFragment fragment = TimeIntervalPickerFragment.newInstance(R.string.sleep_hours_dialog_title,
                avatar.getSleepStartTime(), avatar.getSleepEndTime(), (startTime, endTime) -> {
                    eventBus.post(new SleepHoursChangedEvent(startTime, endTime));
                    avatar.setSleepStartTime(startTime);
                    avatar.setSleepEndTime(endTime);
                    avatarPersistenceService.save(avatar);
                });
        fragment.show(getFragmentManager());
    }

    private void populateTimeInterval(TextView textView, Time startTime, Time endTime) {
        if (startTime == null || endTime == null) {
            return;
        }
        textView.setText(startTime.toString() + " - " + endTime.toString());
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
        DaysOfWeekPickerFragment fragment = DaysOfWeekPickerFragment.newInstance(R.string.challenge_days_question, selectedDays, this);
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
    public void onTimesOfDayPicked(List<TimeOfDay> selectedTimes) {
        eventBus.post(new MostProductiveTimesChangedEvent(selectedTimes));
        if(selectedTimes.contains(TimeOfDay.ANY_TIME) || selectedTimes.isEmpty()) {
            selectedTimes = new ArrayList<>(Arrays.asList(TimeOfDay.ANY_TIME));
        }
        avatar.setMostProductiveTimesOfDayList(selectedTimes);
        avatarPersistenceService.save(avatar);
    }

    private void populateMostProductiveTimesOfDay(List<TimeOfDay> selectedTimes) {
        List<String> timeNames = new ArrayList<>();
        for (TimeOfDay timeOfDay : selectedTimes) {
            timeNames.add(StringUtils.capitalizeAndReplaceUnderscore(timeOfDay.name()));
        }
        mostProductiveTime.setText(TextUtils.join(", ", timeNames));
    }

    @Override
    public void onTimePicked(Time time) {
        dailyChallengeStartTime.setText(time.toString());
        localStorage.saveInt(Constants.KEY_DAILY_CHALLENGE_REMINDER_START_MINUTE, time.toMinutesAfterMidnight());
        eventBus.post(new DailyChallengeStartTimeChangedEvent(time));
    }

    @Override
    public void onDaysOfWeekPicked(Set<Integer> selectedDays) {
        populateDaysOfWeekText(dailyChallengeDays, new ArrayList<>(selectedDays));
        localStorage.saveIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, selectedDays);
        eventBus.post(new DailyChallengeDaysOfWeekChangedEvent(selectedDays));
    }

    private void populateDaysOfWeekText(TextView textView, List<Integer> selectedDays) {
        List<String> dayNames = new ArrayList<>();
        for (Constants.DaysOfWeek dayOfWeek : Constants.DaysOfWeek.values()) {
            if (selectedDays.contains(dayOfWeek.getIsoOrder())) {
                dayNames.add(StringUtils.capitalize(dayOfWeek.name()).substring(0, 3));
            }
        }
        if (dayNames.isEmpty()) {
            textView.setText(R.string.no_challenge_days);
        } else {
            textView.setText(TextUtils.join(", ", dayNames));
        }
    }

    @OnClick(R.id.rate_container)
    public void onRateClicked(View v) {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent linkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(linkToMarket);
    }


    @Override
    public void onDataChanged(Avatar avatar) {
        this.avatar = avatar;
        populateMostProductiveTimesOfDay(avatar.getMostProductiveTimesOfDayList());
        populateDaysOfWeekText(workDays, avatar.getWorkDays());
        populateTimeInterval(workHours, avatar.getWorkStartTime(), avatar.getWorkEndTime());
        populateTimeInterval(sleepHours, avatar.getSleepStartTime(), avatar.getSleepEndTime());
        rootContainer.setVisibility(View.VISIBLE);
    }
}