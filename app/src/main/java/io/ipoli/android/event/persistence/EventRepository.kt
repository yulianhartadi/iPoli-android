package io.ipoli.android.event.persistence

import android.content.ContentUris
import android.database.Cursor
import android.provider.CalendarContract
import android.provider.CalendarContract.Instances
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.persistence.CollectionRepository
import io.ipoli.android.common.view.ColorUtil
import io.ipoli.android.event.Event
import io.ipoli.android.myPoliApp
import kotlinx.coroutines.experimental.channels.Channel
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.JulianFields


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/09/2018.
 */

interface EventRepository : CollectionRepository<Event> {

    fun findScheduledAt(calendarIds: Set<Int>, date: LocalDate): List<Event>

    fun findScheduledBetween(
        calendarIds: Set<Int>,
        start: LocalDate,
        end: LocalDate
    ): List<Event>
}

class AndroidCalendarEventRepository : EventRepository {

    companion object {


        private val INSTANCE_PROJECTION = arrayOf(
            Instances.EVENT_ID,
            Instances.START_DAY,
            Instances.END_DAY,
            Instances.START_MINUTE,
            Instances.END_MINUTE,
            Instances.TITLE,
            Instances.EVENT_LOCATION,
            Instances.DURATION,
            Instances.CALENDAR_TIME_ZONE,
            Instances.DISPLAY_COLOR,
            Instances.RRULE,
            Instances.ALL_DAY
        )

        private const val PROJECTION_ID_INDEX = 0
        private const val PROJECTION_BEGIN_DAY_INDEX = 1
        private const val PROJECTION_END_DAY_INDEX = 2
        private const val PROJECTION_START_MIN_INDEX = 3
        private const val PROJECTION_END_MIN_INDEX = 4
        private const val PROJECTION_TITLE_INDEX = 5
        private const val PROJECTION_LOCATION_INDEX = 6
        private const val PROJECTION_DURATION_INDEX = 7
        private const val PROJECTION_TIME_ZONE_INDEX = 8
        private const val PROJECTION_DISPLAY_COLOR = 9
        private const val PROJECTION_RRULE = 10
        private const val PROJECTION_ALL_DAY = 11
    }

    override fun findAll(): List<Event> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findScheduledAt(calendarIds: Set<Int>, date: LocalDate) =
        findScheduledBetween(calendarIds, date, date.plusDays(1))

    override fun findScheduledBetween(
        calendarIds: Set<Int>,
        start: LocalDate,
        end: LocalDate
    ): List<Event> {

        val builder = Instances.CONTENT_BY_DAY_URI.buildUpon()
        ContentUris.appendId(builder, start.getLong(JulianFields.JULIAN_DAY))
        ContentUris.appendId(builder, end.getLong(JulianFields.JULIAN_DAY))

        val selection = (CalendarContract.Events.CALENDAR_ID + " = ?")

        val events = mutableListOf<Event>()

        calendarIds.forEach {
            val selectionArgs = listOf(it.toString()).toTypedArray()
            myPoliApp.instance.contentResolver.query(
                builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null
            ).use {
                while (it.moveToNext()) {
                    val e = createEvent(it)
                    if (e.startDate >= start && e.endDate <= end) {
                        events.add(e)
                    }
                }
            }
        }

        return events
    }

    private fun createEvent(cursor: Cursor): Event {
        val eventStartTime = Time.of(cursor.getInt(PROJECTION_START_MIN_INDEX))
        val eventEndTime = Time.of(cursor.getInt(PROJECTION_END_MIN_INDEX))

        val rRule = cursor.getString(PROJECTION_RRULE)
        val title = if (cursor.isNull(PROJECTION_TITLE_INDEX))
            "(No title)"
        else
            cursor.getString(PROJECTION_TITLE_INDEX)

        val startDate =
            LocalDate.now()
                .with(JulianFields.JULIAN_DAY, cursor.getLong(PROJECTION_BEGIN_DAY_INDEX))

        val endDate =
            LocalDate.now()
                .with(JulianFields.JULIAN_DAY, cursor.getLong(PROJECTION_END_DAY_INDEX))

        val isAllDay =
            if (cursor.isNull(PROJECTION_ALL_DAY))
                false
            else
                cursor.getInt(PROJECTION_ALL_DAY) == 1

        return Event(
            id = cursor.getString(PROJECTION_ID_INDEX),
            name = if (title.isBlank()) "(No title)" else title,
            startTime = eventStartTime,
            endTime = eventEndTime,
            startDate = startDate,
            endDate = endDate,
            color = ColorUtil.fromGoogleCalendarDisplayColor(cursor.getInt(PROJECTION_DISPLAY_COLOR)),
            isRepeating = rRule != null && rRule.isNotEmpty(),
            isAllDay = isAllDay
        )
    }

    override fun save(entity: Event): Event {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(entities: List<Event>): List<Event> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findById(id: String): Event? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listenById(id: String): Channel<Event?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listenForAll(): Channel<List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(entity: Event) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(id: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun undoRemove(id: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}