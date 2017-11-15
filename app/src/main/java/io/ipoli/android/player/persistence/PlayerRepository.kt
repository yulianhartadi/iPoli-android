package io.ipoli.android.player.persistence

import com.couchbase.lite.Database
import io.ipoli.android.common.persistence.BaseCouchbaseRepository
import io.ipoli.android.common.persistence.CouchbasePersistedModel
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.quest.AuthProvider
import io.ipoli.android.quest.Player
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
        return Player(
            id = cp.id,
            level = cp.level,
            coins = cp.coins,
            experience = cp.experience,
            authProvider = authProvider,
            avatar = Avatar.fromCode(cp.avatarCode)!!,
            createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(cp.createdAt), ZoneId.systemDefault())
        )
    }

    override fun toCouchbaseObject(entity: Player): CouchbasePlayer {
        val cp = CouchbasePlayer()
        cp.id = entity.id
        cp.type = CouchbasePlayer.TYPE
        cp.level = entity.level
        cp.coins = entity.coins
        cp.experience = entity.experience
        cp.authProvider = createCouchbaseAuthProvider(entity).map
        cp.avatarCode = entity.avatar.code
        cp.createdAt = entity.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return cp
    }

    private fun createCouchbaseAuthProvider(entity: Player): CouchbaseAuthProvider {
        val authProvider = entity.authProvider

        val cap = CouchbaseAuthProvider()
        cap.id = authProvider.id
        cap.email = authProvider.email
        cap.firstName = authProvider.firstName
        cap.lastName = authProvider.lastName
        cap.username = authProvider.username
        cap.image = authProvider.image
        cap.provider = authProvider.provider
        return cap
    }
}