package com.curiousily.ipoli.quest;

import com.curiousily.ipoli.R;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/30/15.
 */
public class Quest {
    public String name;
    public String description;
    public final int duration;
    public String time;
    public MaterialDrawableBuilder.IconValue icon;
    public final List<String> journal = new ArrayList<>();
    public final List<String> tags;
    public Context context;

    public enum Context {

        Personal(MaterialDrawableBuilder.IconValue.ACCOUNT, R.color.md_orange_500, R.color.md_orange_700),
        Home(MaterialDrawableBuilder.IconValue.HOME, R.color.md_pink_500, R.color.md_pink_700),
        Work(MaterialDrawableBuilder.IconValue.BRIEFCASE, R.color.md_teal_500, R.color.md_teal_700),
        Fun(MaterialDrawableBuilder.IconValue.EMOTICON_HAPPY, R.color.md_purple_500, R.color.md_purple_700),
        Education(MaterialDrawableBuilder.IconValue.SCHOOL, R.color.md_blue_500, R.color.md_blue_700),
        Wellness(MaterialDrawableBuilder.IconValue.HEART, R.color.md_green_500, R.color.md_green_700),
        Activity(MaterialDrawableBuilder.IconValue.RUN, R.color.md_red_500, R.color.md_red_700);

        private final MaterialDrawableBuilder.IconValue icon;
        private final int primaryColor;
        private final int primaryColorDark;

        Context(MaterialDrawableBuilder.IconValue icon, int primaryColor, int primaryColorDark) {
            this.icon = icon;
            this.primaryColor = primaryColor;
            this.primaryColorDark = primaryColorDark;
        }

        public MaterialDrawableBuilder.IconValue getIcon() {
            return icon;
        }

        public int getPrimaryColor() {
            return primaryColor;

        }

        public int getSecondaryColor() {
            return primaryColorDark;
        }

    }

    public Quest(String name, String description, String time, int duration, Context context) {
        this(name, description, time, duration, context, new ArrayList<String>());
    }

    public Quest(String name, String description, String time, int duration, Context context, List<String> tags) {
        this.name = name;
        this.description = description;
        this.time = time;
        this.duration = duration;
        this.context = context;
        this.tags = tags;
    }
}
