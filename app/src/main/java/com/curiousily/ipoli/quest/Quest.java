package com.curiousily.ipoli.quest;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.utils.DateUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/30/15.
 */
public class Quest {
    public String name;
    public String description;
    public int duration;
    public MaterialDrawableBuilder.IconValue icon;
    public List<String> tags;
    public Context context;
    public QuestType type;
    public int timesPerDay;
    public String notes;
    public Set<Repeat> repeats = new HashSet<>();
    public Date due = DateUtils.getNow();

    public Quest() {
    }

    public enum Repeat {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    public enum QuestType {
        ONE_TIME, RECURRENT, HEROIC, EPIC, LEGENDARY
    }

    public enum Context {

        PERSONAL(MaterialDrawableBuilder.IconValue.ACCOUNT, R.color.md_orange_500, R.color.md_orange_700),
        HOME(MaterialDrawableBuilder.IconValue.HOME, R.color.md_pink_500, R.color.md_pink_700),
        WORK(MaterialDrawableBuilder.IconValue.BRIEFCASE, R.color.md_teal_500, R.color.md_teal_700),
        FUN(MaterialDrawableBuilder.IconValue.EMOTICON_HAPPY, R.color.md_purple_500, R.color.md_purple_700),
        EDUCATION(MaterialDrawableBuilder.IconValue.SCHOOL, R.color.md_blue_500, R.color.md_blue_700),
        WELLNESS(MaterialDrawableBuilder.IconValue.HEART, R.color.md_red_500, R.color.md_red_700),
        ACTIVITY(MaterialDrawableBuilder.IconValue.RUN, R.color.md_green_500, R.color.md_green_700);

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

    public Quest(String name, String description, int duration, Context context) {
        this(name, description, duration, context, new ArrayList<String>());
    }

    public Quest(String name, String description, int duration, Context context, List<String> tags) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.context = context;
        this.tags = tags;
    }
}
