package io.ipoli.android;

import org.threeten.bp.DayOfWeek;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.player.data.Avatar;
import io.ipoli.android.player.data.PetAvatar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/15.
 */
public interface Constants {

    String FACEBOOK_APP_LINK = "https://fb.me/1609677589354576";
    String IPOLI_LOGO_URL = "https://i.imgur.com/Gz3rUi1.png";
    String INVITE_IMAGE_URL = "https://i.imgur.com/fLToavB.png";
    String SHARE_URL = "http://bit.ly/ipoli-android";
    String TWITTER_USERNAME = "@iPoliHQ";

    int REMIND_DAILY_CHALLENGE_NOTIFICATION_ID = 101;
    int ONGOING_NOTIFICATION_ID = 102;
    int MEMBERSHIP_EXPIRATION_NOTIFICATION_ID = 103;

    int QUEST_TIMER_NOTIFICATION_ID = 201;
    int QUEST_COMPLETE_NOTIFICATION_ID = 202;

    int PET_STATE_CHANGED_NOTIFICATION_ID = 301;

    int DEFAULT_SNOOZE_TIME_MINUTES = 10;

    int QUEST_WITH_NO_DURATION_TIMER_MINUTES = 30;
    int MAX_QUEST_DURATION_HOURS = 4;

    String PLAYER_ID_EXTRA_KEY = "player_id";

    String PROFILE_ID_EXTRA_KEY = "profile_id";

    String QUEST_ID_EXTRA_KEY = "quest_id";

    String REPEATING_QUEST_ID_EXTRA_KEY = "repeating_quest_id";

    String CHALLENGE_ID_EXTRA_KEY = "challenge_id";

    String REWARD_ID_EXTRA_KEY = "reward_id";

    String CURRENT_SELECTED_DAY_EXTRA_KEY = "CURRENT_SELECTED_DAY";

    String DISPLAY_NAME_EXTRA_KEY = "display_name";

    String SHOW_TRIAL_MESSAGE_EXTRA_KEY = "show_trial_message";

    int CALENDAR_EVENT_MIN_DURATION = 15;

    int CALENDAR_EVENT_MIN_SINGLE_LINE_DURATION = 20;

    int CALENDAR_EVENT_MIN_TWO_LINES_DURATION = 30;

    int QUEST_MIN_DURATION = 10;

    int MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT = 3;
    int RESULT_REMOVED = 100;

    String KEY_APP_RUN_COUNT = "APP_RUN_COUNT";

    String KEY_APP_VERSION_CODE = "APP_VERSION_CODE";

    String KEY_PLAYER_ID = "PLAYER_ID";

    String KEY_DAILY_CHALLENGE_DAYS = "DAILY_CHALLENGE_DAYS";

    String KEY_DAILY_CHALLENGE_REMINDER_START_MINUTE = "DAILY_CHALLENGE_REMINDER_START_MINUTE";

    String KEY_DAILY_CHALLENGE_ENABLE_REMINDER = "DAILY_CHALLENGE_ENABLE_REMINDER";

    String KEY_DAILY_CHALLENGE_LAST_COMPLETED = "DAILY_CHALLENGE_LAST_COMPLETED";

    String KEY_ONGOING_NOTIFICATION_ENABLED = "ONGOING_NOTIFICATION_ENABLED";

    String KEY_SHOULD_SHOW_TUTORIAL = "SHOULD_SHOW_TUTORIAL";

    String IPOLI_EMAIL = "hi@ipoli.io";

    String API_RESOURCE_SOURCE = "ipoli-android";
    int DEFAULT_PLAYER_XP = 20;
    int DEFAULT_PLAYER_LEVEL = 1;
    long DEFAULT_PLAYER_COINS = 10;
    Avatar DEFAULT_PLAYER_AVATAR = Avatar.IPOLI_CLASSIC;
    Set<TimeOfDay> DEFAULT_PLAYER_PRODUCTIVE_TIMES = new HashSet<>(Collections.singletonList(TimeOfDay.MORNING));
    int DEFAULT_PLAYER_WORK_START_MINUTE = 10 * 60;
    int DEFAULT_PLAYER_WORK_END_MINUTE = 18 * 60;
    int DEFAULT_PLAYER_SLEEP_START_MINUTE = 23 * 60;
    int DEFAULT_PLAYER_SLEEP_END_MINUTE = 8 * 60;
    int DEFAULT_PLAYER_COMPLETE_DAILY_QUESTS_MINUTE = 0;

    Set<Integer> DEFAULT_PLAYER_WORK_DAYS = new HashSet<>(Arrays.asList(
            DayOfWeek.MONDAY.getValue(),
            DayOfWeek.TUESDAY.getValue(),
            DayOfWeek.WEDNESDAY.getValue(),
            DayOfWeek.THURSDAY.getValue(),
            DayOfWeek.FRIDAY.getValue()
    ));

    Integer[] DURATIONS = {10, 15, 25, 30, 45, 60, 90, 120};

    Integer[] REWARD_COINS = {10, 20, 50, 100, 200, 500, 1000};

    int DEFAULT_REWARD_PRICE = 10;

    int REWARD_MAX_PRICE = 10000;

    int REWARD_MIN_PRICE = 1;

    int DEFAULT_DAILY_CHALLENGE_REMINDER_START_MINUTE = 10 * 60;

    boolean DEFAULT_DAILY_CHALLENGE_ENABLE_REMINDER = true;

    boolean DEFAULT_ONGOING_NOTIFICATION_ENABLED = true;
    int[] REMINDER_PREDEFINED_MINUTES = new int[]{0, 10, 15, 30, 60};
    int MIN_FLEXIBLE_TIMES_A_WEEK_COUNT = 1;
    int MAX_FLEXIBLE_TIMES_A_WEEK_COUNT = 6;

    int MIN_FLEXIBLE_TIMES_A_MONTH_COUNT = 1;
    int MAX_FLEXIBLE_TIMES_A_MONTH_COUNT = 15;

    Set<Integer> DEFAULT_DAILY_CHALLENGE_DAYS = new HashSet<>(Arrays.asList(
            DayOfWeek.MONDAY.getValue(),
            DayOfWeek.TUESDAY.getValue(),
            DayOfWeek.WEDNESDAY.getValue(),
            DayOfWeek.THURSDAY.getValue(),
            DayOfWeek.FRIDAY.getValue()
    ));

    int DAILY_CHALLENGE_QUEST_COUNT = 3;
    int DEFAULT_BAR_COUNT = 4;
    String REMINDER_START_TIME = "reminder_start_time";

    String QUICK_ADD_ADDITIONAL_TEXT = "quick_add_additional_text";
    String DEFAULT_PET_NAME = "Flopsy";
    PetAvatar DEFAULT_PET_AVATAR = PetAvatar.ELEPHANT;
    String DEFAULT_PET_BACKGROUND_PICTURE = "pet_background_1";

    Integer DEFAULT_PET_HP = 80;
    double XP_BONUS_PERCENTAGE_OF_HP = 20.0;
    double COINS_BONUS_PERCENTAGE_OF_HP = 10.0;
    int MAX_PET_COIN_BONUS = 10;

    int MAX_PET_XP_BONUS = 20;

    double XP_TO_PET_HP_RATIO = 13.2;
    int REVIVE_PET_COST = 300;
    String PREDEFINED_CHALLENGE_INDEX = "predefined_challenge_index";
    int RANDOM_SEED = 42; // duh!
    int MAX_TIMES_A_DAY_COUNT = 8;
    int SCHEMA_VERSION = 9;

    double MAX_PENALTY_COEFFICIENT = 0.5;
    double NO_QUESTS_PENALTY_COEFFICIENT = 0.3;
    double IMPORTANT_QUEST_PENALTY_PERCENT = 5;

    String KEY_WIDGET_AGENDA_QUEST_LIST = "widget_agenda_quest_list";
    int API_READ_TIMEOUT_SECONDS = 30;
    String DEFAULT_VIEW_VERSION = "1.0";
    String SOURCE_ANDROID_CALENDAR = "android-calendar";

    int XP_BAR_MAX_VALUE = 100;
    int RC_CALENDAR_PERM = 102;
    String KEY_LAST_ANDROID_CALENDAR_SYNC_DATE = "LAST_ANDROID_CALENDAR_SYNC_DATE";
    String FACEBOOK_PACKAGE = "com.facebook.katana";
    String TWITTER_PACKAGE = "com.twitter.android";
    int SYNC_CALENDAR_JOB_ID = 1;
    int PROFILES_FIRST_SCHEMA_VERSION = 7;

    int POWER_UP_GRACE_PERIOD_DAYS = 7;
    int POWER_UPS_TRIAL_PERIOD_DAYS = 15;

    String SKU_SUBSCRIPTION_MONTHLY = "monthly_plan_70_percent";
    String SKU_SUBSCRIPTION_QUARTERLY = "quarterly_plan_70_percent";
    String SKU_SUBSCRIPTION_YEARLY = "yearly_plan_70_percent";

    String KEY_ACHIEVEMENT_ACTION = "achievement_action";
    String KEY_ACHIEVEMENT_ACTION_CLASS = "achievement_action_class";

    String NOTIFICATION_CHANNEL_ID = "iPoli";
    String NOTIFICATION_CHANNEL_NAME = "iPoli";
}