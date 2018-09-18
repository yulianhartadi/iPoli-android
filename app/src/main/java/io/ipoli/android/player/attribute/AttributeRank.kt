package io.ipoli.android.player.attribute

import io.ipoli.android.player.data.Player

object AttributeRank {

    fun of(attributeLevel: Int, playerRank: Player.Rank) =
        Player.Rank.values().reversed().firstOrNull { status ->

            val index = Player.Rank.values().indexOf(status)

            if (index == 0)
                false
            else if (attributeLevel == 100 && playerRank == Player.Rank.DIVINITY) {
                true
            } else {
                val prevStatus = Player.Rank.values()[index - 1]
                val curStatus = Player.Rank.values()[index]
                attributeLevel >= index * 10 && (playerRank == curStatus || playerRank == prevStatus)
            }
        } ?: Player.Rank.NOVICE
}