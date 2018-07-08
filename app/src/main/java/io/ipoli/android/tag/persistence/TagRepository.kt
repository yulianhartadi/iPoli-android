package io.ipoli.android.tag.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Millisecond
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.distinct
import io.ipoli.android.common.persistence.*
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import org.jetbrains.annotations.NotNull
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/03/2018.
 */
interface TagRepository : CollectionRepository<Tag>

class FirestoreTagRepository(
    database: FirebaseFirestore
) : BaseCollectionFirestoreRepository<Tag, DbTag>(
    database
) {

    override val collectionReference: CollectionReference
        get() = database.collection("players").document(playerId).collection("tags")

    override fun toEntityObject(dataMap: MutableMap<String, Any?>) =
        with(
            DbTag(dataMap.withDefault {
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
                isFavorite = isFavorite,
                updatedAt = updatedAt.instant,
                createdAt = createdAt.instant,
                removedAt = removedAt?.instant
            )
        }

    override fun toDatabaseObject(entity: Tag) =
        DbTag().apply {
            id = entity.id
            name = entity.name
            color = entity.color.name
            icon = entity.icon?.name
            isFavorite = entity.isFavorite
            updatedAt = entity.updatedAt.toEpochMilli()
            createdAt = entity.createdAt.toEpochMilli()
            removedAt = entity.removedAt?.toEpochMilli()
        }

}

class DbTag(override val map: MutableMap<String, Any?> = mutableMapOf()) : FirestoreModel {
    override var id: String by map
    var name: String by map
    var color: String by map
    var icon: String? by map
    var isFavorite: Boolean by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}

@Dao
abstract class TagDao : BaseDao<RoomTag>() {

    @Query("SELECT * FROM tags")
    abstract fun findAll(): List<RoomTag>

    @Query("SELECT * FROM tags WHERE id = :id")
    abstract fun findById(id: String): RoomTag

    @Query("SELECT * FROM tags WHERE removedAt IS NULL")
    abstract fun listenForNotRemoved(): LiveData<List<RoomTag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    abstract fun listenById(id: String): LiveData<RoomTag>

    @Query("UPDATE tags $REMOVE_QUERY")
    abstract fun remove(id: String, currentTimeMillis: Long = System.currentTimeMillis())

    @Query("DELETE FROM habit_tag_join WHERE tagId = :id")
    abstract fun deleteHabitJoins(id: String)

    @Query("DELETE FROM challenge_tag_join WHERE tagId = :id")
    abstract fun deleteChallengeJoins(id: String)

    @Query("DELETE FROM repeating_quest_tag_join WHERE tagId = :id")
    abstract fun deleteRepeatingQuestJoins(id: String)

    @Query("DELETE FROM quest_tag_join WHERE tagId = :id")
    abstract fun deleteQuestJoins(id: String)

    @Query(
        """
        SELECT tags.*
        FROM tags
        INNER JOIN habit_tag_join ON tags.id = habit_tag_join.tagId
        WHERE habit_tag_join.habitId = :habitId
        """
    )
    abstract fun findForHabit(habitId: String): List<RoomTag>

    @Query(
        """
        SELECT tags.*
        FROM tags
        INNER JOIN quest_tag_join ON tags.id = quest_tag_join.tagId
        WHERE quest_tag_join.questId = :questId
        """
    )
    abstract fun findForQuest(questId: String): List<RoomTag>

    @Query(
        """
        SELECT tags.*
        FROM tags
        INNER JOIN challenge_tag_join ON tags.id = challenge_tag_join.tagId
        WHERE challenge_tag_join.challengeId = :challengeId
        """
    )
    abstract fun findForChallenge(challengeId: String): List<RoomTag>

    @Query(
        """
        SELECT tags.*
        FROM tags
        INNER JOIN repeating_quest_tag_join ON tags.id = repeating_quest_tag_join.tagId
        WHERE repeating_quest_tag_join.repeatingQuestId = :repeatingQuestId
        """
    )
    abstract fun findForRepeatingQuest(repeatingQuestId: String): List<RoomTag>

    @Query("SELECT * FROM tags $FIND_SYNC_QUERY")
    abstract fun findAllForSync(lastSync: Long): List<RoomTag>
}

class RoomTagRepository(roomDb: MyPoliRoomDatabase) :
    BaseRoomRepository<Tag, RoomTag, TagDao>(roomDb.tagDao()), TagRepository {

    private val mapper = RoomTagMapper()

    override fun findAllForSync(lastSync: Duration<Millisecond>) =
        dao.findAllForSync(lastSync.millisValue).map { toEntityObject(it) }

    override fun save(entity: Tag): Tag {
        val rEntity = toDatabaseObject(entity)
        dao.save(rEntity)
        return entity.copy(id = rEntity.id)
    }

    override fun save(entities: List<Tag>): List<Tag> {
        val rEntities = entities.map { toDatabaseObject(it) }
        dao.saveAll(rEntities)
        return rEntities.mapIndexed { i, rdc ->
            entities[i].copy(id = rdc.id)
        }
    }

    override fun findById(id: String): Tag? =
        toEntityObject(dao.findById(id))

    override fun findAll() =
        dao.findAll().map { toEntityObject(it) }

    override fun listenById(id: String) =
        dao.listenById(id).distinct().notifySingle()

    override fun listenForAll() =
        dao.listenForNotRemoved().notify()

    override fun remove(entity: Tag) {
        remove(entity.id)
    }

    override fun remove(id: String) {
        removeWithJoins(id)
    }

    @Transaction
    private fun removeWithJoins(id: String) {
        dao.remove(id)
        dao.deleteHabitJoins(id)
        dao.deleteChallengeJoins(id)
        dao.deleteRepeatingQuestJoins(id)
        dao.deleteQuestJoins(id)
    }

    override fun undoRemove(id: String) {
        TODO("not implemented")
    }

    override fun toEntityObject(dbObject: RoomTag) =
        mapper.toEntityObject(dbObject)

    override fun toDatabaseObject(entity: Tag) =
        mapper.toDatabaseObject(entity)
}

class RoomTagMapper {
    fun toEntityObject(dbObject: RoomTag) =
        Tag(
            id = dbObject.id,
            name = dbObject.name,
            color = Color.valueOf(dbObject.color),
            icon = dbObject.icon?.let {
                Icon.valueOf(it)
            },
            isFavorite = dbObject.isFavorite,
            updatedAt = dbObject.updatedAt.instant,
            createdAt = dbObject.createdAt.instant,
            removedAt = dbObject.removedAt?.instant
        )

    fun toDatabaseObject(entity: Tag) =
        RoomTag(
            id = if (entity.id.isEmpty()) UUID.randomUUID().toString() else entity.id,
            name = entity.name,
            color = entity.color.name,
            icon = entity.icon?.name,
            isFavorite = entity.isFavorite,
            updatedAt = System.currentTimeMillis(),
            createdAt = entity.createdAt.toEpochMilli(),
            removedAt = entity.removedAt?.toEpochMilli()
        )
}

@Entity(
    tableName = "tags",
    indices = [
        Index("updatedAt"),
        Index("removedAt")
    ]
)
data class RoomTag(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String,
    val color: String,
    val icon: String?,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val removedAt: Long?
)