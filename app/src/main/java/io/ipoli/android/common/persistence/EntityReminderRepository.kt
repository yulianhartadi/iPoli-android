package io.ipoli.android.common.persistence

import android.arch.persistence.room.*
import io.ipoli.android.common.datetime.milliseconds
import io.ipoli.android.common.datetime.seconds
import io.ipoli.android.common.datetime.startOfDayUTC
import org.jetbrains.annotations.NotNull
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 06/26/2018.
 */

interface EntityReminderRepository {
    fun findNextReminderTime(afterTime: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())): LocalDateTime?
}

class RoomEntityReminderRepository(private val dao: EntityReminderDao) : EntityReminderRepository {

    override fun findNextReminderTime(afterTime: ZonedDateTime): LocalDateTime? {
        val currentDateMillis = afterTime.toLocalDate().startOfDayUTC()

        val millisOfDay = afterTime.toLocalTime().toSecondOfDay().seconds.millisValue

        val r = dao.findAfter(currentDateMillis, millisOfDay) ?: return null

        return LocalDateTime.of(
            r.date.startOfDayUTC,
            LocalTime.ofSecondOfDay(r.millisOfDay.milliseconds.asSeconds.longValue)
        )
    }
}

@Dao
interface EntityReminderDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun save(entity: RoomEntityReminder)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun saveAll(entities: List<RoomEntityReminder>)

    @Query("DELETE FROM entity_reminders WHERE entityId = :entityId")
    fun purgeForEntity(entityId: String)

    @Query("DELETE FROM entity_reminders WHERE entityId IN (:entityIds)")
    fun purgeForEntities(entityIds: List<String>)

    @Query(
        """
        SELECT *
        FROM entity_reminders
        WHERE date >= :date AND millisOfDay > :millisOfDay
        ORDER BY date ASC, millisOfDay ASC
        LIMIT 1
        """
    )
    fun findAfter(date: Long, millisOfDay: Long): RoomEntityReminder?
}

@Entity(
    tableName = "entity_reminders",
    indices = [
        Index("date"),
        Index("millisOfDay"),
        Index("entityType"),
        Index("entityId")
    ]
)
data class RoomEntityReminder(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val date: Long,
    val millisOfDay: Long,
    val entityType: String,
    val entityId: String
) {
    enum class EntityType { QUEST }
}