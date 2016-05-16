package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Habit;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/7/16.
 */
public class HabitSavedEvent {
    public final Habit habit;

    public HabitSavedEvent(Habit habit) {
        this.habit = habit;
    }
}
