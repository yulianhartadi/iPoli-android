package com.curiousily.ipoli.quest.viewmodel;

import android.databinding.ObservableBoolean;
import android.text.TextUtils;

import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.SubQuest;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/15.
 */
public class QuestViewModel {
    public String name;
    public String description;
    public Calendar startTime;
    public String context;
    public int duration;
    public int backgroundColor;
    public String tags;
    public String notes;
    public ObservableBoolean isRunning;
    public MaterialDrawableBuilder.IconValue icon;
    public List<SubQuestViewModel> subQuests = new ArrayList<>();
    public Calendar endTime;

    public static QuestViewModel from(Quest quest) {
        QuestViewModel model = new QuestViewModel();
        model.name = quest.name;
        model.description = quest.description;
        model.duration = quest.duration;
        model.context = quest.context.name();
        model.backgroundColor = quest.context.getPrimaryColor();
        model.icon = quest.context.getIcon();
        model.tags = TextUtils.join(", ", quest.tags);
        model.isRunning = new ObservableBoolean(quest.status == Quest.Status.RUNNING);
        for (SubQuest subQuest : quest.subQuests) {
            model.subQuests.add(new SubQuestViewModel(subQuest.name, false));
        }
        return model;
    }
}
