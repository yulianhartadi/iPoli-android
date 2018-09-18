package io.ipoli.android.player.attribute.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.player.data.Player
import org.amshove.kluent.`should equal`
import org.amshove.kluent.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class CheckForOneTimeBoostUseCaseSpek : Spek({

    fun playerWithAttributeLevel(
        attributeType: Player.AttributeType, level: Int
    ): Player {
        val attrs = TestUtil.player.attributes.toMutableMap()
        attrs[attributeType] = attrs[attributeType]!!.copy(level = level)
        return TestUtil.player.copy(
            attributes = attrs
        )
    }

    describe("CheckForOneTimeBoostUseCase") {

        fun executeUseCase(player: Player, rank: Player.Rank) =
            CheckForOneTimeBoostUseCase(
                mock()
            ).execute(
                CheckForOneTimeBoostUseCase.Params(player, rank)
            )

        it("should increase life by 20% when strength is at status apprentice") {
            val p =
                playerWithAttributeLevel(
                    attributeType = Player.AttributeType.STRENGTH,
                    level = 10
                )

            val newPlayer = executeUseCase(p, Player.Rank.APPRENTICE)

            newPlayer.health.max.`should equal`(p.health.max + (p.health.max * .2).toInt())
        }

        it("should increase life by 30% when strength is at status adept") {
            val p =
                playerWithAttributeLevel(
                    attributeType = Player.AttributeType.STRENGTH,
                    level = 20
                )

            val newPlayer = executeUseCase(p, Player.Rank.ADEPT)

            newPlayer.health.max.`should equal`(p.health.max + (p.health.max * .3).toInt())
        }

        it("should save max strength level at stats") {
            val p =
                playerWithAttributeLevel(
                    attributeType = Player.AttributeType.STRENGTH,
                    level = 10
                )

            executeUseCase(p, Player.Rank.APPRENTICE).statistics.strengthStatusIndex.`should equal`(Player.Rank.APPRENTICE.ordinal.toLong())
        }

        it("should not give strength boost after it was unlocked once") {
            val p =
                playerWithAttributeLevel(
                    attributeType = Player.AttributeType.STRENGTH,
                    level = 10
                )

            val np = executeUseCase(p, Player.Rank.APPRENTICE)
            executeUseCase(np, Player.Rank.APPRENTICE).health.max.`should equal`(np.health.max)
        }
    }
})