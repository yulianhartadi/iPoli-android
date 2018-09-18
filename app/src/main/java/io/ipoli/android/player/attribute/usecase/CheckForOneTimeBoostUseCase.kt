package io.ipoli.android.player.attribute.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.attribute.AttributeRank
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.data.Player.Rank.*
import io.ipoli.android.player.job.SecretSocietyInviteScheduler

class CheckForOneTimeBoostUseCase(
    private val secretSocietyInviteScheduler: SecretSocietyInviteScheduler
) : UseCase<CheckForOneTimeBoostUseCase.Params, Player> {

    override fun execute(parameters: Params): Player {
        val player = parameters.player

        val status = parameters.playerRank

        var newPlayer = checkForStrengthUnlocks(player, status)
        newPlayer = checkForIntelligenceUnlocks(newPlayer, status)
        newPlayer = checkForCharismaUnlocks(newPlayer, status)
        newPlayer = updateExpertiseStatusIfChanged(newPlayer, status)
        newPlayer = updateWellBeingStatusIfChanged(newPlayer, status)
        newPlayer = updateWillpowerStatusIfChanged(newPlayer, status)

        return newPlayer
    }

    private fun updateExpertiseStatusIfChanged(
        player: Player,
        rank: Player.Rank
    ): Player {
        val stats = player.statistics

        val (currentStatus, newStatus) = attributeStatuses(
            rank,
            stats.expertiseStatusIndex.toInt(),
            player.attributeLevel(Player.AttributeType.EXPERTISE)
        )

        if (currentStatus == newStatus) {
            return player
        }

        return player.copy(
            statistics = stats.copy(
                expertiseStatusIndex = newStatus.ordinal.toLong()
            )
        )
    }

    private fun updateWellBeingStatusIfChanged(
        player: Player,
        rank: Player.Rank
    ): Player {
        val stats = player.statistics

        val (currentStatus, newStatus) = attributeStatuses(
            rank,
            stats.wellBeingStatusIndex.toInt(),
            player.attributeLevel(Player.AttributeType.WELL_BEING)
        )

        if (currentStatus == newStatus) {
            return player
        }

        return player.copy(
            statistics = stats.copy(
                wellBeingStatusIndex = newStatus.ordinal.toLong()
            )
        )
    }

    private fun updateWillpowerStatusIfChanged(
        player: Player,
        rank: Player.Rank
    ): Player {
        val stats = player.statistics

        val (currentStatus, newStatus) = attributeStatuses(
            rank,
            stats.willpowerStatusIndex.toInt(),
            player.attributeLevel(Player.AttributeType.WILLPOWER)
        )

        if (currentStatus == newStatus) {
            return player
        }

        return player.copy(
            statistics = stats.copy(
                willpowerStatusIndex = newStatus.ordinal.toLong()
            )
        )
    }

    private fun checkForStrengthUnlocks(player: Player, rank: Player.Rank): Player {

        val stats = player.statistics

        val (currentStatus, newStatus) = attributeStatuses(
            rank,
            stats.strengthStatusIndex.toInt(),
            player.attributeLevel(Player.AttributeType.STRENGTH)
        )

        if (currentStatus == newStatus) {
            return player
        }

        val health = player.health

        if (newStatus == APPRENTICE) {
            return player.copy(
                health = Player.Health(health.current, health.max + (health.max * .2).toInt()),
                statistics = stats.copy(
                    strengthStatusIndex = newStatus.ordinal.toLong()
                )
            )
        }

        if (newStatus == ADEPT) {
            return player.copy(
                health = Player.Health(health.current, health.max + (health.max * .3).toInt()),
                statistics = stats.copy(
                    strengthStatusIndex = newStatus.ordinal.toLong()
                )
            )
        }

        if (newStatus != rank) {
            return player.copy(
                statistics = stats.copy(
                    strengthStatusIndex = newStatus.ordinal.toLong()
                )
            )
        }

        return player
    }

    private fun checkForIntelligenceUnlocks(player: Player, rank: Player.Rank): Player {
        val stats = player.statistics

        val (currentStatus, newStatus) = attributeStatuses(
            rank,
            stats.charismaStatusIndex.toInt(),
            player.attributeLevel(Player.AttributeType.CHARISMA)
        )

        if (currentStatus == newStatus) {
            return player
        }

        if (newStatus == SPECIALIST) {
            secretSocietyInviteScheduler.schedule()
        }

        if (newStatus != rank) {
            return player.copy(
                statistics = stats.copy(
                    intelligenceStatusIndex = newStatus.ordinal.toLong()
                )
            )
        }

        return player
    }

    private fun checkForCharismaUnlocks(player: Player, rank: Player.Rank): Player {
        val stats = player.statistics

        val (currentStatus, newStatus) = attributeStatuses(
            rank,
            stats.charismaStatusIndex.toInt(),
            player.attributeLevel(Player.AttributeType.CHARISMA)
        )

        if (currentStatus == newStatus) {
            return player
        }

        if (newStatus == APPRENTICE) {
            return player.copy(
                statistics = stats.copy(
                    charismaStatusIndex = newStatus.ordinal.toLong(),
                    inviteForFriendCount = stats.inviteForFriendCount + 1
                )
            )
        }

        if (newStatus == ADEPT) {
            return player.copy(
                statistics = stats.copy(
                    charismaStatusIndex = newStatus.ordinal.toLong(),
                    inviteForFriendCount = stats.inviteForFriendCount + 5
                )
            )
        }


        if (newStatus != rank) {
            return player.copy(
                statistics = stats.copy(
                    charismaStatusIndex = newStatus.ordinal.toLong()
                )
            )
        }

        return player
    }

    private fun attributeStatuses(
        rank: Player.Rank,
        attributeStatusIndex: Int,
        attributeLevel: Int
    ): Pair<Player.Rank, Player.Rank> {
        val attrStatus = values()[attributeStatusIndex]

        val newAttrStatus = AttributeRank.of(
            attributeLevel,
            rank
        )
        return Pair(attrStatus, newAttrStatus)
    }

    data class Params(val player: Player, val playerRank: Player.Rank)
}