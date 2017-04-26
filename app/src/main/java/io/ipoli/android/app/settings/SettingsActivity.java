package io.ipoli.android.app.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.App;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.events.TimeFormatChangedEvent;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.app.settings.events.DailyChallengeDaysOfWeekChangedEvent;
import io.ipoli.android.app.settings.events.DailyChallengeReminderChangeEvent;
import io.ipoli.android.app.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.app.settings.events.EnableSynCalendarsEvent;
import io.ipoli.android.app.settings.events.MostProductiveTimesChangedEvent;
import io.ipoli.android.app.settings.events.OngoingNotificationChangeEvent;
import io.ipoli.android.app.settings.events.SleepHoursChangedEvent;
import io.ipoli.android.app.settings.events.WorkDaysChangedEvent;
import io.ipoli.android.app.settings.events.WorkHoursChangedEvent;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.tutorial.events.ShowTutorialEvent;
import io.ipoli.android.app.ui.dialogs.AndroidCalendarsPickerFragment;
import io.ipoli.android.app.ui.dialogs.DaysOfWeekPickerFragment;
import io.ipoli.android.app.ui.dialogs.LoadingDialog;
import io.ipoli.android.app.ui.dialogs.TimeIntervalPickerFragment;
import io.ipoli.android.app.ui.dialogs.TimeOfDayPickerFragment;
import io.ipoli.android.app.ui.dialogs.TimePickerFragment;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.events.PickAvatarRequestEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import me.everything.providers.android.calendar.Event;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SettingsActivity extends BaseActivity implements
        TimeOfDayPickerFragment.OnTimesOfDayPickedListener,
        TimePickerFragment.OnTimePickedListener,
        DaysOfWeekPickerFragment.OnDaysOfWeekPickedListener,
        EasyPermissions.PermissionCallbacks,
        LoaderManager.LoaderCallbacks<Void> {

    private static final int RC_CALENDAR_PERM = 102;
    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Inject
    CalendarPersistenceService calendarPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    AndroidCalendarEventParser androidCalendarEventParser;

    @Inject
    RepeatingQuestScheduler repeatingQuestScheduler;

    @Inject
    SyncAndroidCalendarProvider syncAndroidCalendarProvider;

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.ongoing_notification)
    Switch ongoingNotification;

    @BindView(R.id.time_format_current_time)
    TextView currentTime;

    @BindView(R.id.time_format)
    Switch timeFormat;

    @BindView(R.id.enable_sync_calendars)
    Switch enableSyncCalendars;

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

    @BindView(R.id.daily_challenge_start_time_hint)
    TextView dailyChallengeStartTimeHint;

    @BindView(R.id.daily_challenge_start_time)
    TextView dailyChallengeStartTime;

    @BindView(R.id.daily_challenge_days_hint)
    TextView dailyChallengeDaysHint;

    @BindView(R.id.daily_challenge_days)
    TextView dailyChallengeDays;

    @BindView(R.id.select_sync_calendars_hint)
    TextView selectSyncCalendarsHint;

    @BindView(R.id.selected_sync_calendars)
    TextView selectedSyncCalendars;

    @BindView(R.id.app_version)
    TextView appVersion;

    private LoadingDialog loadingDialog;
    private Map<Long, Category> selectedCalendars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Player player = getPlayer();

        initScheduling(player);
        initOngoingNotification();
        initTimeFormat(player);
        initSyncCalendars(player);
        initDailyChallenge();

        appVersion.setText(BuildConfig.VERSION_NAME);
        eventBus.post(new ScreenShownEvent(EventSource.SETTINGS));
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    private void initScheduling(Player player) {
        populateMostProductiveTimesOfDay(player.getMostProductiveTimesOfDayList());
        populateDaysOfWeekText(workDays, player.getWorkDays());
        populateTimeInterval(workHours, player.getWorkStartTime(), player.getWorkEndTime());
        populateTimeInterval(sleepHours, player.getSleepStartTime(), player.getSleepEndTime());
    }

    private void initTimeFormat(Player player) {
        currentTime.setText(Time.now().toString(player.getUse24HourFormat()));
        timeFormat.setChecked(player.getUse24HourFormat());
        timeFormat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentTime.setText(Time.now().toString(isChecked));
            eventBus.post(new TimeFormatChangedEvent(isChecked));
            player.setUse24HourFormat(isChecked);
            playerPersistenceService.save(player);
        });
    }

    private void initOngoingNotification() {
        ongoingNotification.setChecked(localStorage.readBool(Constants.KEY_ONGOING_NOTIFICATION_ENABLED, Constants.DEFAULT_ONGOING_NOTIFICATION_ENABLED));
        ongoingNotification.setOnCheckedChangeListener((compoundButton, b) -> {
            localStorage.saveBool(Constants.KEY_ONGOING_NOTIFICATION_ENABLED, ongoingNotification.isChecked());
            eventBus.post(new OngoingNotificationChangeEvent(ongoingNotification.isChecked()));
        });
    }

    private void initDailyChallenge() {
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
    }

    private CompoundButton.OnCheckedChangeListener onCheckSyncCalendarChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            eventBus.post(new EnableSynCalendarsEvent(true));
            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_CALENDAR)) {
                onSyncCalendarsSelected();
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.allow_read_calendars_perm_reason), RC_CALENDAR_PERM, Manifest.permission.READ_CALENDAR);
            }
        } else {
            showAlertSyncCalendarsDialog();
        }
    };

    private void showAlertSyncCalendarsDialog() {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_disable_google_calendar_sync_title))
                .setMessage(getString(R.string.dialog_disable_google_calendar_sync_message))
                .setPositiveButton(getString(R.string.dialog_yes), (dialog, which) -> {
                    eventBus.post(new EnableSynCalendarsEvent(false));
                    deleteSyncCalendars();
                })
                .setNegativeButton(getString(R.string.dialog_no), (dialog, which) -> {
                    enableSyncCalendars.setOnCheckedChangeListener(null);
                    enableSyncCalendars.setChecked(true);
                    enableSyncCalendars.setOnCheckedChangeListener(onCheckSyncCalendarChangeListener);
                })
                .create();
        d.show();
    }

    private void deleteSyncCalendars() {
        LoadingDialog loadingDialog = LoadingDialog.show(this, getString(R.string.sync_calendars_delete_loading_dialog_title), getString(R.string.sync_calendars_loading_dialog_message));
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                calendarPersistenceService.deleteAllCalendarsSync(getPlayer());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
                onSyncCalendarsChanged(0);
            }
        }.execute();
    }

    private void initSyncCalendars(Player player) {
        int calendarsCount = player.getAndroidCalendars().size();
        enableSyncCalendars.setChecked(calendarsCount > 0);
        enableSyncCalendars.setOnCheckedChangeListener(onCheckSyncCalendarChangeListener);
        onSyncCalendarsChanged(calendarsCount);
    }

    @AfterPermissionGranted(RC_CALENDAR_PERM)
    private void onSyncCalendarsSelected() {
        onSyncCalendarsChanged(0);
    }


    private void populateSelectedSyncCalendarsText(int calendarsCount) {
        String syncText = calendarsCount == 0 ? getString(R.string.no_calendars_selected_to_sync) :
                String.format(getString(R.string.sync_calendars_count), calendarsCount);
        selectedSyncCalendars.setText(syncText);
    }

    @OnClick(R.id.pick_avatar_container)
    public void onPickAvatarClicked(View view) {
        eventBus.post(new PickAvatarRequestEvent(EventSource.SETTINGS));
    }

    @OnClick(R.id.ongoing_notification_container)
    public void onOngoingNotificationClicked(View view) {
        ongoingNotification.setChecked(!ongoingNotification.isChecked());
    }

    @OnClick(R.id.time_format_container)
    public void onTimeFormatClicked(View view) {
        timeFormat.setChecked(!timeFormat.isChecked());
    }

    @OnClick(R.id.show_tutorial_container)
    public void onShowTutorialClicked(View view) {
        eventBus.post(new ShowTutorialEvent());
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.most_productive_time_container)
    public void onMostProductiveTimeClicked(View view) {
        Player player = getPlayer();
        TimeOfDayPickerFragment fragment = TimeOfDayPickerFragment.newInstance(R.string.time_of_day_picker_title, player.getMostProductiveTimesOfDayList(), this);
        fragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.work_days_container)
    public void onWorkDaysClicked(View view) {
        Player player = getPlayer();
        DaysOfWeekPickerFragment fragment = DaysOfWeekPickerFragment.newInstance(R.string.work_days_picker_title, new HashSet<>(player.getWorkDays()),
                selectedDays -> {
                    eventBus.post(new WorkDaysChangedEvent(selectedDays));
                    player.setWorkDays(new ArrayList<>(selectedDays));
                    playerPersistenceService.save(player);
                    populateDaysOfWeekText(workDays, player.getWorkDays());
                });
        fragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.work_hours_container)
    public void onWorkHoursClicked(View view) {
        Player player = getPlayer();
        TimeIntervalPickerFragment fragment = TimeIntervalPickerFragment.newInstance(R.string.work_hours_dialog_title,
                player.getWorkStartTime(), player.getWorkEndTime(), (startTime, endTime) -> {
                    eventBus.post(new WorkHoursChangedEvent(startTime, endTime));
                    player.setWorkStartTime(startTime);
                    player.setWorkEndTime(endTime);
                    playerPersistenceService.save(player);
                    populateTimeInterval(workHours, player.getWorkStartTime(), player.getWorkEndTime());
                });
        fragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.sleep_hours_container)
    public void onSleepHoursClicked(View view) {
        Player player = getPlayer();
        TimeIntervalPickerFragment fragment = TimeIntervalPickerFragment.newInstance(R.string.sleep_hours_dialog_title,
                player.getSleepStartTime(), player.getSleepEndTime(), (startTime, endTime) -> {
                    eventBus.post(new SleepHoursChangedEvent(startTime, endTime));
                    player.setSleepStartTime(startTime);
                    player.setSleepEndTime(endTime);
                    playerPersistenceService.save(player);
                    populateTimeInterval(sleepHours, player.getSleepStartTime(), player.getSleepEndTime());
                });
        fragment.show(getSupportFragmentManager());
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
            fragment.show(getSupportFragmentManager());
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
        if (!dailyChallengeNotification.isChecked()) {
            return;
        }
        Set<Integer> selectedDays = localStorage.readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
        DaysOfWeekPickerFragment fragment = DaysOfWeekPickerFragment.newInstance(R.string.challenge_days_question, selectedDays, this);
        fragment.show(getSupportFragmentManager());
    }

    private void onDailyChallengeNotificationChanged() {
        if (dailyChallengeNotification.isChecked()) {
            dailyChallengeStartTimeHint.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_87));
            dailyChallengeStartTime.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_54));
            dailyChallengeDaysHint.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_87));
            dailyChallengeDays.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_54));
        } else {
            dailyChallengeStartTimeHint.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_26));
            dailyChallengeStartTime.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_26));
            dailyChallengeDaysHint.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_26));
            dailyChallengeDays.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_26));
        }
    }

    private void onSyncCalendarsChanged(int calendarsCount) {
        populateSelectedSyncCalendarsText(calendarsCount);
        if (calendarsCount > 0 || enableSyncCalendars.isChecked()) {
            selectSyncCalendarsHint.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_87));
            selectedSyncCalendars.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_54));
        } else {
            selectSyncCalendarsHint.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_26));
            selectedSyncCalendars.setTextColor(ContextCompat.getColor(this, R.color.md_dark_text_26));
        }
    }

    @OnClick(R.id.select_sync_calendars_container)
    public void onSelectSyncCalendarsClicked(View v) {
        if (!enableSyncCalendars.isChecked()) {
            return;
        }
        Player player = getPlayer();
        AndroidCalendarsPickerFragment fragment = AndroidCalendarsPickerFragment.newInstance(R.string.choose_calendars_title, player.getAndroidCalendars(), this::onSelectCalendarsToSync);
        fragment.show(getSupportFragmentManager());
    }

    private void onSelectCalendarsToSync(Map<Long, Category> selectedCalendars) {
        populateSelectedSyncCalendarsText(selectedCalendars.size());
        loadingDialog = LoadingDialog.show(this, getString(R.string.sync_calendars_loading_dialog_title), getString(R.string.sync_calendars_loading_dialog_message));
        this.selectedCalendars = selectedCalendars;
        if (!selectedCalendars.isEmpty()) {
            eventBus.post(new SyncCalendarRequestEvent(selectedCalendars, EventSource.SETTINGS));
        }
        getSupportLoaderManager().initLoader(1, null, this);
    }

    @OnClick(R.id.sync_calendars_container)
    public void onSyncCalendarsClicked(View view) {
        enableSyncCalendars.setChecked(!enableSyncCalendars.isChecked());
    }

    @Override
    public void onTimesOfDayPicked(List<TimeOfDay> selectedTimes) {
        Player player = getPlayer();
        eventBus.post(new MostProductiveTimesChangedEvent(selectedTimes));
        if (selectedTimes.contains(TimeOfDay.ANY_TIME) || selectedTimes.isEmpty()) {
            selectedTimes = new ArrayList<>(Arrays.asList(TimeOfDay.ANY_TIME));
        }
        player.setMostProductiveTimesOfDayList(selectedTimes);
        playerPersistenceService.save(player);
        populateMostProductiveTimesOfDay(selectedTimes);
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
        localStorage.saveInt(Constants.KEY_DAILY_CHALLENGE_REMINDER_START_MINUTE, time.toMinuteOfDay());
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
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent linkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(linkToMarket);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        enableSyncCalendars.setOnCheckedChangeListener(null);
        enableSyncCalendars.setChecked(false);
        enableSyncCalendars.setOnCheckedChangeListener(onCheckSyncCalendarChangeListener);
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        return new CalendarLoader(this, selectedCalendars, getPlayer(), syncAndroidCalendarProvider, androidCalendarEventParser, repeatingQuestScheduler, calendarPersistenceService);
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {

    }

    public static class CalendarLoader extends AsyncTaskLoader<Void> {

        private Map<Long, Category> selectedCalendars;
        private Player player;
        private SyncAndroidCalendarProvider syncAndroidCalendarProvider;
        private AndroidCalendarEventParser androidCalendarEventParser;
        private RepeatingQuestScheduler repeatingQuestScheduler;
        private CalendarPersistenceService calendarPersistenceService;

        public CalendarLoader(Context context, Map<Long, Category> selectedCalendars, Player player, SyncAndroidCalendarProvider syncAndroidCalendarProvider, AndroidCalendarEventParser androidCalendarEventParser, RepeatingQuestScheduler repeatingQuestScheduler, CalendarPersistenceService calendarPersistenceService) {
            super(context);
            this.selectedCalendars = selectedCalendars;
            this.player = player;
            this.syncAndroidCalendarProvider = syncAndroidCalendarProvider;
            this.androidCalendarEventParser = androidCalendarEventParser;
            this.repeatingQuestScheduler = repeatingQuestScheduler;
            this.calendarPersistenceService = calendarPersistenceService;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Void loadInBackground() {
            Set<Long> calendarsToAdd = getCalendarsToAdd(selectedCalendars, player.getAndroidCalendars().keySet());
            List<Quest> quests = new ArrayList<>();
            Map<Quest, Long> questToOriginalId = new HashMap<>();
            List<RepeatingQuest> repeatingQuests = new ArrayList<>();
            for (Long calendarId : calendarsToAdd) {
                List<Event> events = syncAndroidCalendarProvider.getCalendarEvents(calendarId);
                AndroidCalendarEventParser.Result result = androidCalendarEventParser.parse(events, selectedCalendars.get(calendarId));
                quests.addAll(result.quests);
                questToOriginalId.putAll(result.questToOriginalId);
                repeatingQuests.addAll(result.repeatingQuests);
            }

            Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests = new HashMap<>();
            for (RepeatingQuest rq : repeatingQuests) {
                repeatingQuestToQuests.put(rq, repeatingQuestScheduler.schedule(rq, LocalDate.now()));
            }

            Set<Long> calendarsToRemove = getCalendarsToRemove(selectedCalendars, player.getAndroidCalendars().keySet());
            Map<Long, Category> calendarsToUpdate = getCalendarsToUpdate(selectedCalendars, player.getAndroidCalendars());

            player.setAndroidCalendars(selectedCalendars);
            calendarPersistenceService.updateSync(player, quests, questToOriginalId, repeatingQuestToQuests, calendarsToRemove, calendarsToUpdate);
            return null;
        }

        @NonNull
        private Set<Long> getCalendarsToRemove(Map<Long, Category> selectedCalendars, Set<Long> playerCalendars) {
            Set<Long> calendarsToRemove = new HashSet<>();
            for (Long calendarId : playerCalendars) {
                if (!selectedCalendars.containsKey(calendarId)) {
                    calendarsToRemove.add(calendarId);
                }
            }
            return calendarsToRemove;
        }

        @NonNull
        private Set<Long> getCalendarsToAdd(Map<Long, Category> selectedCalendars, Set<Long> playerCalendars) {
            Set<Long> calendarsToAdd = new HashSet<>();
            for (Long calendarId : selectedCalendars.keySet()) {
                if (!playerCalendars.contains(calendarId)) {
                    calendarsToAdd.add(calendarId);
                }
            }
            return calendarsToAdd;
        }

        @NonNull
        private Map<Long, Category> getCalendarsToUpdate(Map<Long, Category> selectedCalendars, Map<Long, Category> playerCalendars) {
            Map<Long, Category> calendarsToUpdate = new HashMap<>();
            for (Long calendarId : selectedCalendars.keySet()) {
                if (playerCalendars.keySet().contains(calendarId)) {
                    if (selectedCalendars.get(calendarId) != playerCalendars.get(calendarId)) {
                        calendarsToUpdate.put(calendarId, selectedCalendars.get(calendarId));
                    }
                }
            }
            return calendarsToUpdate;
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }
    }
}
