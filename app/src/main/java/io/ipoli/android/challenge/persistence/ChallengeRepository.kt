package io.ipoli.android.challenge.persistence

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.BaseCollectionFirestoreRepository
import io.ipoli.android.common.persistence.CollectionRepository
import io.ipoli.android.common.persistence.FirestoreModel
import io.ipoli.android.common.persistence.TagProvider
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import kotlinx.coroutines.experimental.channels.Channel
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/07/2018.
 */
interface ChallengeRepository : CollectionRepository<Challenge> {
    fun findByTag(tagId: String): List<Challenge>
}

class FirestoreChallengeRepository(
    database: FirebaseFirestore,
    coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences,
    tagProvider: TagProvider
) : BaseCollectionFirestoreRepository<Challenge, DbChallenge>(
    database,
    coroutineContext,
    sharedPreferences
), ChallengeRepository {

    private val tags by tagProvider

    override val collectionReference
        get() = database.collection("players").document(playerId).collection("challenges")

    override suspend fun listenForAll(channel: Channel<List<Challenge>>) =
        collectionReference
            .orderBy("end", Query.Direction.ASCENDING)
            .listenForChanges(channel)

    override fun findByTag(tagId: String) =
        collectionReference
            .whereEqualTo("tagIds.$tagId", true)
            .entities

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
            tags = c.tagIds.keys.map {
                tags[it]!!
            },
            start = c.start.startOfDayUTC,
            end = c.end.startOfDayUTC,
            motivations = c.motivations,
            experience = c.experience?.toInt(),
            coins = c.coins?.toInt(),
            completedAtDate = c.completedAtDate?.startOfDayUTC,
            completedAtTime = c.completedAtMinute?.let {
                Time.of(it.toInt())
            },
            note = c.note,
            createdAt = c.createdAt.instant,
            updatedAt = c.updatedAt.instant
        )
    }

    override fun toDatabaseObject(entity: Challenge): DbChallenge {
        val c = DbChallenge()
        c.id = entity.id
        c.name = entity.name
        c.color = entity.color.name
        c.icon = entity.icon?.name
        c.difficulty = entity.difficulty.name
        c.tagIds = entity.tags.map { it.id to true }.toMap()
        c.start = entity.start.startOfDayUTC()
        c.end = entity.end.startOfDayUTC()
        c.motivations = entity.motivations
        c.experience = entity.experience?.toLong()
        c.coins = entity.coins?.toLong()
        c.completedAtDate = entity.completedAtDate?.startOfDayUTC()
        c.completedAtMinute = entity.completedAtTime?.toMinuteOfDay()?.toLong()
        c.note = entity.note
        c.updatedAt = entity.updatedAt.toEpochMilli()
        c.createdAt = entity.createdAt.toEpochMilli()
        return c
    }
}


data class DbChallenge(override val map: MutableMap<String, Any?> = mutableMapOf()) :
    FirestoreModel {
    override var id: String by map
    var name: String by map
    var color: String by map
    var icon: String? by map
    var difficulty: String by map
    var tagIds: Map<String, Boolean> by map
    var start: Long by map
    var end: Long by map
    var motivations: List<String> by map
    var experience: Long? by map
    var coins: Long? by map
    var completedAtDate: Long? by map
    var completedAtMinute: Long? by map
    var note: String? by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}