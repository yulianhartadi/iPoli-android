package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/29/17.
 */

public class PlayerMigratedEvent {
    public final int previousSchemaVersion;
    public final int currentSchemaVersion;

    public PlayerMigratedEvent(int previousSchemaVersion, int currentSchemaVersion) {
        this.previousSchemaVersion = previousSchemaVersion;
        this.currentSchemaVersion = currentSchemaVersion;
    }
}
