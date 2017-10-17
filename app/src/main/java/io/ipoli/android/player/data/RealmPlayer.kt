package io.ipoli.android.player.data

import io.ipoli.android.common.persistence.PersistedModel
import io.ipoli.android.player.auth.AuthProvider
import io.ipoli.android.store.avatars.data.Avatar
import java.util.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/2/17.
 */
open class CouchbasePlayer(

    override var id: String = "",
    var coins: Int = 0,
    var experience: Int = 0,
    var authProvider: AuthProvider? = null,
    var avatarCode: Int = Avatar.IPOLI_CLASSIC.code,
    override var createdAt: Long = Date().time,
    override var updatedAt: Long = Date().time,
    override var removedAt: Long? = null
) : PersistedModel