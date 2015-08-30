package com.curiousily.ipoli.quest.viewmodel;

import android.text.TextUtils;

import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.SubQuest;

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
    public int duration;
    public int backgroundColor;
    public String tags;
    public String notes;
    public MaterialDrawableBuilder.IconValue icon;
    public List<SubQuestViewModel> subQuests = new ArrayList<>();

    public static QuestViewModel from(Quest quest) {
        QuestViewModel q = new QuestViewModel();
        q.name = quest.name;
        q.description = quest.description;
        q.duration = quest.duration;
        q.context = quest.context.name();
        q.backgroundColor = quest.context.getPrimaryColor();
        q.icon = quest.context.getIcon();
        q.tags = TextUtils.join(", ", quest.tags);
        for (SubQuest subQuest : quest.subQuests) {
            q.subQuests.add(new SubQuestViewModel(subQuest.name, false));
        }
        return q;
    }
}
