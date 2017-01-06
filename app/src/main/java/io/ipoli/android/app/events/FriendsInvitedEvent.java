package io.ipoli.android.app.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/6/17.
 */
public class FriendsInvitedEvent {
    public final String[] invitationIds;

    public FriendsInvitedEvent(String... invitationIds) {
        this.invitationIds = invitationIds;
    }
}
