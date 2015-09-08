package com.curiousily.ipoli;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/15.
 */
public interface Constants {
    String ALERT_DIALOG_TAG = "alert_dialog";
    String DEFAULT_SERVER_DATE_FORMAT = "yyyy-MM-dd";
    String DEFAULT_UI_DATE_FORMAT = "dd-MM-yyyy";
    String DEFAULT_TIME_FORMAT = "HH:mm";
    String DEFAULT_SERVER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    String DATA_SHARING_KEY_QUEST = "quest";
    String KEY_USER_ID = "key_user_id";

    int[] DURATION_TEXT_INDEX_TO_MINUTES = new int[]{1, 2, 3, 5, 10, 15, 20, 25, 30, 45, 60, 90, 120};

    int DAYS_IN_A_WEEK = 7;
}
