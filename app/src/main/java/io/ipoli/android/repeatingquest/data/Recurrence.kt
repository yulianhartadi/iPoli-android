package io.ipoli.android.repeatingquest.data

import io.ipoli.android.common.datetime.DateUtils
import io.realm.RealmObject
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov venelin@curiousily.com>
 * on 8/22/17.
 */
open class Recurrence : RealmObject() {

    enum class RepeatType {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    var flexibleCount: Int = 0

    var rrule: String? = null

    var rdate: String? = null

    var exrule: String? = null
    var exdate: String? = null
    var dtstart: Long? = null
    var dtend: Long? = null
    var repeatType: String? = null

    var dtstartDate: LocalDate?
        get() = if (dtstart != null) DateUtils.fromMillis(dtstart!!) else null
        set(dtstartDate) {
            dtstart = if (dtstartDate != null) DateUtils.toMillis(dtstartDate) else null
        }

    var dtendDate: LocalDate?
        get() = if (dtend != null) DateUtils.fromMillis(dtend!!) else null
        set(dtendDate) {
            dtend = if (dtendDate != null) DateUtils.toMillis(dtendDate) else null
        }

    var recurrenceType: RepeatType
        get() = RepeatType.valueOf(repeatType!!)
        set(type) {
            this.repeatType = type.name
        }

    val isFlexible: Boolean
        get() = flexibleCount > 0

    companion object {

        val RRULE_EVERY_DAY = "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR,SA,SU"

        fun create(): Recurrence {
            val recurrence = Recurrence()
            recurrence.recurrenceType = RepeatType.DAILY
            recurrence.dtstartDate = LocalDate.now()
            recurrence.flexibleCount = 0
            return recurrence
        }
    }
}