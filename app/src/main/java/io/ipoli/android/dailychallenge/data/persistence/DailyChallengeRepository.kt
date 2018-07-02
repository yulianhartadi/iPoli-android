package io.ipoli.android.dailychallenge.data.persistence

import android.arch.persistence.room.*
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Millisecond
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.BaseCollectionFirestoreRepository
import io.ipoli.android.common.persistence.BaseDao
import io.ipoli.android.common.persistence.BaseRoomRepository
import io.ipoli.android.common.persistence.FirestoreModel
import io.ipoli.android.dailychallenge.data.DailyChallenge
import org.jetbrains.annotations.NotNull
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/28/18.
 */
interface DailyChallengeRepository {

    fun findAllForSync(lastSync: Duration<Millisecond>): List<DailyChallenge>

    fun findDailyChallengeStreak(currentDate: LocalDate = LocalDate.now()): Int

    fun findForDate(currentDate: LocalDate = LocalDate.now()): DailyChallenge?

    fun save(entity: DailyChallenge): DailyChallenge

    fun save(entities: List<DailyChallenge>): List<DailyChallenge>
    fun findAll(): List<DailyChallenge>
}

@Dao
abstract class DailyChallengeDao : BaseDao<RoomDailyChallenge>() {

    @android.arch.persistence.room.Query("SELECT * FROM dailyChallenges")
    abstract fun findAll(): List<RoomDailyChallenge>

    @android.arch.persistence.room.Query("SELECT * FROM dailyChallenges WHERE date = :date")
    abstract fun findForDate(date: Long): RoomDailyChallenge?

    @android.arch.persistence.room.Query("SELECT * FROM dailyChallenges WHERE date < :date AND isCompleted = 0 ORDER BY date DESC LIMIT 1")
    abstract fun findLastIncomplete(date: Long): RoomDailyChallenge?

    @android.arch.persistence.room.Query("SELECT COUNT(*) FROM dailyChallenges WHERE isCompleted = 1 AND date < :date")
    abstract fun countCompletedBefore(date: Long): Int

    @android.arch.persistence.room.Query("SELECT COUNT(*) FROM dailyChallenges WHERE date > :startDate AND date <= :endDate AND isCompleted = 1")
    abstract fun countInRange(startDate: Long, endDate: Long): Int

    @Query("SELECT * FROM dailyChallenges $FIND_SYNC_QUERY")
    abstract fun findAllForSync(lastSync: Long): List<RoomDailyChallenge>
}

class RoomDailyChallengeRepository(dao: DailyChallengeDao) : DailyChallengeRepository,
    BaseRoomRepository<DailyChallenge, RoomDailyChallenge, DailyChallengeDao>(dao) {

    override fun findAllForSync(lastSync: Duration<Millisecond>) =
        dao.findAllForSync(lastSync.millisValue).map { toEntityObject(it) }

    override fun findAll() = dao.findAll().map { toEntityObject(it) }

    override fun findDailyChallengeStreak(currentDate: LocalDate): Int {
        val lastIncomplete = dao.findLastIncomplete(currentDate.startOfDayUTC())
        return if (lastIncomplete == null) {
            dao.countCompletedBefore(currentDate.startOfDayUTC())
        } else {
            dao.countInRange(lastIncomplete.date, currentDate.startOfDayUTC())
        }
    }

    override fun findForDate(currentDate: LocalDate) =
        dao.findForDate(currentDate.startOfDayUTC())?.let { toEntityObject(it) }

    override fun save(entity: DailyChallenge): DailyChallenge {
        val rEntity = toDatabaseObject(entity)
        dao.save(rEntity)
        return entity.copy(id = rEntity.id)
    }

    override fun save(entities: List<DailyChallenge>): List<DailyChallenge> {
        val rEntities = entities.map { toDatabaseObject(it) }
        dao.saveAll(rEntities)
        return rEntities.mapIndexed { i, rdc ->
            entities[i].copy(id = rdc.id)
        }
    }

    override fun toEntityObject(dbObject: RoomDailyChallenge) =
        DailyChallenge(
            id = dbObject.id,
            date = dbObject.date.startOfDayUTC,
            questIds = dbObject.questIds,
            isCompleted = dbObject.isCompleted,
            createdAt = dbObject.createdAt.instant,
            updatedAt = dbObject.updatedAt.instant,
            removedAt = dbObject.removedAt?.instant
        )

    override fun toDatabaseObject(entity: DailyChallenge) =
        RoomDailyChallenge(
            id = if (entity.id.isEmpty()) UUID.randomUUID().toString() else entity.id,
            date = entity.date.startOfDayUTC(),
            questIds = entity.questIds,
            isCompleted = entity.isCompleted,
            createdAt = entity.createdAt.toEpochMilli(),
            updatedAt = System.currentTimeMillis(),
            removedAt = entity.removedAt?.toEpochMilli()
        )
}


@Entity(
    tableName = "dailyChallenges",
    indices = [
        Index("date"),
        Index("isCompleted"),
        Index("updatedAt"),
        Index("removedAt")
    ]
)
data class RoomDailyChallenge(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val date: Long,
    val questIds: List<String>,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val removedAt: Long?
)

data class DbDailyChallenge(override val map: MutableMap<String, Any?> = mutableMapOf()) :
    FirestoreModel {
    override var id: String by map
    var date: Long by map
    var questIds: List<String> by map
    var isCompleted: Boolean by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}

class FirestoreDailyChallengeRepository(
    database: FirebaseFirestore
) : BaseCollectionFirestoreRepository<DailyChallenge, DbDailyChallenge>(
    database
) {

    override val collectionReference
        get() = database.collection("players").document(playerId).collection("dailyChallenges")

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): DailyChallenge {
        val dc = DbDailyChallenge(dataMap.withDefault {
            null
        })
        return DailyChallenge(
            id = dc.id,
            date = dc.date.startOfDayUTC,
            questIds = dc.questIds,
            isCompleted = dc.isCompleted,
            createdAt = dc.createdAt.instant,
            updatedAt = dc.updatedAt.instant,
            removedAt = dc.removedAt?.instant
        )
    }

    override fun toDatabaseObject(entity: DailyChallenge) =
        DbDailyChallenge().also {
            it.id = entity.id
            it.date = entity.date.startOfDayUTC()
            it.questIds = entity.questIds
            it.isCompleted = entity.isCompleted
            it.createdAt = entity.createdAt.toEpochMilli()
            it.updatedAt = entity.updatedAt.toEpochMilli()
            it.removedAt = entity.removedAt?.toEpochMilli()
        }

}