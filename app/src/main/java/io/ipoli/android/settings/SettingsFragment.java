package io.ipoli.android.settings;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.events.DailyChallengeStartTimeChangedEvent;
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

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.daily_challenge_notification)
    Switch dailyChallengeNotification;

    @BindView(R.id.daily_challenge_start_time)
    TextView dailyChallengeStartTime;

    @BindView(R.id.daily_challenge_days)
    TextView dailyChallengeDays;

    @BindView(R.id.daily_challenge_start_time_hint)
    TextView dailyChallengeStartTimeHint;

    private Unbinder unbinder;

    private LocalStorage localStorage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.title_fragment_settings);

        localStorage = LocalStorage.of(getContext());
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
        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    @OnClick(R.id.pick_avatar_container)
    public void onPickAvatarClicked(View view) {
        eventBus.post(new PickAvatarRequestEvent(EventSource.SETTINGS));
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
}