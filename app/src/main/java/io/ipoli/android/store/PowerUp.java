package io.ipoli.android.store;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/21/17.
 */
public enum PowerUp {
    REMINDERS(1, 300, R.string.reminders, R.string.power_up_reminders_sub_title,
            R.string.power_up_reminders_short_desc, R.string.power_up_reminders_long_desc, R.drawable.ic_reminders_white_24dp),
    
    CHALLENGES(3, 500, R.string.challenges, R.string.power_up_challenges_sub_title,
            R.string.power_up_challenges_short_desc, R.string.power_up_challenges_long_desc, R.drawable.ic_sword_white_24dp),

    CALENDAR_SYNC(4, 1000, R.string.settings_sync_google_calendars, R.string.power_up_sync_calendars_sub_title,
            R.string.power_up_sync_calendars_short_desc, R.string.power_up_sync_calendars_long_desc, R.drawable.ic_event_white_24dp),

    TIMER(5, 300, R.string.timer, R.string.power_up_timer_sub_title,
            R.string.power_up_timer_short_desc, R.string.power_up_timer_long_desc, R.drawable.ic_timer_white_24dp),

    GROWTH(6, 500, R.string.growth, R.string.power_up_growth_sub_title,
            R.string.power_up_growth_short_desc, R.string.power_up_growth_long_desc, R.drawable.ic_growth_white_24dp),

    EISENHOWER_MATRIX(7, 500, R.string.title_activity_eisenhower_matrix, R.string.power_up_matrix_sub_title,
            R.string.power_up_matrix_short_desc, R.string.power_up_matrix_long_desc, R.drawable.ic_matrix_white_24dp),

    PREDEFINED_CHALLENGES(8, 1000, R.string.programmed_challenges, R.string.power_up_predefined_challenges_sub_title,
            R.string.power_up_predefined_challenges_short_desc, R.string.power_up_predefined_challenges_long_desc,
            R.drawable.ic_sword_white_24dp, CHALLENGES),

    SUB_QUESTS(9, 400, R.string.sub_quests, R.string.power_up_sub_quests_sub_title,
            R.string.power_up_sub_quests_short_desc, R.string.power_up_sub_quests_long_desc, R.drawable.ic_format_list_bulleted_white_24dp),

    NOTES(10, 200, R.string.notes, R.string.power_up_notes_sub_title,
            R.string.power_up_notes_short_desc, R.string.power_up_notes_long_desc, R.drawable.ic_note_white_24dp),

    CUSTOM_DURATION(11, 300, R.string.custom_duration, R.string.power_up_custom_duration_sub_title,
            R.string.power_up_custom_duration_short_desc, R.string.power_up_custom_duration_long_desc, R.drawable.ic_timer_white_24dp);


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

    public final PowerUp requiredPowerUp;

    PowerUp(int code, int price, @StringRes int title, @StringRes int subTitle, @StringRes int shortDesc,
            @StringRes int longDesc, @DrawableRes int picture) {
        this(code, price, title, subTitle, shortDesc, longDesc, picture, null);
    }

    PowerUp(int code, int price, @StringRes int title, @StringRes int subTitle, @StringRes int shortDesc,
            @StringRes int longDesc, @DrawableRes int picture, PowerUp requiredPowerUp) {
        this.code = code;
        this.price = price;
        this.title = title;
        this.subTitle = subTitle;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.picture = picture;
        this.requiredPowerUp = requiredPowerUp;
    }

    public static PowerUp get(int code) {
        for (PowerUp powerUp : values()) {
            if (powerUp.code == code) {
                return powerUp;
            }
        }
        return null;
    }
}