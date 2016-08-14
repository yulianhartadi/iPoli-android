package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class NetworkConnectionChangedEvent {
    public final boolean hasInternet;

    public NetworkConnectionChangedEvent(boolean hasInternet) {
        this.hasInternet = hasInternet;
    }
}
