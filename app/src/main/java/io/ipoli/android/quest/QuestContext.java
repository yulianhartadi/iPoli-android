package io.ipoli.android.quest;

import android.support.annotation.ColorRes;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/4/16.
 */
public enum QuestContext {
    PERSONAL(R.color.md_blue_500, R.color.md_blue_700),
    WELLNESS(R.color.md_green_500, R.color.md_green_700),
    LEARNING(R.color.md_orange_500, R.color.md_orange_700),
    WORK(R.color.md_red_500, R.color.md_red_700),
    FUN(R.color.md_purple_500, R.color.md_purple_700),
    CHORES(R.color.md_blue_grey_500, R.color.md_blue_grey_700);

    @ColorRes
    public final int resLightColor;

    @ColorRes
    public final int resDarkColor;

    QuestContext(@ColorRes int resLightColor, @ColorRes int resDarkColor) {
        this.resLightColor = resLightColor;
        this.resDarkColor = resDarkColor;
    }
}
