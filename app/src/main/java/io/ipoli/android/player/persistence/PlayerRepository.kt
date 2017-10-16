package io.ipoli.android.player.persistence

import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.player.data.RealmPlayer
import io.ipoli.android.quest.Player
import io.realm.Realm
import org.threeten.bp.LocalDateTime

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/2/17.
 */
interface PlayerRepository : Repository<Player> {
    fun get(): Player?
}

class RealmPlayerRepository : BaseRealmRepository<Player, RealmPlayer>(), PlayerRepository {
    override fun convertToRealmModel(entity: Player): RealmPlayer =
        entity.let {
            RealmPlayer(
                id = it.id,
                coins = it.coins,
                experience = it.experience,
                authProvider = it.authProvider)
        }

    override fun convertToEntity(realmModel: RealmPlayer): Player =
        realmModel.let {
            Player(
                id = it.id,
                coins = it.coins,
                experience = it.experience,
                authProvider = it.authProvider,
                createdAt = LocalDateTime.now()
            )
        }

    override fun get(): Player? =
        Realm.getDefaultInstance().use { realm ->
            val realmModel = realm.where(getModelClass()).findFirst() ?: return@use null
            convertToEntity(realmModel)
        }

    override fun getModelClass(): Class<RealmPlayer> = RealmPlayer::class.java

}