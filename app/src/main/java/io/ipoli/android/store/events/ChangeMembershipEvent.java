package io.ipoli.android.store.events;

import org.threeten.bp.LocalDate;

import io.ipoli.android.player.data.MembershipType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/18/17.
 */
public class ChangeMembershipEvent {
    public final MembershipType previousMembership;
    public final MembershipType newMembership;
    public final LocalDate purchasedDate;
    public final LocalDate serverExpirationDate;
    public final LocalDate calculatedExpirationDate;

    public ChangeMembershipEvent(MembershipType previousMembership, MembershipType newMembership,
                                 LocalDate purchasedDate, LocalDate serverExpirationDate,
                                 LocalDate calculatedExpirationDate) {
        this.previousMembership = previousMembership;
        this.newMembership = newMembership;
        this.purchasedDate = purchasedDate;
        this.serverExpirationDate = serverExpirationDate;
        this.calculatedExpirationDate = calculatedExpirationDate;
    }
}
