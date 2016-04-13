package io.ipoli.android.quest;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/4/16.
 */
public enum QuestContext {

    LEARNING(R.color.md_blue_50, R.color.md_blue_500, R.color.md_blue_700, R.color.md_blue_800, R.drawable.ic_context_learning_white_24dp, R.drawable.ic_context_learning_blue_24dp),
    WELLNESS(R.color.md_green_50, R.color.md_green_500, R.color.md_green_700, R.color.md_green_800, R.drawable.ic_context_wellness_white_24dp, R.drawable.ic_context_wellness_green_24dp),
    PERSONAL(R.color.md_orange_50, R.color.md_orange_500, R.color.md_orange_700, R.color.md_orange_800, R.drawable.ic_context_personal_white_24dp, R.drawable.ic_context_personal_orange_24dp),
    WORK(R.color.md_red_50, R.color.md_red_500, R.color.md_red_700, R.color.md_red_800, R.drawable.ic_context_work_white, R.drawable.ic_context_work_red),
    FUN(R.color.md_purple_50, R.color.md_purple_500, R.color.md_purple_700, R.color.md_purple_800, R.drawable.ic_context_fun_white, R.drawable.ic_context_fun_purple),
    CHORES(R.color.md_brown_50, R.color.md_brown_500, R.color.md_brown_700, R.color.md_brown_800, R.drawable.ic_context_chores_white, R.drawable.ic_context_chores_brown);

    @ColorRes
    public final int backgroundColor;

    @ColorRes
    public final int resLightColor;

    @ColorRes
    public final int resDarkColor;

    @ColorRes
    public final int resDarkerColor;

    @DrawableRes
    public final int whiteImage;

    @DrawableRes
    public final int colorfulImage;

    QuestContext(int backgroundColor, @ColorRes int resLightColor, @ColorRes int resDarkColor, int resDarkerColor, int whiteImage, int colorfulImage) {
        this.backgroundColor = backgroundColor;
        this.resLightColor = resLightColor;
        this.resDarkColor = resDarkColor;
        this.resDarkerColor = resDarkerColor;
        this.whiteImage = whiteImage;
        this.colorfulImage = colorfulImage;
    }
}
