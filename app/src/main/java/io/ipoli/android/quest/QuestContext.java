package io.ipoli.android.quest;

import android.support.annotation.ColorRes;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/4/16.
 */
public enum QuestContext {
    PERSONAL(R.color.md_blue_500, R.color.md_blue_700, R.color.md_blue_800),
    WELLNESS(R.color.md_green_500, R.color.md_green_700, R.color.md_green_800),
    WORK(R.color.md_orange_500, R.color.md_orange_700, R.color.md_orange_800),
    LEARNING(R.color.md_red_500, R.color.md_red_700, R.color.md_red_800),
    FUN(R.color.md_purple_500, R.color.md_purple_700, R.color.md_purple_800),
    CHORES(R.color.md_blue_grey_500, R.color.md_blue_grey_700, R.color.md_blue_grey_800);

    @ColorRes
    public final int resLightColor;

    @ColorRes
    public final int resDarkColor;

    @ColorRes
    public final int resDarkerColor;

    QuestContext(@ColorRes int resLightColor, @ColorRes int resDarkColor, int resDarkerColor) {
        this.resLightColor = resLightColor;
        this.resDarkColor = resDarkColor;
        this.resDarkerColor = resDarkerColor;
    }
}
