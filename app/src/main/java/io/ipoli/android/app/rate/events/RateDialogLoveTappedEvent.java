package io.ipoli.android.app.rate.events;

import org.threeten.bp.LocalDateTime;

import io.ipoli.android.app.rate.DialogAnswer;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/18/16.
 */
public class RateDialogLoveTappedEvent {
    public final int appRun;
    public final DialogAnswer answer;
    public final LocalDateTime dateTime;

    public RateDialogLoveTappedEvent(int appRun, DialogAnswer answer) {
        this.appRun = appRun;
        this.answer = answer;
        dateTime = LocalDateTime.now();
    }
}
