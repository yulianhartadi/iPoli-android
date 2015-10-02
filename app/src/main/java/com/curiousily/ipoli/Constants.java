package com.curiousily.ipoli;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/15.
 */
public interface Constants {
    String ALERT_DIALOG_TAG = "alert_dialog";
    String DEFAULT_SERVER_DATE_FORMAT = "yyyy-MM-dd";
    String DEFAULT_UI_DATE_FORMAT = "dd-MM-yyyy";
    String DEFAULT_TIME_FORMAT = "HH:mm";
    String DEFAULT_SERVER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    String DATA_SHARING_KEY_QUEST = "quest";
    String KEY_USER_ID = "key_user_id";

    int[] DURATION_TEXT_INDEX_TO_MINUTES = new int[]{5, 10, 15, 20, 25, 30, 45, 60, 90, 120};

    int DAYS_IN_A_WEEK = 7;

    String ACTION_QUEST_DONE = "com.curiousily.ipoli.action.QUEST_DONE";
    String ACTION_QUEST_CANCELED = "com.curiousily.ipoli.action.QUEST_CANCELED";

    int QUEST_RUNNING_REQUEST_CODE = 5001;
    int QUEST_RUNNING_NOTIFICATION_ID = 101;
    int QUEST_DONE_NOTIFICATION_ID = 102;

    String DEFAULT_PLAN_DAY_TIME = "9:00";
    int REMIND_PLAN_DAY_NOTIFICATION_ID = 201;


}
