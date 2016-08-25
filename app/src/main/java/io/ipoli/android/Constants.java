package io.ipoli.android;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/15.
 */
public interface Constants {

    String API_VERSION = "v0";

    String FACEBOOK_APP_LINK = "https://fb.me/1609677589354576";
    String FACEBOOK_IPOLI_LOGO_URL = "https://scontent-vie1-1.xx.fbcdn.net/v/t1.0-1/p320x320/12805840_980283392053167_8375277495702561179_n.png?oh=fa2926a5ab2e10b227f283d21b670e83&oe=57ACFC48";
    String FACEBOOK_INVITE_IMAGE_URL = "https://scontent-vie1-1.xx.fbcdn.net/v/t1.0-9/13091967_1022897927791713_5208044433494824774_n.png?oh=e96baa01cf046a5164e8053102deb9aa&oe=57A4913D";
    String SHARE_URL = "http://bit.ly/ipoli-android";
    String TWITTER_USERNAME = "@iPoliHQ";

    int EDIT_QUEST_RESULT_REQUEST_CODE = 11;

    int REMIND_DAILY_CHALLENGE_NOTIFICATION_ID = 101;
    int ONGOING_NOTIFICATION_ID = 102;

    int QUEST_TIMER_NOTIFICATION_ID = 201;
    int QUEST_COMPLETE_NOTIFICATION_ID = 202;

    int DEFAULT_SNOOZE_TIME_MINUTES = 10;

    int QUEST_WITH_NO_DURATION_TIMER_MINUTES = 30;
    int MAX_QUEST_DURATION_HOURS = 4;
    String QUEST_ID_EXTRA_KEY = "quest_id";
    String REPEATING_QUEST_ID_EXTRA_KEY = "repeating_quest_id";
    String QUEST_IDS_EXTRA_KEY = "quest_ids";
    String REMINDER_NOTIFICATION_ID_EXTRA_KEY = "reminder_notification_id";

    String CHALLENGE_ID_EXTRA_KEY = "challenge_id";

    String REWARD_ID_EXTRA_KEY = "reward_id";
    int CALENDAR_EVENT_MIN_DURATION = 15;

    int CALENDAR_EVENT_MIN_SINGLE_LINE_DURATION = 20;

    int QUEST_MIN_DURATION = 10;

    int MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT = 3;
    int RESULT_REMOVED = 100;

    String KEY_APP_RUN_COUNT = "APP_RUN_COUNT";

    String KEY_APP_VERSION_CODE = "APP_VERSION_CODE";

    String KEY_PLAYER_ID = "PLAYER_ID";

    String KEY_SELECTED_ANDROID_CALENDARS = "SELECTED_ANDROID_CALENDARS";
    String KEY_DAILY_CHALLENGE_DAYS = "DAILY_CHALLENGE_DAYS";

    String KEY_DAILY_CHALLENGE_REMINDER_START_MINUTE = "DAILY_CHALLENGE_REMINDER_START_MINUTE";

    String KEY_DAILY_CHALLENGE_ENABLE_REMINDER = "DAILY_CHALLENGE_ENABLE_REMINDER";

    String KEY_DAILY_CHALLENGE_LAST_COMPLETED = "DAILY_CHALLENGE_LAST_COMPLETED";

    String KEY_ONGOING_NOTIFICATION_ENABLED = "ONGOING_NOTIFICATION_ENABLED";

    String KEY_SHOULD_SHOW_TUTORIAL = "SHOULD_SHOW_TUTORIAL";

    String IPOLI_EMAIL = "hi@ipoli.io";

    String API_RESOURCE_SOURCE = "ipoli-android";
    int READ_CALENDAR_PERMISSION_REQUEST_CODE = 100;
    String SOURCE_ANDROID_CALENDAR = "android-calendar";

    int DEFAULT_PLAYER_XP = 0;
    int DEFAULT_AVATAR_LEVEL = 1;
    long DEFAULT_PLAYER_COINS = 10;
    String DEFAULT_PLAYER_PICTURE = "avatar_12";

    int AVATAR_COUNT = 12;

    String PICTURE_NAME_EXTRA_KEY = "avatar_name";
    String EXTERNAL_SOURCE_ANDROID_CALENDAR = "androidCalendar";

    int[] DURATIONS = {10, 15, 20, 30, 60, 90, 120, 180, 240};

    int DEFAULT_MIN_REWARD_PRICE = 100;

    int DEFAULT_DAILY_CHALLENGE_REMINDER_START_MINUTE = 10 * 60;

    boolean DEFAULT_DAILY_CHALLENGE_ENABLE_REMINDER = true;

    boolean DEFAULT_ONGOING_NOTIFICATION_ENABLED = true;

    int[] REMINDER_PREDEFINED_MINUTES = new int[]{0, 10, 15, 30, 60};
    int MIN_FLEXIBLE_TIMES_A_WEEK_COUNT = 1;
    int MAX_FLEXIBLE_TIMES_A_WEEK_COUNT = 6;
    int MIN_FLEXIBLE_TIMES_A_MONTH_COUNT = 1;

    int MAX_FLEXIBLE_TIMES_A_MONTH_COUNT = 15;
    Set<Integer> DEFAULT_DAILY_CHALLENGE_DAYS = new HashSet<>(Arrays.asList(new Integer[]{
            DaysOfWeek.MONDAY.getIsoOrder(),
            DaysOfWeek.TUESDAY.getIsoOrder(),
            DaysOfWeek.WEDNESDAY.getIsoOrder(),
            DaysOfWeek.THURSDAY.getIsoOrder(),
            DaysOfWeek.FRIDAY.getIsoOrder()
    }));
    int DAILY_CHALLENGE_QUEST_COUNT = 3;

    int DEFAULT_CHALLENGE_DEADLINE_DAY_DURATION = 30;
    int DEFAULT_BAR_COUNT = 4;
    String WIDGET_AGENDA_QUESTS = "widget_agenda_quests";
    String REMINDER_START_TIME = "reminder_start_time";
    String QUICK_ADD_ADDITIONAL_TEXT = "quick_add_additional_text";

    String DEFAULT_PET_NAME = "iPoli";
    String DEFAULT_PET_AVATAR = "pet_1";
    String DEFAULT_PET_BACKGROUND_IMAGE = "pet_background_1";
    Integer DEFAULT_PET_HP = 100;
    Integer DEFAULT_PET_XP_BONUS = 10;
    Integer DEFAULT_PET_COINS_BONUS = 5;

    String KEY_XP_BONUS_PERCENTAGE = "XP_BONUS";
    String KEY_COINS_BONUS_PERCENTAGE = "COINS_BONUS";
    double COINS_BONUS_PERCENTAGE_OF_HP = 20.0;
    double XP_BONUS_PERCENTAGE_OF_HP = 10.0;


    enum DaysOfWeek {
        SUNDAY(7), MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6);

        private final int order;

        DaysOfWeek(int order) {
            this.order = order;
        }

        public int getIsoOrder() {
            return order;
        }
    }
}