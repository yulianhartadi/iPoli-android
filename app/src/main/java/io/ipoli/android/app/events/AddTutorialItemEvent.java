package io.ipoli.android.app.events;

import io.ipoli.android.tutorial.TutorialItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class AddTutorialItemEvent {

    public final TutorialItem tutorialItem;

    public AddTutorialItemEvent(TutorialItem tutorialItem) {
        this.tutorialItem = tutorialItem;
    }

}
