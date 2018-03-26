package mypoli.android.store.membership.usecase

import mypoli.android.common.UseCase
import mypoli.android.player.Membership
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.store.membership.MembershipPlan
import mypoli.android.store.powerup.usecase.EnableAllPowerUpsUseCase
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/22/18.
 */
class UpdatePlayerMembershipUseCase(
    private val playerRepository: PlayerRepository,
    private val enableAllPowerUpsUseCase: EnableAllPowerUpsUseCase
) : UseCase<UpdatePlayerMembershipUseCase.Params, Player> {

    override fun execute(parameters: Params): Player {
        val purchasedDate = parameters.purchasedDate
        val expirationDate = parameters.expirationDate

        var (membership, resultExpirationDate) = when (parameters.plan) {
            MembershipPlan.MONTHLY ->
                Pair(
                    Membership.MONTHLY,
                    purchasedDate.plusMonths(1).minusDays(1)
                )
            MembershipPlan.YEARLY ->
                Pair(
                    Membership.YEARLY,
                    purchasedDate.plusYears(1).minusDays(1)

                )
            MembershipPlan.QUARTERLY ->
                Pair(
                    Membership.QUARTERLY,
                    purchasedDate.plusMonths(3).minusDays(1)
                )
        }

        if (expirationDate != null && expirationDate.isAfter(resultExpirationDate)) {
            resultExpirationDate = expirationDate
        }

        val newPlayer = enableAllPowerUpsUseCase.execute(EnableAllPowerUpsUseCase.Params(resultExpirationDate))

        return playerRepository.save(
            newPlayer.copy(
                membership = membership
            )
        )
    }

    data class Params(
        val plan: MembershipPlan,
        val purchasedDate: LocalDate = LocalDate.now(),
        val expirationDate: LocalDate? = null
    )
}