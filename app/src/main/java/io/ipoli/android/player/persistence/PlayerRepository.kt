package io.ipoli.android.player.persistence

import com.couchbase.lite.Database
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.player.auth.AuthProvider
import io.ipoli.android.quest.Player
import io.ipoli.android.quest.data.persistence.BaseCouchbaseRepository
import io.ipoli.android.quest.data.persistence.CouchbasePersistedModel
import io.ipoli.android.store.avatars.data.Avatar

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
    var authProvider: AuthProvider? by map
    var avatar: Avatar by map
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
    }

    override fun toCouchbaseObject(entity: Player): CouchbasePlayer {
    }

}


//class RealmPlayerRepository : BaseRealmRepository<Player, RealmPlayer>(), PlayerRepository {
//    override fun convertToRealmModel(entity: Player): RealmPlayer =
//        entity.let {
//            RealmPlayer(
//                id = it.id,
//                coins = it.coins,
//                experience = it.experience,
//                authProvider = it.authProvider)
//        }
//
//    override fun convertToEntity(realmModel: RealmPlayer): Player =
//        realmModel.let {
//            Player(
//                id = it.id,
//                coins = it.coins,
//                experience = it.experience,
//                authProvider = it.authProvider,
//                createdAt = LocalDateTime.now()
//            )
//        }
//
//    override fun get(): Player? =
//        Realm.getDefaultInstance().use { realm ->
//            val realmModel = realm.where(getModelClass()).findFirst() ?: return@use null
//            convertToEntity(realmModel)
//        }
//
//    override fun getModelClass(): Class<RealmPlayer> = RealmPlayer::class.java
//
//}