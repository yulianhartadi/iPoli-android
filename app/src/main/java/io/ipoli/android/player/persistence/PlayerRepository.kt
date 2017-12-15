package io.ipoli.android.player.persistence

import com.couchbase.lite.Database
import io.ipoli.android.common.persistence.BaseCouchbaseRepository
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.pet.Food
import io.ipoli.android.pet.Pet
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.*
import io.ipoli.android.player.persistence.model.*
import io.ipoli.android.quest.ColorPack
import io.ipoli.android.store.avatars.data.Avatar
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/2/17.
 */
interface PlayerRepository : Repository<Player>

class CouchbasePlayerRepository(database: Database, coroutineContext: CoroutineContext) : BaseCouchbaseRepository<Player, CouchbasePlayer>(database, coroutineContext), PlayerRepository {
    override val modelType = CouchbasePlayer.TYPE

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): Player {
        val cp = CouchbasePlayer(dataMap)

        val cap = CouchbaseAuthProvider(cp.authProvider)
        val authProvider = AuthProvider(
            id = cap.id,
            provider = cap.provider,
            firstName = cap.firstName,
            lastName = cap.lastName,
            email = cap.email,
            image = cap.image
        )
        val cPet = CouchbasePet(cp.pet)
        val pet = Pet(
            name = cPet.name,
            avatar = PetAvatar.valueOf(cPet.avatar),
            moodPoints = cPet.moodPoints,
            healthPoints = cPet.healthPoints,
            coinBonus = cPet.coinBonus,
            experienceBonus = cPet.experienceBonus,
            bountyBonus = cPet.itemDropChanceBonus
        )

        val ci = CouchbaseInventory(cp.inventory)
        val inventory = Inventory(
            food = ci.food.entries.associate { Food.valueOf(it.key) to it.value.toInt() },
            pets = ci.pets.map {
                val cip = CouchbaseInventoryPet(it)
                InventoryPet(cip.name, PetAvatar.valueOf(cip.avatar))
            }.toSet(),
            themes = ci.themes.map { Theme.valueOf(it) }.toSet(),
            colorPacks = ci.colorPacks.map { ColorPack.valueOf(it) }.toSet()
        )

        return Player(
            id = cp.id,
            level = cp.level,
            coins = cp.coins,
            experience = cp.experience,
            authProvider = authProvider,
            avatar = Avatar.fromCode(cp.avatarCode)!!,
            currentTheme = Theme.valueOf(cp.currentTheme),
            inventory = inventory,
            createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(cp.createdAt), ZoneId.systemDefault()),
            pet = pet
        )
    }

    override fun toCouchbaseObject(entity: Player) =
        CouchbasePlayer().also {
            it.id = entity.id
            it.type = CouchbasePlayer.TYPE
            it.level = entity.level
            it.coins = entity.coins
            it.experience = entity.experience
            it.authProvider = createCouchbaseAuthProvider(entity.authProvider).map
            it.avatarCode = entity.avatar.code
            it.createdAt = entity.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            it.currentTheme = entity.currentTheme.name
            it.pet = createCouchbasePet(entity.pet).map
            it.inventory = createCouchbaseInventory(entity.inventory).map
        }

    private fun createCouchbasePet(pet: Pet) =
        CouchbasePet().also {
            it.name = pet.name
            it.avatar = pet.avatar.name
            it.healthPoints = pet.healthPoints
            it.moodPoints = pet.moodPoints
            it.coinBonus = pet.coinBonus
            it.experienceBonus = pet.experienceBonus
            it.itemDropChanceBonus = pet.bountyBonus
        }

    private fun createCouchbaseAuthProvider(authProvider: AuthProvider) =
        CouchbaseAuthProvider().also {
            it.id = authProvider.id
            it.email = authProvider.email
            it.firstName = authProvider.firstName
            it.lastName = authProvider.lastName
            it.username = authProvider.username
            it.image = authProvider.image
            it.provider = authProvider.provider
        }

    private fun createCouchbaseInventory(inventory: Inventory) =
        CouchbaseInventory().also {
            it.food = inventory.food.entries
                .associate { it.key.name to it.value.toLong() }
                .toMutableMap()
            it.pets = inventory.pets
                .map { createCouchbaseInventoryPet(it).map }
            it.themes = inventory.themes.map { it.name }
            it.colorPacks = inventory.colorPacks.map { it.name }
        }

    private fun createCouchbaseInventoryPet(inventoryPet: InventoryPet) =
        CouchbaseInventoryPet().also {
            it.name = inventoryPet.name
            it.avatar = inventoryPet.avatar.name
        }
}