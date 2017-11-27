package io.ipoli.android.player

import io.ipoli.android.Constants
import io.ipoli.android.pet.Pet
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.quest.Entity
import io.ipoli.android.store.avatars.data.Avatar
import org.threeten.bp.LocalDateTime

data class Player(
    override val id: String = "",
    val level: Int = 1,
    val coins: Int = 0,
    val experience: Long = 0,
    val authProvider: AuthProvider,
    val avatar: Avatar = Avatar.IPOLI_CLASSIC,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val pet: Pet = Pet(name = Constants.DEFAULT_PET_NAME, avatar = PetAvatar.ELEPHANT)
) : Entity {
    fun addExperience(experience: Int): Player {
        val newXp = experience + this.experience
        return copy(
            experience = newXp,
            level = nextLevel(newXp)
        )
    }

    private fun nextLevel(newXp: Long): Int {
        var newLevel = level
        while (newXp >= ExperienceForLevelGenerator.forLevel(newLevel + 1)) {
            newLevel++
        }
        return newLevel
    }

    fun removeExperience(experience: Int): Player {
        val newXp = Math.max(this.experience - experience, 0)
        return copy(
            experience = newXp,
            level = prevLevel(newXp)
        )
    }

    private fun prevLevel(newXp: Long): Int {
        var newLevel = level
        while (newLevel - 1 > 0 && newXp < ExperienceForLevelGenerator.forLevel(newLevel)) {
            newLevel--
        }
        return newLevel
    }
}

data class AuthProvider(
    val id: String = "",
    val provider: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val image: String = ""
)