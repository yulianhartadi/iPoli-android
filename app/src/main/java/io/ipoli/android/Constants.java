package io.ipoli.android;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/15.
 */
public interface Constants {
    String FACEBOOK_APP_LINK = "https://fb.me/1609677589354576";
    String FACEBOOK_IPOLI_LOGO_URL = "https://scontent-vie1-1.xx.fbcdn.net/v/t1.0-1/p320x320/12805840_980283392053167_8375277495702561179_n.png?oh=fa2926a5ab2e10b227f283d21b670e83&oe=57ACFC48";
    String FACEBOOK_INVITE_IMAGE_URL = "https://scontent-vie1-1.xx.fbcdn.net/v/t1.0-9/13091967_1022897927791713_5208044433494824774_n.png?oh=e96baa01cf046a5164e8053102deb9aa&oe=57A4913D";
    String SHARE_URL = "http://bit.ly/ipoli-android";
    String TWITTER_USERNAME = "@iPoliHQ";

    String API_DATETIME_ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";

    int EDIT_QUEST_RESULT_REQUEST_CODE = 11;
    int REMIND_START_QUEST_NOTIFICATION_ID = 103;

    int QUEST_TIMER_NOTIFICATION_ID = 201;

    int QUEST_COMPLETE_NOTIFICATION_ID = 202;
    int DEFAULT_SNOOZE_TIME_MINUTES = 10;

    String DEFAULT_PLAYER_AVATAR = "avatar_12";

    int QUEST_WITH_NO_DURATION_TIMER_MINUTES = 30;
    int MAX_QUEST_DURATION_HOURS = 4;

    String QUEST_ID_EXTRA_KEY = "quest_id";
    String REPEATING_QUEST_ID_EXTRA_KEY = "repeating_quest_id";

    int CALENDAR_EVENT_MIN_DURATION = 15;
    int QUEST_MIN_DURATION = 10;

    int MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT = 3;
    int RESULT_REMOVED = 100;

    String KEY_APP_RUN_COUNT = "APP_RUN_COUNT";
    String KEY_APP_VERSION_CODE = "APP_VERSION_CODE";
    String KEY_PLAYER_REMOTE_ID = "PLAYER_REMOTE_ID";

    String KEY_SELECTED_ANDROID_CALENDARS = "SELECTED_ANDROID_CALENDARS";

    String KEY_REMOVED_QUESTS = "REMOVED_QUESTS";

    String IPOLI_EMAIL = "hi@ipoli.io";
    String KEY_SHOULD_SHOW_TUTORIAL = "SHOULD_SHOW_TUTORIAL";

    String API_RESOURCE_SOURCE = "ipoli-android";
    int MINIMUM_DELAY_SYNC_MINUTES = 5;
    int READ_CALENDAR_PERMISSION_REQUEST_CODE = 100;

    String SOURCE_ANDROID_CALENDAR = "android-calendar";
    int DEFAULT_PLAYER_XP = 0;
    int DEFAULT_PLAYER_LEVEL = 1;

    long DEFAULT_PLAYER_COINS = 10;

    String REWARD_ID_EXTRA_KEY = "reward_id";
    int AVATAR_COUNT = 12;
    String AVATAR_NAME_EXTRA_KEY = "avatar_name";
    String EXTERNAL_SOURCE_ANDROID_CALENDAR = "androidCalendar";

    int[] DURATIONS = {10, 15, 30, 45, 60, 90, 120, 180, 240};
    int DEFAULT_MIN_REWARD_PRICE = 100;
}