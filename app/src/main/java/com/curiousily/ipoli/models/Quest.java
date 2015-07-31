package com.curiousily.ipoli.models;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/30/15.
 */
public class Quest {
    public String name;
    public final int duration;
    public final int backgroundColor;
    public final int separatorColor;
    public String time;
    public MaterialDrawableBuilder.IconValue icon;
    public String snippet;


    public Quest(String name, String snippet, String time, int duration, int backgroundColor, int separatorColor, MaterialDrawableBuilder.IconValue icon) {
        this.name = name;
        this.snippet = snippet;
        this.time = time;
        this.duration = duration;
        this.backgroundColor = backgroundColor;
        this.separatorColor = separatorColor;
        this.icon = icon;
    }

    public Quest(String name, String time, int duration, int backgroundColor, int separatorColor, MaterialDrawableBuilder.IconValue icon) {
        this(name, "", time, duration, backgroundColor, separatorColor, icon);
    }
}
