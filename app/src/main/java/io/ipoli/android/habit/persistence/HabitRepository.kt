package io.ipoli.android.habit.persistence

import android.content.SharedPreferences
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.BaseCollectionFirestoreRepository
import io.ipoli.android.common.persistence.CollectionRepository
import io.ipoli.android.common.persistence.FirestoreModel
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.data.persistence.DbEmbedTag
import io.ipoli.android.tag.Tag
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import java.util.concurrent.ExecutorService
import kotlin.coroutines.experimental.CoroutineContext

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

class FirestoreHabitRepository(
    database: FirebaseFirestore,
    coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences,
    executor: ExecutorService
) : BaseCollectionFirestoreRepository<Habit, DbHabit>(
    database,
    coroutineContext,
    sharedPreferences,
    executor
), HabitRepository {

    override val collectionReference: CollectionReference
        get() = database.collection("players").document(playerId).collection("habits")

    override fun findAllForChallenge(challengeId: String): List<Habit> =
        collectionReference
            .whereEqualTo("challengeId", challengeId)
            .notRemovedEntities

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
            createdAt = Instant.ofEpochMilli(h.createdAt),
            updatedAt = Instant.ofEpochMilli(h.updatedAt)
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