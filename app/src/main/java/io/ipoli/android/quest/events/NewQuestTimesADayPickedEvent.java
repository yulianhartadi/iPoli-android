package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/16/17.
 */
public class NewQuestTimesADayPickedEvent {
    public final int timesADay;

    public NewQuestTimesADayPickedEvent(int timesADay) {
        this.timesADay = timesADay;
    }
}
