package io.ipoli.android.app.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/6/17.
 */
public class FirebaseInviteSentEvent {
    public final String[] invitationIds;

    public FirebaseInviteSentEvent(String... invitationIds) {
        this.invitationIds = invitationIds;
    }
}
