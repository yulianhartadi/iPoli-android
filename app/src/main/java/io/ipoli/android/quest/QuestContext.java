package io.ipoli.android.quest;

import android.support.annotation.ColorRes;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/4/16.
 */
public enum QuestContext {
    PERSONAL(R.color.md_blue_500),
    WORK(R.color.md_red_500),
    WELLNESS(R.color.md_green_500),
    FUN(R.color.md_purple_500),
    LEARNING(R.color.md_orange_500),
    CHORES(R.color.md_blue_grey_500);
    
    @ColorRes
    public final int resColor;

    QuestContext(@ColorRes int resColor) {
        this.resColor = resColor;
    }
}
