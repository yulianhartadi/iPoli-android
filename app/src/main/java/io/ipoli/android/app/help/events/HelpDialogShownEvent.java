package io.ipoli.android.app.help.events;

import io.ipoli.android.app.help.Screen;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/26/16.
 */
public class HelpDialogShownEvent {
    public final Screen screen;
    public final int appRun;

    public HelpDialogShownEvent(Screen screen, int appRun) {
        this.screen = screen;
        this.appRun = appRun;
    }
}
