package io.ipoli.android.dailychallenge.data.persistence

import android.arch.persistence.room.*
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.Reward
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Millisecond
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.BaseCollectionFirestoreRepository
import io.ipoli.android.common.persistence.BaseDao
import io.ipoli.android.common.persistence.BaseRoomRepository
import io.ipoli.android.common.persistence.FirestoreModel
import io.ipoli.android.dailychallenge.data.DailyChallenge
import io.ipoli.android.pet.Food
import io.ipoli.android.player.data.Player
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.DbBounty
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

    @android.arch.persistence.room.Query("SELECT * FROM daily_challenges")
    abstract fun findAll(): List<RoomDailyChallenge>

    @android.arch.persistence.room.Query("SELECT * FROM daily_challenges WHERE date = :date")
    abstract fun findForDate(date: Long): RoomDailyChallenge?

    @android.arch.persistence.room.Query("SELECT * FROM daily_challenges WHERE date < :date AND isCompleted = 0 ORDER BY date DESC LIMIT 1")
    abstract fun findLastIncomplete(date: Long): RoomDailyChallenge?

    @android.arch.persistence.room.Query("SELECT COUNT(*) FROM daily_challenges WHERE isCompleted = 1 AND date < :date")
    abstract fun countCompletedBefore(date: Long): Int

    @android.arch.persistence.room.Query("SELECT COUNT(*) FROM daily_challenges WHERE date > :startDate AND date <= :endDate AND isCompleted = 1")
    abstract fun countInRange(startDate: Long, endDate: Long): Int

    @Query("SELECT * FROM daily_challenges $FIND_SYNC_QUERY")
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
            reward = dbObject.coins?.let {
                val dbBounty = DbBounty(dbObject.bounty!!.toMutableMap())
                Reward(
                    attributePoints = dbObject.attributePoints!!.map { a ->
                        Player.AttributeType.valueOf(
                            a.key
                        ) to a.value.toInt()
                    }.toMap(),
                    healthPoints = 0,
                    experience = dbObject.experience!!.toInt(),
                    coins = dbObject.coins.toInt(),
                    bounty = when {
                        dbBounty.type == DbBounty.Type.NONE.name -> Quest.Bounty.None
                        dbBounty.type == DbBounty.Type.FOOD.name -> Quest.Bounty.Food(
                            Food.valueOf(
                                dbBounty.name!!
                            )
                        )
                        else -> throw IllegalArgumentException("Unknown bounty type ${dbBounty.type}")
                    }
                )
            },
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
            experience = entity.reward?.experience?.toLong(),
            coins = entity.reward?.coins?.toLong(),
            attributePoints = entity.reward?.attributePoints?.map { a -> a.key.name to a.value.toLong() }?.toMap(),
            bounty = entity.reward?.let {
                DbBounty().apply {
                    type = when (it.bounty) {
                        is Quest.Bounty.None -> DbBounty.Type.NONE.name
                        is Quest.Bounty.Food -> DbBounty.Type.FOOD.name
                    }
                    name = if (it.bounty is Quest.Bounty.Food) it.bounty.food.name else null
                }.map
            },
            createdAt = entity.createdAt.toEpochMilli(),
            updatedAt = System.currentTimeMillis(),
            removedAt = entity.removedAt?.toEpochMilli()
        )
}


@Entity(
    tableName = "daily_challenges",
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
    val experience: Long?,
    val coins: Long?,
    val bounty: Map<String, Any?>?,
    val attributePoints: Map<String, Long>?,
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
    var experience: Long? by map
    var coins: Long? by map
    var bounty: Map<String, Any?>? by map
    var attributePoints: Map<String, Long>? by map
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
            reward = dc.coins?.let {
                val dbBounty = DbBounty(dc.bounty!!.toMutableMap())
                Reward(
                    attributePoints = dc.attributePoints!!.map { a ->
                        Player.AttributeType.valueOf(
                            a.key
                        ) to a.value.toInt()
                    }.toMap(),
                    healthPoints = 0,
                    experience = dc.experience!!.toInt(),
                    coins = dc.coins!!.toInt(),
                    bounty = when {
                        dbBounty.type == DbBounty.Type.NONE.name -> Quest.Bounty.None
                        dbBounty.type == DbBounty.Type.FOOD.name -> Quest.Bounty.Food(
                            Food.valueOf(
                                dbBounty.name!!
                            )
                        )
                        else -> throw IllegalArgumentException("Unknown bounty type ${dbBounty.type}")
                    }
                )
            },
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
            entity.reward?.let { r ->
                it.experience = r.experience.toLong()
                it.coins = r.coins.toLong()
                it.attributePoints =
                    r.attributePoints.map { a -> a.key.name to a.value.toLong() }.toMap()
                it.bounty = DbBounty().apply {
                    type = when (r.bounty) {
                        is Quest.Bounty.None -> DbBounty.Type.NONE.name
                        is Quest.Bounty.Food -> DbBounty.Type.FOOD.name
                    }
                    name = if (r.bounty is Quest.Bounty.Food) r.bounty.food.name else null
                }.map
            }
            it.createdAt = entity.createdAt.toEpochMilli()
            it.updatedAt = entity.updatedAt.toEpochMilli()
            it.removedAt = entity.removedAt?.toEpochMilli()
        }

}