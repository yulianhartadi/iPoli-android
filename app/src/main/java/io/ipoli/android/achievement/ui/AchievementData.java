package io.ipoli.android.achievement.ui;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Class that holds the data to be displayed
 * Created by Darkion Avey
 */
public class AchievementData {
    private String title = "", subtitle;
    private Drawable icon;
    private int textColor = 0xff000000, backgroundColor = 0xffffffff, iconBackgroundColor = 0x0;
    private View.OnClickListener onClickListener;
    AchievementIconView.AchievementIconViewStates state = null;

    public AchievementData setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public static AchievementData copyFrom(AchievementData data) {
        AchievementData result = new AchievementData();
        result.setTitle(data.getTitle());
        result.setSubtitle(data.getSubtitle());
        result.setIcon(data.getIcon());
        result.setState(data.getState());
        result.setBackgroundColor(data.getBackgroundColor());
        result.setIconBackgroundColor(data.getIconBackgroundColor());
        result.setTextColor(data.getTextColor());
        result.setPopUpOnClickListener(data.getPopUpOnClickListener());

        return result;
    }

    View.OnClickListener getPopUpOnClickListener() {
        return onClickListener;
    }

    AchievementData setPopUpOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    int getTextColor() {
        return textColor;
    }

    public AchievementData setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    String getTitle() {
        return title;
    }

    public AchievementData setTitle(String title) {
        this.title = title;
        return this;
    }

    String getSubtitle() {
        return subtitle;
    }

    public AchievementIconView.AchievementIconViewStates getState() {
        return state;
    }

    public void setState(AchievementIconView.AchievementIconViewStates state) {
        this.state = state;
    }

    Drawable getIcon() {

        return icon;
    }

    public AchievementData setIcon(Drawable icon) {
        this.icon = icon;
        return this;
    }

    int getBackgroundColor() {
        return backgroundColor;
    }

    public AchievementData setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    int getIconBackgroundColor() {
        return iconBackgroundColor;
    }

    public AchievementData setIconBackgroundColor(int iconBackgroundColor) {
        this.iconBackgroundColor = iconBackgroundColor;
        return this;
    }
}