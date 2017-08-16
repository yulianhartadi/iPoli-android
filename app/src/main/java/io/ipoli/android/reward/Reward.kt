package io.ipoli.android.reward

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 7/7/17.
 */
open class Reward(
        @PrimaryKey var id: String = "",
        var name: String = "",
        var description: String = "",
        var price: Int = 0
) : RealmObject()