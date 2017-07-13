package io.ipoli.android;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.ValidationStatus;
import io.ipoli.android.player.UpgradeStatusValidator;
import io.ipoli.android.player.data.MembershipType;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.store.Upgrade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/13/17.
 */
public class ValidationStatusValidatorTest {

    private Player player;

    @Before
    public void setUp() {
        player = new Player();
        player.setMembership(MembershipType.NONE);
        player.setCreatedAtDate(LocalDate.now());
    }

    @Test
    public void validate_PlayerWithoutUpgrades_EmptyLists() {
        UpgradeStatusValidator validator = new UpgradeStatusValidator(player);
        ValidationStatus status = validator.validate();
        assertThat(status.expired.size(), is(0));
        assertThat(status.expiring.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
    }

    @Test
    public void validate_PlayerWithoutMembershipAndValidUpgrades_EmptyLists() {
        long createdAtExpiredTrial = DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS + 1));
        player.setCreatedAt(createdAtExpiredTrial);
        player.getInventory().addUpgrade(Upgrade.CHALLENGES, LocalDate.now().plusDays(1));
        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now());
        ValidationStatus status = validator.validate();
        assertThat(status.expired.size(), is(0));
        assertThat(status.expiring.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(ValidationStatus.StatusType.NOT_MEMBER));
    }

    @Test
    public void validate_PlayerWithoutMembershipAndUpgradesAboutToExpire_FindsAboutToExpireUpgrades() {
        long createdAtExpiredTrial = DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS + 1));
        player.setCreatedAt(createdAtExpiredTrial);
        player.getInventory().addUpgrade(Upgrade.CHALLENGES, LocalDate.now());
        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now(), false);
        ValidationStatus status = validator.validate();
        assertThat(status.expired.size(), is(0));
        assertThat(status.expiring.size(), is(1));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(ValidationStatus.StatusType.NOT_MEMBER));
    }

    @Test
    public void validate_PlayerWithoutMembershipAndExpiredUpgrade_FindsExpiredUpgrade() {
        long createdAtExpiredTrial = DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS + 1));
        player.setCreatedAt(createdAtExpiredTrial);
        player.getInventory().addUpgrade(Upgrade.CHALLENGES, LocalDate.now().minusDays(1));
        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now());
        ValidationStatus status = validator.validate();
        assertThat(status.expired.size(), is(1));
        assertThat(status.expiring.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(ValidationStatus.StatusType.NOT_MEMBER));
    }

    @Test
    public void validate_PlayerInTrial_TrialStatus() {
        player.setCreatedAt(DateUtils.toMillis(LocalDate.now()));
        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now());
        ValidationStatus status = validator.validate();
        assertThat(status.type, is(ValidationStatus.StatusType.TRIAL));
    }

    @Test
    public void validate_PlayerInLastDayOfTrial_FindsExpiringUpgrades() {
        player.setCreatedAt(DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS - 1)));
        player.getInventory().unlockAllUpgrades(LocalDate.now());
        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now(), false);
        ValidationStatus status = validator.validate();
        assertThat(status.type, is(ValidationStatus.StatusType.NOT_MEMBER));
        assertThat(status.expired.size(), is(0));
        assertThat(status.expiring.size(), is(Upgrade.values().length));
        assertThat(status.toBeRenewed.size(), is(0));
    }

    @Test
    public void validate_PlayerAfterTrial_FindsExpiredUpgradesWithNonMemberStatus() {
        player.setCreatedAt(DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS)));
        player.getInventory().unlockAllUpgrades(LocalDate.now().minusDays(1));
        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now());
        ValidationStatus status = validator.validate();
        assertThat(status.type, is(ValidationStatus.StatusType.NOT_MEMBER));
        assertThat(status.expired.size(), is(Upgrade.values().length));
    }

    @Test
    public void validate_PlayerMemberWithValidUpgrades_EmptyListsAndMemberStatus() {
        player.setMembership(MembershipType.MONTHLY);
        player.setCreatedAtDate(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS));
        player.getInventory().unlockAllUpgrades(LocalDate.now().plusMonths(1));
        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now());
        ValidationStatus status = validator.validate();
        assertThat(status.expired.size(), is(0));
        assertThat(status.expiring.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(ValidationStatus.StatusType.MEMBER));
    }

    @Test
    public void validate_PlayerMemberWithinGracePeriodIsRenewed_RenewUpgrades() {
        player.setMembership(MembershipType.MONTHLY);
        player.setCreatedAtDate(LocalDate.now().minusMonths(2));
        player.getInventory().unlockAllUpgrades(LocalDate.now().plusDays(1));

        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now(), LocalDate.now().plusMonths(1));
        ValidationStatus status = validator.validate();

        assertThat(status.expired.size(), is(0));
        assertThat(status.expiring.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(Upgrade.values().length));
        assertThat(status.type, is(ValidationStatus.StatusType.MEMBER));
    }

    @Test
    public void validate_PlayerMemberAtLastDay_FindsExpiring() {
        player.setMembership(MembershipType.MONTHLY);
        player.setCreatedAtDate(LocalDate.now().minusMonths(2));
        player.getInventory().unlockAllUpgrades(LocalDate.now());

        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now(), false);
        ValidationStatus status = validator.validate();

        assertThat(status.expired.size(), is(0));
        assertThat(status.expiring.size(), is(Upgrade.values().length));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(ValidationStatus.StatusType.MEMBER));
    }

    @Test
    public void validate_PlayerMember_FindsExpired() {
        player.setMembership(MembershipType.MONTHLY);
        player.setCreatedAtDate(LocalDate.now().minusMonths(2));
        player.getInventory().unlockAllUpgrades(LocalDate.now().minusDays(1));

        UpgradeStatusValidator validator = new UpgradeStatusValidator(player, LocalDate.now(), false);
        ValidationStatus status = validator.validate();

        assertThat(status.expired.size(), is(Upgrade.values().length));
        assertThat(status.expiring.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(ValidationStatus.StatusType.NOT_MEMBER));
    }
}