package io.ipoli.android.quest.data

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/19/17.
 */

class SourceMapping private constructor() {
    private var androidCalendarMapping: AndroidCalendarMapping? = null

    companion object {

        fun fromGoogleCalendar(calendarId: Long, eventId: Long): SourceMapping {
            val sourceMapping = SourceMapping()
            sourceMapping.androidCalendarMapping = AndroidCalendarMapping(calendarId, eventId)
            return sourceMapping
        }
    }
}

class AndroidCalendarMapping {
    var calendarId: Long? = null
    var eventId: Long? = null

    constructor()

    constructor(calendarId: Long?, eventId: Long?) {
        this.calendarId = calendarId
        this.eventId = eventId
    }
}