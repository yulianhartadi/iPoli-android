package io.ipoli.android.quest.data

import io.realm.RealmObject
import java.util.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/19/17.
 */
open class SubQuest : RealmObject {

    var name: String? = null

    var completedAt: Long? = null

    var completedAtMinute: Int? = null

    constructor() {}

    constructor(name: String) {
        this.name = name
    }

    var completedAtDate: Date?
        get() = if (completedAt != null) Date(completedAt!!) else null
        set(completedAtDate) {
            completedAt = if (completedAtDate != null) completedAtDate.getTime() else null
        }

    val isCompleted: Boolean
        get() = completedAtDate != null
}