package io.ipoli.android.habit.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.*
import io.ipoli.android.common.distinct
import io.ipoli.android.common.persistence.*
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.data.persistence.DbEmbedTag
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.persistence.RoomTag
import io.ipoli.android.tag.persistence.RoomTagMapper
import io.ipoli.android.tag.persistence.TagDao
import kotlinx.coroutines.experimental.channels.Channel
import org.jetbrains.annotations.NotNull
import org.threeten.bp.DayOfWeek
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/16/18.
 */
interface HabitRepository : CollectionRepository<Habit> {

    fun findAllForChallenge(challengeId: String): List<Habit>
}

data class DbHabit(override val map: MutableMap<String, Any?> = mutableMapOf()) :
    FirestoreModel {
    override var id: String by map
    var name: String by map
    var color: String by map
    var icon: String by map
    var tags: Map<String, MutableMap<String, Any?>> by map
    var days: List<String> by map
    var isGood: Boolean by map
    var timesADay: Long by map
    var challengeId: String? by map
    var note: String by map
    var history: Map<String, MutableMap<String, Any?>> by map
    var currentStreak: Long by map
    var prevStreak: Long by map
    var bestStreak: Long by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}

data class DbCompletedEntry(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var completedAtMinutes: List<Long> by map
    var experience: Long? by map
    var coins: Long? by map
}

@Dao
abstract class HabitDao : BaseDao<RoomHabit>() {
    @Query("SELECT * FROM habits")
    abstract fun findAll(): List<RoomHabit>

    @Query("SELECT * FROM habits WHERE id = :id")
    abstract fun findById(id: String): RoomHabit

    @Query("SELECT * FROM habits WHERE challengeId = :challengeId")
    abstract fun findAllForChallenge(challengeId: String): List<RoomHabit>

    @Query("SELECT * FROM habits WHERE removedAt IS NULL")
    abstract fun listenForNotRemoved(): LiveData<List<RoomHabit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    abstract fun listenById(id: String): LiveData<RoomHabit>

    @Query("UPDATE habits $REMOVE_QUERY")
    abstract fun remove(id: String, currentTimeMillis: Long = System.currentTimeMillis())

    @Query("UPDATE habits $UNDO_REMOVE_QUERY")
    abstract fun undoRemove(id: String, currentTimeMillis: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun saveTags(joins: List<RoomHabit.Companion.RoomTagJoin>)

    @Query("DELETE FROM habit_tag_join WHERE habitId = :habitId")
    abstract fun deleteAllTags(habitId: String)

    @Query("DELETE FROM habit_tag_join WHERE habitId IN (:habitIds)")
    abstract fun deleteAllTags(habitIds: List<String>)

    @Query("SELECT * FROM habits $FIND_SYNC_QUERY")
    abstract fun findAllForSync(lastSync: Long): List<RoomHabit>
}

class RoomHabitRoomRepository(dao: HabitDao, tagDao: TagDao) : HabitRepository,
    BaseRoomRepositoryWithTags<Habit, RoomHabit, HabitDao, RoomHabit.Companion.RoomTagJoin>(dao) {

    private val mapper = RoomHabitMapper(tagDao)

    override fun findAllForSync(lastSync: Duration<Millisecond>) =
        dao.findAllForSync(lastSync.millisValue).map { toEntityObject(it) }

    override fun createTagJoin(entityId: String, tagId: String) =
        RoomHabit.Companion.RoomTagJoin(entityId, tagId)

    override fun newIdForEntity(id: String, entity: Habit) = entity.copy(id = id)

    override fun saveTags(joins: List<RoomHabit.Companion.RoomTagJoin>) = dao.saveTags(joins)

    override fun deleteAllTags(entityId: String) = dao.deleteAllTags(entityId)

    override fun deleteAllTags(entityIds: List<String>) = dao.deleteAllTags(entityIds)

    override fun findAllForChallenge(challengeId: String) =
        dao.findAllForChallenge(challengeId).map { toEntityObject(it) }

    override fun findById(id: String) =
        toEntityObject(dao.findById(id))

    override fun findAll() =
        dao.findAll().map { toEntityObject(it) }

    override fun listenById(id: String, channel: Channel<Habit?>) =
        dao.listenById(id).distinct().notifySingle(channel)

    override fun listenForAll(channel: Channel<List<Habit>>) =
        dao.listenForNotRemoved().notify(channel)

    override fun remove(entity: Habit) {
        remove(entity.id)
    }

    override fun remove(id: String) {
        dao.remove(id)
    }

    override fun undoRemove(id: String) {
        dao.undoRemove(id)
    }

    override fun toEntityObject(dbObject: RoomHabit) =
        mapper.toEntityObject(dbObject)

    override fun toDatabaseObject(entity: Habit) =
        mapper.toDatabaseObject(entity)
}

class RoomHabitMapper(private val tagDao: TagDao) {

    private val tagMapper = RoomTagMapper()

    fun toEntityObject(dbObject: RoomHabit) =
        Habit(
            id = dbObject.id,
            name = dbObject.name,
            color = Color.valueOf(dbObject.color),
            icon = Icon.valueOf(dbObject.icon),
            tags = tagDao.findForHabit(dbObject.id).map { tagMapper.toEntityObject(it) },
            days = dbObject.days.map { DayOfWeek.valueOf(it) }.toSet(),
            isGood = dbObject.isGood,
            timesADay = dbObject.timesADay.toInt(),
            challengeId = dbObject.challengeId,
            note = dbObject.note,
            history = dbObject.history.map {
                it.key.toLong().startOfDayUTC to createCompletedEntry(it.value)
            }.toMap(),
            currentStreak = dbObject.currentStreak.toInt(),
            prevStreak = dbObject.prevStreak.toInt(),
            bestStreak = dbObject.bestStreak.toInt(),
            updatedAt = dbObject.updatedAt.instant,
            createdAt = dbObject.createdAt.instant,
            removedAt = dbObject.removedAt?.instant
        )

    fun toDatabaseObject(entity: Habit) =
        RoomHabit(
            id = if (entity.id.isEmpty()) UUID.randomUUID().toString() else entity.id,
            name = entity.name,
            color = entity.color.name,
            icon = entity.icon.name,
            days = entity.days.map { it.name },
            isGood = entity.isGood,
            timesADay = entity.timesADay.toLong(),
            challengeId = entity.challengeId,
            note = entity.note,
            history = entity.history.map {
                it.key.startOfDayUTC().toString() to createDbCompletedEntry(it.value).map
            }.toMap(),
            currentStreak = entity.currentStreak.toLong(),
            prevStreak = entity.prevStreak.toLong(),
            bestStreak = entity.bestStreak.toLong(),
            updatedAt = System.currentTimeMillis(),
            createdAt = entity.createdAt.toEpochMilli(),
            removedAt = entity.removedAt?.toEpochMilli()
        )

    private fun createDbCompletedEntry(completedEntry: CompletedEntry) =
        DbCompletedEntry().apply {
            completedAtMinutes = completedEntry.completedAtTimes.map { it.toMinuteOfDay().toLong() }
            coins = completedEntry.coins?.toLong()
            experience = completedEntry.experience?.toLong()
        }

    private fun createCompletedEntry(dataMap: MutableMap<String, Any?>) =
        with(
            DbCompletedEntry(dataMap.withDefault {
                null
            })
        ) {
            CompletedEntry(
                completedAtTimes = completedAtMinutes.map { Time.of(it.toInt()) },
                coins = coins?.toInt(),
                experience = experience?.toInt()
            )
        }
}


@Entity(
    tableName = "habits",
    indices = [
        Index("challengeId"),
        Index("updatedAt"),
        Index("removedAt")
    ]
)
data class RoomHabit(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    override val id: String,
    val name: String,
    val color: String,
    val icon: String,
    val days: List<String>,
    val isGood: Boolean,
    val timesADay: Long,
    val challengeId: String?,
    val note: String,
    val history: Map<String, MutableMap<String, Any?>>,
    val currentStreak: Long,
    val prevStreak: Long,
    val bestStreak: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val removedAt: Long?
) : RoomEntity {

    companion object {

        @Entity(
            tableName = "habit_tag_join",
            primaryKeys = ["habitId", "tagId"],
            foreignKeys = [
                ForeignKey(
                    entity = RoomHabit::class,
                    parentColumns = ["id"],
                    childColumns = ["habitId"],
                    onDelete = CASCADE
                ),
                ForeignKey(
                    entity = RoomTag::class,
                    parentColumns = ["id"],
                    childColumns = ["tagId"],
                    onDelete = CASCADE
                )
            ],
            indices = [Index("habitId"), Index("tagId")]
        )
        data class RoomTagJoin(val habitId: String, val tagId: String)
    }

}


class FirestoreHabitRepository(
    database: FirebaseFirestore
) : BaseCollectionFirestoreRepository<Habit, DbHabit>(
    database
) {

    override val collectionReference: CollectionReference
        get() = database.collection("players").document(playerId).collection("habits")

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): Habit {
        val h = DbHabit(dataMap.withDefault {
            null
        })

        return Habit(
            id = h.id,
            name = h.name,
            color = Color.valueOf(h.color),
            icon = Icon.valueOf(h.icon),
            tags = h.tags.values.map {
                createTag(it)
            },
            days = h.days.map { DayOfWeek.valueOf(it) }.toSet(),
            isGood = h.isGood,
            timesADay = h.timesADay.toInt(),
            challengeId = h.challengeId,
            note = h.note,
            history = h.history.map {
                it.key.toLong().startOfDayUTC to createCompletedEntry(it.value)
            }.toMap(),
            currentStreak = h.currentStreak.toInt(),
            prevStreak = h.prevStreak.toInt(),
            bestStreak = h.bestStreak.toInt(),
            createdAt = h.createdAt.instant,
            updatedAt = h.updatedAt.instant,
            removedAt = h.removedAt?.instant
        )
    }

    override fun toDatabaseObject(entity: Habit): DbHabit {
        val h = DbHabit()
        h.id = entity.id
        h.name = entity.name
        h.color = entity.color.name
        h.icon = entity.icon.name
        h.tags = entity.tags.map { it.id to createDbTag(it).map }.toMap()
        h.days = entity.days.map { it.name }
        h.isGood = entity.isGood
        h.timesADay = entity.timesADay.toLong()
        h.challengeId = entity.challengeId
        h.note = entity.note
        h.history = entity.history.map {
            it.key.startOfDayUTC().toString() to createDbCompletedEntry(it.value).map
        }.toMap()
        h.currentStreak = entity.currentStreak.toLong()
        h.prevStreak = entity.prevStreak.toLong()
        h.bestStreak = entity.bestStreak.toLong()
        h.updatedAt = entity.updatedAt.toEpochMilli()
        h.createdAt = entity.createdAt.toEpochMilli()
        h.removedAt = entity.removedAt?.toEpochMilli()
        return h
    }

    private fun createDbTag(tag: Tag) =
        DbEmbedTag().apply {
            id = tag.id
            name = tag.name
            isFavorite = tag.isFavorite
            color = tag.color.name
            icon = tag.icon?.name
        }

    private fun createTag(dataMap: MutableMap<String, Any?>) =
        with(
            DbEmbedTag(dataMap.withDefault {
                null
            })
        ) {
            Tag(
                id = id,
                name = name,
                color = Color.valueOf(color),
                icon = icon?.let {
                    Icon.valueOf(it)
                },
                isFavorite = isFavorite
            )
        }

    private fun createDbCompletedEntry(completedEntry: CompletedEntry) =
        DbCompletedEntry().apply {
            completedAtMinutes = completedEntry.completedAtTimes.map { it.toMinuteOfDay().toLong() }
            coins = completedEntry.coins?.toLong()
            experience = completedEntry.experience?.toLong()
        }

    private fun createCompletedEntry(dataMap: MutableMap<String, Any?>) =
        with(
            DbCompletedEntry(dataMap.withDefault {
                null
            })
        ) {
            CompletedEntry(
                completedAtTimes = completedAtMinutes.map { Time.of(it.toInt()) },
                coins = coins?.toInt(),
                experience = experience?.toInt()
            )
        }
}