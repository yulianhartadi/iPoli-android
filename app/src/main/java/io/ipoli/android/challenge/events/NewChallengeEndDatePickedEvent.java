package io.ipoli.android.challenge.events;


import org.threeten.bp.LocalDate;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/17.
 */
public class NewChallengeEndDatePickedEvent {
    public final LocalDate date;

    public NewChallengeEndDatePickedEvent(LocalDate date) {
        this.date = date;
    }
}
