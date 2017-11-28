package io.ipoli.android.player.persistence

import com.couchbase.lite.Database
import io.ipoli.android.common.persistence.BaseCouchbaseRepository
import io.ipoli.android.common.persistence.CouchbasePersistedModel
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.pet.Pet
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.AuthProvider
import io.ipoli.android.player.Player
import io.ipoli.android.store.avatars.data.Avatar
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/2/17.
 */
interface PlayerRepository : Repository<Player> {
}

data class CouchbasePlayer(override val map: MutableMap<String, Any?> = mutableMapOf()) : CouchbasePersistedModel {
    override var type: String by map
    override var id: String by map
    var level: Int by map
    var coins: Int by map
    var experience: Long by map
    var authProvider: MutableMap<String, Any?> by map
    var avatarCode: Int by map
    var pet: MutableMap<String, Any?> by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map

    companion object {
        const val TYPE = "Player"
    }
}

data class CouchbaseAuthProvider(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var id: String by map
    var provider: String by map
    var firstName: String by map
    var lastName: String by map
    var username: String by map
    var email: String by map
    var image: String by map
}

data class CouchbasePet(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var name: String by map
    var avatar: String by map
    var moodPoints: Int by map
    var healthPoints: Int by map
    var experienceBonus: Float by map
    var coinBonus: Float by map
    var unlockChanceBonus: Float by map
}


enum class ProviderType {
    FACEBOOK, GOOGLE, ANONYMOUS
}

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
            unlockChanceBonus = cPet.unlockChanceBonus
        )

        return Player(
            id = cp.id,
            level = cp.level,
            coins = cp.coins,
            experience = cp.experience,
            authProvider = authProvider,
            avatar = Avatar.fromCode(cp.avatarCode)!!,
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
            it.pet = createCouchbasePet(entity.pet).map
        }

    private fun createCouchbasePet(pet: Pet) =
        CouchbasePet().also {
            it.name = pet.name
            it.avatar = pet.avatar.name
            it.healthPoints = pet.healthPoints
            it.moodPoints = pet.moodPoints
            it.coinBonus = pet.coinBonus
            it.experienceBonus = pet.experienceBonus
            it.unlockChanceBonus = pet.unlockChanceBonus
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
}