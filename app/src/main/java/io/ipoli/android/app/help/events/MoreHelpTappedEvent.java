package io.ipoli.android.app.help.events;

import io.ipoli.android.app.help.Screen;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/26/16.
 */
public class MoreHelpTappedEvent {
    public final Screen screen;
    public final int appRun;

    public MoreHelpTappedEvent(Screen screen, int appRun) {
        this.screen = screen;
        this.appRun = appRun;
    }
}
