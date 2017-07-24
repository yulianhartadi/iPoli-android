package io.ipoli.android.app.tutorial.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class RequestOverlayPermissionEvent {
    public final int versionNumber;

    public RequestOverlayPermissionEvent(int versionNumber) {

        this.versionNumber = versionNumber;
    }
}
