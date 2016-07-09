package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Subquest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/9/16.
 */
public class UpdateSubquestEvent {
    public final Subquest subquest;

    public UpdateSubquestEvent(Subquest subquest) {
        this.subquest = subquest;
    }
}
