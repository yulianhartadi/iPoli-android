package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Habit;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/16.
 */
public class ShowHabitEvent {
    public final Habit habit;

    public ShowHabitEvent(Habit habit) {
        this.habit = habit;
    }
}
