package io.ipoli.android.quest.data;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/4/16.
 */
public enum Category {

    LEARNING(R.color.md_blue_50, R.color.md_blue_100, R.color.md_blue_300, R.color.md_blue_500, R.color.md_blue_700, R.color.md_blue_800, R.drawable.ic_context_learning_white_24dp, R.drawable.ic_context_learning_blue_24dp),
    WELLNESS(R.color.md_green_50, R.color.md_green_100, R.color.md_green_300, R.color.md_green_500, R.color.md_green_700, R.color.md_green_800, R.drawable.ic_context_wellness_white_24dp, R.drawable.ic_context_wellness_green_24dp),
    PERSONAL(R.color.md_orange_50, R.color.md_orange_100, R.color.md_orange_300, R.color.md_orange_500, R.color.md_orange_700, R.color.md_orange_800, R.drawable.ic_context_personal_white_24dp, R.drawable.ic_context_personal_orange_24dp),
    WORK(R.color.md_red_50, R.color.md_red_100, R.color.md_red_300, R.color.md_red_500, R.color.md_red_700, R.color.md_red_800, R.drawable.ic_context_work_white, R.drawable.ic_context_work_red),
    FUN(R.color.md_purple_50, R.color.md_purple_100, R.color.md_purple_300, R.color.md_purple_500, R.color.md_purple_700, R.color.md_purple_800, R.drawable.ic_context_fun_white, R.drawable.ic_context_fun_purple),
    CHORES(R.color.md_brown_50, R.color.md_brown_100, R.color.md_brown_300, R.color.md_brown_500, R.color.md_brown_700, R.color.md_brown_800, R.drawable.ic_context_chores_white, R.drawable.ic_context_chores_brown);

    @ColorRes
    public final int color50;

    @ColorRes
    public final int color100;

    @ColorRes
    public final int color300;

    @ColorRes
    public final int color500;

    @ColorRes
    public final int color700;

    @ColorRes
    public final int color800;

    @DrawableRes
    public final int whiteImage;

    @DrawableRes
    public final int colorfulImage;

    Category(@ColorRes int color50, @ColorRes int color100, @ColorRes int color300, @ColorRes int color500, @ColorRes int color700, int color800, int whiteImage, int colorfulImage) {
        this.color50 = color50;
        this.color100 = color100;
        this.color300 = color300;
        this.color500 = color500;
        this.color700 = color700;
        this.color800 = color800;
        this.whiteImage = whiteImage;
        this.colorfulImage = colorfulImage;
    }
}
