package io.ipoli.android.quest.data

import io.realm.RealmObject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/19/17.
 */

open class SourceMapping : RealmObject {
    private var androidCalendarMapping: AndroidCalendarMapping? = null

    constructor()

    companion object {

        fun fromGoogleCalendar(calendarId: Long, eventId: Long): SourceMapping {
            val sourceMapping = SourceMapping()
            sourceMapping.androidCalendarMapping = AndroidCalendarMapping(calendarId, eventId)
            return sourceMapping
        }
    }
}

open class AndroidCalendarMapping : RealmObject {
    var calendarId: Long? = null
    var eventId: Long? = null

    constructor()

    constructor(calendarId: Long?, eventId: Long?) {
        this.calendarId = calendarId
        this.eventId = eventId
    }
}