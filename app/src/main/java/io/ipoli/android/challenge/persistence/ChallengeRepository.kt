package io.ipoli.android.challenge.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.datetime.*
import io.ipoli.android.common.persistence.*
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.data.persistence.DbEmbedTag
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.persistence.RoomTag
import io.ipoli.android.tag.persistence.RoomTagMapper
import io.ipoli.android.tag.persistence.TagDao
import org.jetbrains.annotations.NotNull
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/07/2018.
 */
interface ChallengeRepository : CollectionRepository<Challenge> {
}

@Dao
abstract class ChallengeDao : BaseDao<RoomChallenge>() {

    @Query("SELECT * FROM challenges")
    abstract fun findAll(): List<RoomChallenge>

    @Query("SELECT * FROM challenges WHERE id = :id")
    abstract fun findById(id: String): RoomChallenge

    @Query("SELECT * FROM challenges WHERE removedAt IS NULL")
    abstract fun listenForNotRemoved(): LiveData<List<RoomChallenge>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    abstract fun listenById(id: String): LiveData<RoomChallenge>

    @Query("UPDATE challenges $REMOVE_QUERY")
    abstract fun remove(id: String, currentTimeMillis: Long = System.currentTimeMillis())

    @Query("UPDATE challenges $UNDO_REMOVE_QUERY")
    abstract fun undoRemove(id: String, currentTimeMillis: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun saveTags(joins: List<RoomChallenge.Companion.RoomTagJoin>)

    @Query("DELETE FROM challenge_tag_join WHERE challengeId = :challengeId")
    abstract fun deleteAllTags(challengeId: String)

    @Query("DELETE FROM challenge_tag_join WHERE challengeId IN (:challengeIds)")
    abstract fun deleteAllTags(challengeIds: List<String>)

    @Query("SELECT * FROM challenges $FIND_SYNC_QUERY")
    abstract fun findAllForSync(lastSync: Long): List<RoomChallenge>
}

class RoomChallengeRepository(dao: ChallengeDao, private val tagDao: TagDao) : ChallengeRepository,
    BaseRoomRepositoryWithTags<Challenge, RoomChallenge, ChallengeDao, RoomChallenge.Companion.RoomTagJoin>(
        dao
    ) {

    override fun findAllForSync(lastSync: Duration<Millisecond>) =
        dao.findAllForSync(lastSync.millisValue).map { toEntityObject(it) }

    override fun createTagJoin(
        entityId: String,
        tagId: String
    ) = RoomChallenge.Companion.RoomTagJoin(entityId, tagId)

    override fun newIdForEntity(id: String, entity: Challenge) = entity.copy(id = id)

    override fun saveTags(joins: List<RoomChallenge.Companion.RoomTagJoin>) = dao.saveTags(joins)

    override fun deleteAllTags(entityId: String) = dao.deleteAllTags(entityId)

    override fun deleteAllTags(entityIds: List<String>) = dao.deleteAllTags(entityIds)

    override fun findById(id: String) = toEntityObject(dao.findById(id))

    override fun findAll() = dao.findAll().map { toEntityObject(it) }

    override fun listenById(id: String) =
        dao.listenById(id).notifySingle()

    override fun listenForAll() =
        dao.listenForNotRemoved().notify()

    override fun remove(entity: Challenge) {
        remove(entity.id)
    }

    override fun remove(id: String) {
        dao.remove(id)
    }

    override fun undoRemove(id: String) {
        dao.undoRemove(id)
    }

    private val tagMapper = RoomTagMapper()

    override fun toEntityObject(dbObject: RoomChallenge) =
        Challenge(
            id = dbObject.id,
            name = dbObject.name,
            color = Color.valueOf(dbObject.color),
            icon = dbObject.icon?.let {
                Icon.valueOf(it)
            },
            difficulty = Challenge.Difficulty.valueOf(dbObject.difficulty),
            tags = tagDao.findForChallenge(dbObject.id).map { tagMapper.toEntityObject(it) },
            startDate = dbObject.startDate.startOfDayUTC,
            endDate = dbObject.endDate.startOfDayUTC,
            motivations = dbObject.motivations,
            experience = dbObject.experience?.toInt(),
            coins = dbObject.coins?.toInt(),
            completedAtDate = dbObject.completedAtDate?.startOfDayUTC,
            completedAtTime = dbObject.completedAtMinute?.let {
                Time.of(it.toInt())
            },
            note = dbObject.note,
            createdAt = dbObject.createdAt.instant,
            updatedAt = dbObject.updatedAt.instant,
            removedAt = dbObject.removedAt?.instant
        )

    override fun toDatabaseObject(entity: Challenge) =
        RoomChallenge(
            id = if (entity.id.isEmpty()) UUID.randomUUID().toString() else entity.id,
            name = entity.name,
            color = entity.color.name,
            icon = entity.icon?.name,
            difficulty = entity.difficulty.name,
            startDate = entity.startDate.startOfDayUTC(),
            endDate = entity.endDate.startOfDayUTC(),
            motivations = entity.motivations,
            experience = entity.experience?.toLong(),
            coins = entity.coins?.toLong(),
            completedAtDate = entity.completedAtDate?.startOfDayUTC(),
            completedAtMinute = entity.completedAtTime?.toMinuteOfDay()?.toLong(),
            note = entity.note,
            updatedAt = System.currentTimeMillis(),
            createdAt = entity.createdAt.toEpochMilli(),
            removedAt = entity.removedAt?.toEpochMilli()
        )
}

@Entity(
    tableName = "challenges",
    indices = [
        Index("updatedAt"),
        Index("removedAt")
    ]
)
data class RoomChallenge(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    override val id: String,
    val name: String,
    val color: String,
    val icon: String?,
    val difficulty: String,
    val startDate: Long,
    val endDate: Long,
    val motivations: List<String>,
    val experience: Long?,
    val coins: Long?,
    val completedAtDate: Long?,
    val completedAtMinute: Long?,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val removedAt: Long?
) : RoomEntity {
    companion object {

        @Entity(
            tableName = "challenge_tag_join",
            primaryKeys = ["challengeId", "tagId"],
            foreignKeys = [
                ForeignKey(
                    entity = RoomChallenge::class,
                    parentColumns = ["id"],
                    childColumns = ["challengeId"],
                    onDelete = CASCADE
                ),
                ForeignKey(
                    entity = RoomTag::class,
                    parentColumns = ["id"],
                    childColumns = ["tagId"],
                    onDelete = CASCADE
                )
            ],
            indices = [Index("challengeId"), Index("tagId")]
        )
        data class RoomTagJoin(val challengeId: String, val tagId: String)
    }
}

class FirestoreChallengeRepository(
    database: FirebaseFirestore
) : BaseCollectionFirestoreRepository<Challenge, DbChallenge>(
    database
) {


    override val collectionReference: CollectionReference
        get() {
            return database.collection("players").document(playerId).collection("challenges")
        }

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): Challenge {
        val c = DbChallenge(dataMap.withDefault {
            null
        })

        return Challenge(
            id = c.id,
            name = c.name,
            color = Color.valueOf(c.color),
            icon = c.icon?.let {
                Icon.valueOf(it)
            },
            difficulty = Challenge.Difficulty.valueOf(c.difficulty),
            tags = c.tags.values.map {
                createTag(it)
            },
            startDate = c.startDate.startOfDayUTC,
            endDate = c.endDate.startOfDayUTC,
            motivations = c.motivations,
            experience = c.experience?.toInt(),
            coins = c.coins?.toInt(),
            completedAtDate = c.completedAtDate?.startOfDayUTC,
            completedAtTime = c.completedAtMinute?.let {
                Time.of(it.toInt())
            },
            note = c.note,
            createdAt = c.createdAt.instant,
            updatedAt = c.updatedAt.instant,
            removedAt = c.removedAt?.instant
        )
    }

    override fun toDatabaseObject(entity: Challenge): DbChallenge {
        val c = DbChallenge()
        c.id = entity.id
        c.name = entity.name
        c.color = entity.color.name
        c.icon = entity.icon?.name
        c.difficulty = entity.difficulty.name
        c.tags = entity.tags.map { it.id to createDbTag(it).map }.toMap()
        c.startDate = entity.startDate.startOfDayUTC()
        c.endDate = entity.endDate.startOfDayUTC()
        c.motivations = entity.motivations
        c.experience = entity.experience?.toLong()
        c.coins = entity.coins?.toLong()
        c.completedAtDate = entity.completedAtDate?.startOfDayUTC()
        c.completedAtMinute = entity.completedAtTime?.toMinuteOfDay()?.toLong()
        c.note = entity.note
        c.updatedAt = entity.updatedAt.toEpochMilli()
        c.createdAt = entity.createdAt.toEpochMilli()
        c.removedAt = entity.removedAt?.toEpochMilli()
        return c
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
}


data class DbChallenge(override val map: MutableMap<String, Any?> = mutableMapOf()) :
    FirestoreModel {
    override var id: String by map
    var name: String by map
    var color: String by map
    var icon: String? by map
    var difficulty: String by map
    var tags: Map<String, MutableMap<String, Any?>> by map
    var startDate: Long by map
    var endDate: Long by map
    var motivations: List<String> by map
    var experience: Long? by map
    var coins: Long? by map
    var completedAtDate: Long? by map
    var completedAtMinute: Long? by map
    var note: String by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}