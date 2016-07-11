package io.ipoli.android.quest.events.subquests;

import io.ipoli.android.quest.data.Subquest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/11/16.
 */
public class UpdateSubquestNameEvent {
    public final Subquest subquest;

    public UpdateSubquestNameEvent(Subquest subquest) {
        this.subquest = subquest;
    }
}
