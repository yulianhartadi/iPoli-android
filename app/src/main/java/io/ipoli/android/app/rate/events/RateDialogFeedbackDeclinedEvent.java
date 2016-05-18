package io.ipoli.android.app.rate.events;

import org.joda.time.LocalDateTime;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/18/16.
 */
public class RateDialogFeedbackDeclinedEvent {
    public final int appRun;
    public final LocalDateTime dateTime;

    public RateDialogFeedbackDeclinedEvent(int appRun) {
        this.appRun = appRun;
        dateTime = new LocalDateTime();
    }
}
