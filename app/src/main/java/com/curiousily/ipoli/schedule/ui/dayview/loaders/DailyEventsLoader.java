package com.curiousily.ipoli.schedule.ui.dayview.loaders;

import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.viewmodel.QuestViewModel;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 10/1/15.
 */
public interface DailyEventsLoader {
    List<Quest> loadEventsFor(Calendar day);
}
