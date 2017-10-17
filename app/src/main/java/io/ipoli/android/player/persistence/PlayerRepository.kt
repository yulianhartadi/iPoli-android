package io.ipoli.android.player.persistence

import com.couchbase.lite.Database
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.player.auth.AuthProvider
import io.ipoli.android.quest.Player
import io.ipoli.android.quest.data.persistence.BaseCouchbaseRepository
import io.ipoli.android.quest.data.persistence.CouchbasePersistedModel
import io.ipoli.android.store.avatars.data.Avatar
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/2/17.
 */
interface PlayerRepository : Repository<Player> {
}

data class CouchbasePlayer(override val map: MutableMap<String, Any?> = mutableMapOf()) : CouchbasePersistedModel {
    override var type: String by map
    override var id: String by map
    var coins: Int by map
    var experience: Int by map
    var authProvider: MutableMap<String, Any?>? by map
    var avatarCode: Int by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map

    companion object {
        const val TYPE = "Player"
    }
}

class CouchbasePlayerRepository(database: Database) : BaseCouchbaseRepository<Player, CouchbasePlayer>(database), PlayerRepository {
    override val modelType = CouchbasePlayer.TYPE

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): Player {
        val cp = CouchbasePlayer(dataMap)
        var authProvider: AuthProvider? = null
        if (cp.authProvider != null) {
            authProvider = AuthProvider(cp.authProvider as MutableMap<String, Any?>)
        }
        return Player(
            id = cp.id,
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
        cp.coins = entity.coins
        cp.experience = entity.experience
        cp.authProvider = entity.authProvider?.map
        cp.avatarCode = entity.avatar.code
        cp.createdAt = entity.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return cp
    }

}