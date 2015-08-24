package com.curiousily.ipoli.quest.viewmodel;

import com.curiousily.ipoli.quest.Quest;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/15.
 */
public class QuestViewModel {
    public String name;
    public String description;
    public String startTime;
    public String context;
    public int backgroundColor;
    public String tags;
    public String notes;
    public MaterialDrawableBuilder.IconValue icon;
    public List<SubQuestViewModel> subQuests = new ArrayList<>();
}
