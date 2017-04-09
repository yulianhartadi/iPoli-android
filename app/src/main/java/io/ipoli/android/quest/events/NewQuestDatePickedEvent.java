package io.ipoli.android.quest.events;

import org.threeten.bp.LocalDate;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */
public class NewQuestDatePickedEvent {

    public final LocalDate start;
    public final LocalDate end;

    public NewQuestDatePickedEvent(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }
}
