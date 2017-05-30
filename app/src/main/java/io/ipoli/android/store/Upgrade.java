package io.ipoli.android.store;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/21/17.
 */

public enum Upgrade {
    CHALLENGES(1, 3, R.string.challenges,
            R.string.challenge1_desc, R.string.challenge2_desc, R.drawable.ic_sword_white_24dp),
    NOTES(2, 2, R.string.help_dialog_challenge_title,
            R.string.challenge1_desc, R.string.challenge2_desc, R.drawable.ic_note_white_24dp),
    REMINDERS(3, 1, R.string.help_dialog_challenge_title,
            R.string.challenge1_desc, R.string.challenge2_desc, R.drawable.ic_reminders_white_24dp),
    CALENDAR_SYNC(4, 5, R.string.help_dialog_challenge_title,
            R.string.challenge1_desc, R.string.challenge2_desc, R.drawable.ic_calendar_blank_grey_24dp),
    SUB_QUESTS(5, 3, R.string.help_dialog_challenge_title,
            R.string.challenge1_desc, R.string.challenge2_desc, R.drawable.ic_format_list_bulleted_white_24dp),
    REPEATING_QUESTS(6, 2, R.string.help_dialog_challenge_title,
            R.string.challenge1_desc, R.string.challenge2_desc, R.drawable.ic_repeat_white_24dp),
    TIMER(7, 2, R.string.help_dialog_challenge_title,
            R.string.challenge1_desc, R.string.challenge2_desc, R.drawable.ic_timer_white_24dp),
    CUSTOM_DURATION(8, 2, R.string.help_dialog_challenge_title,
            R.string.challenge1_desc, R.string.challenge2_desc, R.drawable.ic_today_white_24dp);

    public final int code;
    public final int price;

    @StringRes
    public final int title;

    @StringRes
    public final int shortDesc;

    @StringRes
    public final int longDesc;

    @DrawableRes
    public final int picture;

    Upgrade(int code, int price, @StringRes int title, @StringRes int shortDesc,
            @StringRes int longDesc, @DrawableRes int picture) {
        this.code = code;
        this.price = price;
        this.title = title;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.picture = picture;
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