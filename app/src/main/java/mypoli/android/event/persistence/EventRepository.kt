package mypoli.android.event.persistence

import android.content.ContentUris
import android.database.Cursor
import android.provider.CalendarContract
import android.provider.CalendarContract.Instances
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import mypoli.android.common.datetime.Time
import mypoli.android.common.datetime.instant
import mypoli.android.common.datetime.minutes
import mypoli.android.common.persistence.CollectionRepository
import mypoli.android.common.view.ColorUtil
import mypoli.android.event.Event
import mypoli.android.myPoliApp
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.*


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/09/2018.
 */

interface EventRepository : CollectionRepository<Event> {
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
            Instances.BEGIN,
            Instances.END,
            Instances.START_MINUTE,
            Instances.END_MINUTE,
            Instances.TITLE,
            Instances.EVENT_LOCATION,
            Instances.DURATION,
            Instances.CALENDAR_TIME_ZONE,
            Instances.DISPLAY_COLOR,
            Instances.RRULE
        )

        private const val PROJECTION_ID_INDEX = 0
        private const val PROJECTION_BEGIN_INDEX = 1
        private const val PROJECTION_END_INDEX = 2
        private const val PROJECTION_START_MIN_INDEX = 3
        private const val PROJECTION_END_MIN_INDEX = 4
        private const val PROJECTION_TITLE_INDEX = 5
        private const val PROJECTION_LOCATION_INDEX = 6
        private const val PROJECTION_DURATION_INDEX = 7
        private const val PROJECTION_TIME_ZONE_INDEX = 8
        private const val PROJECTION_DISPLAY_COLOR = 9
        private const val PROJECTION_RRULE = 10
    }

    override fun findScheduledBetween(
        calendarIds: Set<Int>,
        start: LocalDate,
        end: LocalDate
    ): List<Event> {

        val beginTime = Calendar.getInstance()
        beginTime.set(start.year, start.monthValue - 1, start.dayOfMonth, 0, 0, 0)

        val endTime = Calendar.getInstance()
        endTime.set(end.year, end.monthValue - 1, end.dayOfMonth, 23, 59, 59)

        val builder = Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, beginTime.timeInMillis)
        ContentUris.appendId(builder, endTime.timeInMillis)

        val selection = (CalendarContract.Events.CALENDAR_ID + " = ?")
        val selectionArgs = calendarIds.map { it.toString() }.toTypedArray()

        val events = mutableListOf<Event>()

        myPoliApp.instance.contentResolver.query(
            builder.build(),
            INSTANCE_PROJECTION,
            selection,
            selectionArgs,
            null
        ).use {
            while (it.moveToNext()) {
                events.add(createEvent(it))
            }
        }

        return events
    }

    private fun createEvent(cursor: Cursor): Event {
        val eventStartTime = Time.of(cursor.getInt(PROJECTION_START_MIN_INDEX))
        val eventEndTime = Time.of(cursor.getInt(PROJECTION_END_MIN_INDEX))
        val tz = ZoneId.of(cursor.getString(PROJECTION_TIME_ZONE_INDEX))
        val rRule = cursor.getString(PROJECTION_RRULE)
        return Event(
            id = cursor.getString(PROJECTION_ID_INDEX),
            name = cursor.getString(PROJECTION_TITLE_INDEX),
            startTime = eventStartTime,
            duration = (eventEndTime - eventStartTime).toMinuteOfDay().minutes,
            startDate = cursor.getLong(PROJECTION_BEGIN_INDEX).instant.atZone(tz).toLocalDate(),
            endDate = cursor.getLong(PROJECTION_END_INDEX).instant.atZone(tz).toLocalDate(),
            color = ColorUtil.fromGoogleCalendarDisplayColor(cursor.getInt(PROJECTION_DISPLAY_COLOR)),
            isRepeating = rRule != null && rRule.isNotEmpty()
        )
    }

    override fun save(entity: Event): Event {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findById(id: String): Event? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listenById(id: String): ReceiveChannel<Event?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listenForAll(): ReceiveChannel<List<Event>> {
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