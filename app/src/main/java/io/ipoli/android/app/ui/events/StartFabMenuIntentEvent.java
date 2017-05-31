package io.ipoli.android.app.ui.events;

import android.content.Intent;

import io.ipoli.android.app.ui.FabMenuView;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/31/17.
 */

public class StartFabMenuIntentEvent {
    public final Intent intent;
    public final FabMenuView.FabName fabName;

    public StartFabMenuIntentEvent(Intent intent, FabMenuView.FabName fabName) {
        this.intent = intent;
        this.fabName = fabName;
    }
}
