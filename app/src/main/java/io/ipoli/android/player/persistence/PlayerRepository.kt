package io.ipoli.android.player.persistence

import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.player.data.Player
import io.realm.Realm

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/17.
 */
interface PlayerRepository : Repository<Player> {
    fun get(): Player?
}

class RealmPlayerRepository : BaseRealmRepository<Player>(), PlayerRepository {
    override fun get(): Player? =
        Realm.getDefaultInstance().use { realm ->
            realm.where(getModelClass()).findFirst()
        }

    override fun getModelClass(): Class<Player> = Player::class.java

}