package io.ipoli.android;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import io.ipoli.android.player.UpgradeStatusChecker;
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
    }

    @Test
    public void checkStatus_PlayerWithoutUpgrades_EmptyLists() {
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player);
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.aboutToExpire.size(), is(0));
        assertThat(status.expired.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
    }

    @Test
    public void checkStatus_PlayerWithoutMembershipAndValidUpgrades_EmptyLists() {
        player.getInventory().addUpgrade(Upgrade.CHALLENGES, LocalDate.now().plusDays(Constants.UPGRADE_EXPIRATION_GRACE_DAYS + 1));
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player, LocalDate.now());
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.aboutToExpire.size(), is(0));
        assertThat(status.expired.size(), is(0));
        assertThat(status.toBeRenewed.size(), is(0));
    }

    @Test
    public void checkStatus_PlayerWithoutMembershipAndExpiredUpgrade_FindsExpiredUpgrade() {
        player.getInventory().addUpgrade(Upgrade.CHALLENGES, LocalDate.now().minusDays(1));
        UpgradeStatusChecker checker = new UpgradeStatusChecker(player, LocalDate.now());
        UpgradeStatusChecker.UpgradeStatus status = checker.checkStatus();
        assertThat(status.aboutToExpire.size(), is(0));
        assertThat(status.expired.size(), is(1));
        assertThat(status.toBeRenewed.size(), is(0));
    }
}
