package io.ipoli.android.app.share.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/17.
 */
public class InviteFriendsProviderPickedEvent {
    public final String providerName;

    public InviteFriendsProviderPickedEvent(String providerName) {
        this.providerName = providerName;
    }
}
