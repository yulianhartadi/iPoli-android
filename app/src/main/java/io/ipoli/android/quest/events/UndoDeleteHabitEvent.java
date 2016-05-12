package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Habit;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/9/16.
 */
public class UndoDeleteHabitEvent {
    public final Habit habit;

    public UndoDeleteHabitEvent(Habit habit) {
        this.habit = habit;
    }
}
