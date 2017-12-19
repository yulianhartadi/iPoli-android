package mypoli.android.player

import mypoli.android.Constants
import mypoli.android.pet.Food
import mypoli.android.pet.Pet
import mypoli.android.pet.PetAvatar
import mypoli.android.quest.ColorPack
import mypoli.android.quest.Entity
import mypoli.android.quest.IconPack
import mypoli.android.store.avatars.data.Avatar
import org.threeten.bp.LocalDateTime

data class Player(
    override val id: String = "",
    val level: Int = 1,
    val coins: Int = Constants.DEFAULT_PLAYER_COINS,
    val experience: Long = Constants.DEFAULT_PLAYER_XP,
    val authProvider: AuthProvider,
    val avatar: Avatar = Avatar.IPOLI_CLASSIC,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val currentTheme: Theme = Constants.DEFAULT_THEME,
    val pet: Pet = Pet(
        name = Constants.DEFAULT_PET_NAME,
        avatar = PetAvatar.ELEPHANT
    ),
    val inventory: Inventory = Inventory(
        food = mapOf(
            Food.BANANA to 2
        ),
        pets = setOf(InventoryPet.fromPet(pet)),
        themes = setOf(currentTheme),
        colorPacks = setOf(ColorPack.FREE),
        iconPacks = setOf(IconPack.FREE)
    )
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

    fun addCoins(coins: Int) = copy(
        coins = coins + this.coins
    )

    fun removeCoins(coins: Int) = copy(
        coins = Math.max(this.coins - coins, 0)
    )

    fun hasPet(petAvatar: PetAvatar) =
        inventory.hasPet(petAvatar)

    fun hasTheme(theme: Theme) =
        inventory.hasTheme(theme)

    fun hasIconPack(iconPack: IconPack) =
        inventory.hasIconPack(iconPack)

    fun hasColorPack(colorPack: ColorPack) =
        inventory.hasColorPack(colorPack)
}

data class InventoryPet(val name: String, val avatar: PetAvatar) {
    companion object {
        fun fromPet(pet: Pet) = InventoryPet(pet.name, pet.avatar)
    }
}

data class Inventory(
    val food: Map<Food, Int> = mapOf(),
    val pets: Set<InventoryPet> = setOf(),
    val themes: Set<Theme> = setOf(),
    val colorPacks: Set<ColorPack> = setOf(),
    val iconPacks: Set<IconPack> = setOf()
) {
    fun addFood(food: Food): Inventory {
        val quantity = this.food.let {
            if (it.containsKey(food)) it[food]!! + 1 else 1
        }
        return copy(
            food = this.food + Pair(food, quantity)
        )
    }

    fun removeFood(food: Food): Inventory {
        requireNotNull(this.food[food])
        val quantity = this.food[food]!! - 1
        return copy(
            food = when (quantity) {
                0 -> this.food.minus(food)
                else -> this.food + Pair(food, quantity)
            }
        )
    }

    fun hasFood(food: Food) = this.food.containsKey(food)

    fun addPet(pet: Pet) =
        copy(
            pets = this.pets + InventoryPet.fromPet(pet)
        )

    fun hasPet(petAvatar: PetAvatar) =
        pets.any { it.avatar == petAvatar }

    fun getPet(petAvatar: PetAvatar) =
        pets.first { it.avatar == petAvatar }

    fun hasTheme(theme: Theme) = themes.contains(theme)

    fun addTheme(theme: Theme)
        = copy(themes = this.themes + theme)

    fun changePetName(name: String, petAvatar: PetAvatar): Inventory {

        val currentPet = getPet(petAvatar)

        return copy(
            pets = this.pets - currentPet + InventoryPet(name, petAvatar)
        )
    }

    fun hasIconPack(iconPack: IconPack) =
        iconPacks.contains(iconPack)

    fun addIconPack(iconPack: IconPack) =
        copy(iconPacks = this.iconPacks + iconPack)

    fun hasColorPack(colorPack: ColorPack) =
        colorPacks.contains(colorPack)

    fun addColorPack(colorPack: ColorPack) =
        copy(colorPacks = this.colorPacks + colorPack)
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