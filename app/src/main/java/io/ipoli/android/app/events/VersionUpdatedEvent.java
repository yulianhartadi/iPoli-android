package io.ipoli.android.app.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class VersionUpdatedEvent {
    public final int oldVersion;
    public final int newVersion;

    public VersionUpdatedEvent(int oldVersion, int newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }
}
