package io.ipoli.android.tutorial.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/15/16.
 */
public class ShowTutorialItemEvent {
    public String state;

    public ShowTutorialItemEvent(String state) {
        this.state = state;
    }
}
