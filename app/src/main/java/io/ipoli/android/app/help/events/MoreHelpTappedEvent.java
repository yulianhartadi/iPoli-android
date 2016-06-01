package io.ipoli.android.app.help.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/26/16.
 */
public class MoreHelpTappedEvent {
    public final String screen;
    public final int appRun;

    public MoreHelpTappedEvent(String screen, int appRun) {
        this.screen = screen;
        this.appRun = appRun;
    }
}
