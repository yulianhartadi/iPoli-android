package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Habit;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/7/16.
 */
public class NewHabitEvent {
    public final Habit habit;

    public NewHabitEvent(Habit habit) {
        this.habit = habit;
    }
}
