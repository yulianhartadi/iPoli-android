package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Habit;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/16.
 */
public class DeleteHabitRequestEvent {
    public final Habit habit;
    public final int position;

    public DeleteHabitRequestEvent(Habit habit, int position) {
        this.habit = habit;
        this.position = position;
    }
}
