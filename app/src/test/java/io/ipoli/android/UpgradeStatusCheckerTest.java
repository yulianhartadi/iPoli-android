package io.ipoli.android;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.UpgradeStatusChecker;
import io.ipoli.android.player.data.MembershipType;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.store.Upgrade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/13/17.
 */
public class UpgradeStatusCheckerTest {

    private Player player;

    @Before
    public void setUp() {
        player = new Player();
        player.setMembership(MembershipType.NONE);
        player.setCreatedAt(DateUtils.toMillis(LocalDate.now()));
    }

    @Test
    public void checkStatus_PlayerWithoutUpgrades_EmptyLists() {
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player);
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.inGracePeriod.size(), is(0));
        assertThat(status.expired.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
    }

    @Test
    public void checkStatus_PlayerWithoutMembershipAndValidUpgrades_EmptyLists() {
        long createdAtExpiredTrial = DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS + Constants.UPGRADE_TRIAL_GRACE_PERIOD_DAYS + 1));
        player.setCreatedAt(createdAtExpiredTrial);
        player.getInventory().addUpgrade(Upgrade.CHALLENGES, LocalDate.now().plusDays(Constants.UPGRADE_EXPIRATION_GRACE_PERIOD_DAYS + 1));
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player, LocalDate.now());
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.inGracePeriod.size(), is(0));
        assertThat(status.expired.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(UpgradeStatusChecker.UpgradeStatus.StatusType.NOT_MEMBER));
    }

    @Test
    public void checkStatus_PlayerWithoutMembershipAndUpgradesAboutToExpire_FindsAboutToExpireUpgrades() {
        long createdAtExpiredTrial = DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS + Constants.UPGRADE_TRIAL_GRACE_PERIOD_DAYS + 1));
        player.setCreatedAt(createdAtExpiredTrial);
        player.getInventory().addUpgrade(Upgrade.CHALLENGES, LocalDate.now());
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player, LocalDate.now());
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.inGracePeriod.size(), is(1));
        assertThat(status.expired.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(UpgradeStatusChecker.UpgradeStatus.StatusType.NOT_MEMBER));
    }

    @Test
    public void checkStatus_PlayerWithoutMembershipAndExpiredUpgrade_FindsExpiredUpgrade() {
        long createdAtExpiredTrial = DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS + Constants.UPGRADE_TRIAL_GRACE_PERIOD_DAYS + 1));
        player.setCreatedAt(createdAtExpiredTrial);
        player.getInventory().addUpgrade(Upgrade.CHALLENGES, LocalDate.now().minusDays(1));
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player, LocalDate.now());
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.inGracePeriod.size(), is(0));
        assertThat(status.expired.size(), is(1));
        assertThat(status.toBeRenewed.size(), is(0));
        assertThat(status.type, is(UpgradeStatusChecker.UpgradeStatus.StatusType.NOT_MEMBER));
    }

    @Test
    public void checkStatus_PlayerInTrial_TrialStatus() {
        player.setCreatedAt(DateUtils.toMillis(LocalDate.now()));
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player, LocalDate.now());
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.type, is(UpgradeStatusChecker.UpgradeStatus.StatusType.TRIAL));
    }

    @Test
    public void checkStatus_PlayerInTrialGrace_TrialGraceStatus() {
        player.setCreatedAt(DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS)));
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player, LocalDate.now());
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.type, is(UpgradeStatusChecker.UpgradeStatus.StatusType.TRIAL_GRACE));
        assertThat(status.inGracePeriod.size(), is(Upgrade.values().length));
    }

    @Test
    public void checkStatus_PlayerAfterTrialGrace_FindsExpiredUpgradesWithNonMemberStatus() {
        player.setCreatedAt(DateUtils.toMillis(LocalDate.now().minusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS + Constants.UPGRADE_TRIAL_GRACE_PERIOD_DAYS)));
        player.getInventory().unlockAllUpgrades(LocalDate.now().minusDays(1));
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player, LocalDate.now());
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.type, is(UpgradeStatusChecker.UpgradeStatus.StatusType.NOT_MEMBER));
        assertThat(status.expired.size(), is(Upgrade.values().length));
    }

}
