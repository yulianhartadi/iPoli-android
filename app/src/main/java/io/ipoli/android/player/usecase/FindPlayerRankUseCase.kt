package io.ipoli.android.player.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.data.Player.Rank

class FindPlayerRankUseCase :
    UseCase<FindPlayerRankUseCase.Params, FindPlayerRankUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val attrLevels = parameters.playerAttributes.map {
            it.value.level
        }

        val level = parameters.playerLevel

        return when {
            level >= 100 && attrLevels.all { it >= 100 } ->
                Result(Rank.DIVINITY, null)

            level >= 90 && attrLevels.all { it >= 90 } ->
                Result(Rank.TITAN, Rank.DIVINITY)

            level >= 80 && attrLevels.all { it >= 80 } ->
                Result(Rank.DEMIGOD, Rank.TITAN)

            level >= 70 && attrLevels.all { it >= 70 } ->
                Result(Rank.IMMORTAL, Rank.DEMIGOD)

            level >= 60 && attrLevels.all { it >= 60 } ->
                Result(Rank.LEGEND, Rank.IMMORTAL)

            level >= 50 && attrLevels.all { it >= 50 } ->
                Result(Rank.MASTER, Rank.LEGEND)

            level >= 40 && attrLevels.all { it >= 40 } ->
                Result(Rank.EXPERT, Rank.MASTER)

            level >= 30 && attrLevels.all { it >= 30 } ->
                Result(Rank.SPECIALIST, Rank.EXPERT)

            level >= 20 && attrLevels.all { it >= 20 } ->
                Result(Rank.ADEPT, Rank.SPECIALIST)

            level >= 10 && attrLevels.all { it >= 10 } ->
                Result(Rank.APPRENTICE, Rank.ADEPT)

            else -> Result(Rank.NOVICE, Rank.APPRENTICE)
        }
    }

    data class Params(
        val playerAttributes: Map<Player.AttributeType, Player.Attribute>,
        val playerLevel: Int
    )

    data class Result(val currentRank: Rank, val nextRank: Rank?)
}