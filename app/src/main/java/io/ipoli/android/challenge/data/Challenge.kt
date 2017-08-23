package io.ipoli.android.challenge.data

import io.ipoli.android.Constants
import io.ipoli.android.common.data.Difficulty
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.persistence.PersistedModel
import io.ipoli.android.quest.data.Category
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/17.
 */
open class Challenge : RealmObject, PersistedModel {

    @PrimaryKey
    override var id: String = ""

    var name: String? = null

    var category: String? = null

    var reasons: String? = null

    var expectedResults: String? = null

    var difficulty: Int? = null

    var end: Long? = null

    var completedAt: Long? = null

    var coins: Long? = null
    var experience: Long? = null

    private var createdAt: Long? = null
    private var updatedAt: Long? = null

    var source: String? = null

    constructor()

    constructor(name: String) {
        this.name = name
        this.category = Category.PERSONAL.name
        this.source = Constants.API_RESOURCE_SOURCE
        createdAt = DateUtils.nowUTC().time
        updatedAt = DateUtils.nowUTC().time
    }

    fun setDifficultyType(difficulty: Difficulty) {
        this.difficulty = difficulty.value
    }

    var endDate: LocalDate?
        get() = if (end != null) DateUtils.fromMillis(end!!) else null
        set(endDate) {
            end = if (endDate != null) DateUtils.toMillis(endDate) else null
        }

    var completedAtDate: Date?
        get() = if (completedAt != null) Date(completedAt!!) else null
        set(completedAtDate) {
            completedAt = completedAtDate?.time
        }

    var categoryType: Category
        get() = Category.valueOf(category!!)
        set(category) {
            this.category = category.name
        }
}
