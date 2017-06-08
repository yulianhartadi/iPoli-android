package io.ipoli.android.store;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/21/17.
 */

public enum Upgrade {
    REMINDERS(1, 300, R.string.reminders, R.string.upgrade_reminders_sub_title,
            R.string.upgrade_reminders_short_desc, R.string.upgrade_reminders_long_desc, R.drawable.ic_reminders_white_24dp),

    REPEATING_QUESTS(2, 500, R.string.repeating_quests, R.string.upgrade_rq_sub_title,
            R.string.upgrade_rq_short_desc, R.string.upgrade_rq_long_desc, R.drawable.ic_repeat_white_24dp),

    CHALLENGES(3, 500, R.string.challenges, R.string.upgrade_challenges_sub_title,
            R.string.upgrade_challenges_short_desc, R.string.upgrade_challenges_long_desc, R.drawable.ic_sword_white_24dp),

    CALENDAR_SYNC(4, 1000, R.string.settings_sync_google_calendars, R.string.upgrade_sync_calendars_sub_title,
            R.string.upgrade_sync_calendars_short_desc, R.string.upgrade_sync_calendars_long_desc, R.drawable.ic_event_white_24dp),

    TIMER(5, 300, R.string.timer, R.string.upgrade_timer_sub_title,
            R.string.upgrade_timer_short_desc, R.string.upgrade_timer_long_desc, R.drawable.ic_timer_white_24dp),

    GROWTH(6, 500, R.string.growth, R.string.upgrade_growth_sub_title,
            R.string.upgrade_growth_short_desc, R.string.upgrade_growth_long_desc, R.drawable.ic_growth_white_24dp),

    EISENHOWER_MATRIX(7, 500, R.string.title_activity_eisenhower_matrix, R.string.upgrade_matrix_sub_title,
            R.string.upgrade_matrix_short_desc, R.string.upgrade_matrix_long_desc, R.drawable.ic_matrix_white_24dp),

    PREDEFINED_CHALLENGES(8, 1000, R.string.programmed_challenges, R.string.upgrade_predefined_challenges_sub_title,
            R.string.upgrade_predefined_challenges_short_desc, R.string.upgrade_predefined_challenges_long_desc,
            R.drawable.ic_sword_white_24dp, CHALLENGES),

    SUB_QUESTS(9, 400, R.string.sub_quests, R.string.upgrade_sub_quests_sub_title,
            R.string.upgrade_sub_quests_short_desc, R.string.upgrade_sub_quests_long_desc, R.drawable.ic_format_list_bulleted_white_24dp),

    NOTES(10, 200, R.string.notes, R.string.upgrade_notes_sub_title,
            R.string.upgrade_notes_short_desc, R.string.upgrade_notes_long_desc, R.drawable.ic_note_white_24dp),

    CUSTOM_DURATION(11, 300, R.string.custom_duration, R.string.upgrade_custom_duration_sub_title,
            R.string.upgrade_custom_duration_short_desc, R.string.upgrade_custom_duration_long_desc, R.drawable.ic_timer_white_24dp);



    public final int code;
    public final int price;

    @StringRes
    public final int title;

    @StringRes
    public final int subTitle;

    @StringRes
    public final int shortDesc;

    @StringRes
    public final int longDesc;

    @DrawableRes
    public final int picture;

    public final Upgrade requiredUpgrade;

    Upgrade(int code, int price, @StringRes int title, @StringRes int subTitle, @StringRes int shortDesc,
            @StringRes int longDesc, @DrawableRes int picture) {
        this(code, price, title, subTitle, shortDesc, longDesc, picture, null);
    }

    Upgrade(int code, int price, @StringRes int title, @StringRes int subTitle, @StringRes int shortDesc,
            @StringRes int longDesc, @DrawableRes int picture, Upgrade requiredUpgrade) {
        this.code = code;
        this.price = price;
        this.title = title;
        this.subTitle = subTitle;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.picture = picture;
        this.requiredUpgrade = requiredUpgrade;
    }

    public static Upgrade get(int code) {
        for (Upgrade upgrade : values()) {
            if (upgrade.code == code) {
                return upgrade;
            }
        }
        return null;
    }
}