package io.ipoli.android.player

import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/17.
 */
interface PlayerRepository : Repository<Player>

class RealmPlayerRepository : BaseRealmRepository<Player>(), PlayerRepository {

    override fun getModelClass(): Class<Player> = Player::class.java
}