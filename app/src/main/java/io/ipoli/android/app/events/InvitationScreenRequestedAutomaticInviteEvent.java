package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/15/16.
 */
public class InvitationScreenRequestedAutomaticInviteEvent {
    public boolean isInviteReceived;

    public InvitationScreenRequestedAutomaticInviteEvent(boolean isInviteReceived) {
        this.isInviteReceived = isInviteReceived;
    }
}
