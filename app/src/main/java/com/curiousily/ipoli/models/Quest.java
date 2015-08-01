package com.curiousily.ipoli.models;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/30/15.
 */
public class Quest {
    public String name;
    public final int duration;
    public String time;
    public MaterialDrawableBuilder.IconValue icon;
    public final List<String> notes = new ArrayList<>();
    public Context context;

    public enum Context {
        Personal, Home, Work, Fun, Education, Wellness, Activity
    }

    public Quest(String name, String time, int duration, Context context) {
        this.name = name;
        this.time = time;
        this.duration = duration;
        this.context = context;
    }
}
