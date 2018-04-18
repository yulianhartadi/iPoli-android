package io.ipoli.android.repeatingquest.persistence

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.BaseCollectionFirestoreRepository
import io.ipoli.android.common.persistence.CollectionRepository
import io.ipoli.android.common.persistence.FirestoreModel
import io.ipoli.android.common.persistence.TagProvider
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.quest.data.persistence.DbReminder
import io.ipoli.android.quest.data.persistence.DbSubQuest
import io.ipoli.android.quest.subquest.SubQuest
import io.ipoli.android.repeatingquest.entity.RepeatPattern
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/16/18.
 */
interface RepeatingQuestRepository : CollectionRepository<RepeatingQuest> {
    fun findAllActive(currentDate: LocalDate = LocalDate.now()): List<RepeatingQuest>
    fun findActiveNotForChallenge(
        challengeId: String,
        currentDate: LocalDate = LocalDate.now()
    ): List<RepeatingQuest>

    fun findAllForChallenge(challengeId: String): List<RepeatingQuest>
    fun findByTag(tagId: String): List<RepeatingQuest>
    fun generateId(): String
    fun purge(id: String)
    fun purge(ids: List<String>)
}

data class DbRepeatingQuest(override val map: MutableMap<String, Any?> = mutableMapOf()) :
    FirestoreModel {
    override var id: String by map
    var name: String by map
    var color: String by map
    var icon: String? by map
    var tagIds: Map<String, Boolean> by map
    var startMinute: Long? by map
    var duration: Int by map
    var reminders: List<MutableMap<String, Any?>> by map
    var repeatPattern: MutableMap<String, Any?> by map
    var subQuests: List<MutableMap<String, Any?>> by map
    var challengeId: String? by map
    var note: String? by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}

data class DbRepeatPattern(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var type: String by map
    var start: Long by map
    var end: Long? by map
    var dayOfMonth: Int by map
    var month: String by map
    var daysOfWeek: List<String> by map
    var daysOfMonth: List<Int> by map
    var timesPerWeek: Int by map
    var timesPerMonth: Int by map
    var preferredDays: List<String> by map
    var scheduledPeriods: MutableMap<String, List<Long>> by map
}

enum class DbRepeatPatternType {
    DAILY, WEEKLY, MONTHLY, YEARLY, FLEXIBLE_WEEKLY, FLEXIBLE_MONTHLY
}

class FirestoreRepeatingQuestRepository(
    database: FirebaseFirestore,
    coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences,
    tagProvider: TagProvider
) : BaseCollectionFirestoreRepository<RepeatingQuest, DbRepeatingQuest>(
    database,
    coroutineContext,
    sharedPreferences
), RepeatingQuestRepository {

    private val tags by tagProvider

    override fun findActiveNotForChallenge(
        challengeId: String,
        currentDate: LocalDate
    ) = findAllActive(currentDate).filter { it.challengeId != challengeId }

    override val collectionReference
        get() = database.collection("players").document(playerId).collection("repeatingQuests")

    override fun findAllActive(currentDate: LocalDate): List<RepeatingQuest> {
        val rqsWithEndDate = collectionReference
            .whereGreaterThanOrEqualTo("repeatPattern.end", currentDate.startOfDayUTC())
            .entities

        val rqsWithoutEndDate = collectionReference
            .whereEqualTo("repeatPattern.end", null)
            .entities

        return rqsWithEndDate + rqsWithoutEndDate
    }

    override fun findAllForChallenge(challengeId: String) =
        collectionReference
            .whereEqualTo("challengeId", challengeId)
            .entities

    override fun findByTag(tagId: String) =
        collectionReference
            .whereEqualTo("tagIds.$tagId", true)
            .entities

    override fun generateId() = collectionReference.document().id

    override fun purge(id: String) {
        collectionReference.document(id).delete()
    }

    override fun purge(ids: List<String>) {
        val batch = database.batch()
        ids.forEach {
            val ref = collectionReference.document(it)
            batch.delete(ref)
        }

        batch.commit()
    }

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): RepeatingQuest {
        val rq = DbRepeatingQuest(dataMap.withDefault {
            null
        })


        return RepeatingQuest(
            id = rq.id,
            name = rq.name,
            color = Color.valueOf(rq.color),
            icon = rq.icon?.let {
                Icon.valueOf(it)
            },
            tags = rq.tagIds.keys.map {
                tags[it]!!
            },
            startTime = rq.startMinute?.let { Time.of(it.toInt()) },
            duration = rq.duration,
            reminders = rq.reminders.map {
                val cr = DbReminder(it)
                Reminder(cr.message, Time.of(cr.minute), cr.date?.startOfDayUTC)
            },
            repeatPattern = createRepeatPattern(DbRepeatPattern(rq.repeatPattern)),
            subQuests = rq.subQuests.map {
                val dsq = DbSubQuest(it)
                SubQuest(
                    name = dsq.name,
                    completedAtDate = dsq.completedAtDate?.startOfDayUTC,
                    completedAtTime = dsq.completedAtTime?.let { Time.of(it.toInt()) }
                )
            },
            challengeId = rq.challengeId,
            note = rq.note,
            updatedAt = rq.updatedAt.instant,
            createdAt = rq.createdAt.instant
        )
    }

    private fun createRepeatPattern(rp: DbRepeatPattern): RepeatPattern {
        val type = DbRepeatPatternType.valueOf(rp.type)

        return when (type) {
            DbRepeatPatternType.DAILY -> {
                RepeatPattern.Daily(
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }
            DbRepeatPatternType.WEEKLY -> {
                RepeatPattern.Weekly(
                    daysOfWeek = rp.daysOfWeek.map { DayOfWeek.valueOf(it) }.toSet(),
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }
            DbRepeatPatternType.MONTHLY -> {
                RepeatPattern.Monthly(
                    daysOfMonth = rp.daysOfMonth.toSet(),
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }

            DbRepeatPatternType.YEARLY -> {
                RepeatPattern.Yearly(
                    dayOfMonth = rp.dayOfMonth,
                    month = Month.valueOf(rp.month),
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }

            DbRepeatPatternType.FLEXIBLE_WEEKLY -> {
                RepeatPattern.Flexible.Weekly(
                    timesPerWeek = rp.timesPerWeek,
                    preferredDays = rp.preferredDays.map { DayOfWeek.valueOf(it) }.toSet(),
                    scheduledPeriods = rp.scheduledPeriods.entries
                        .associate { it.key.toLong().startOfDayUTC to it.value.map { it.startOfDayUTC } },
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }

            DbRepeatPatternType.FLEXIBLE_MONTHLY -> {
                RepeatPattern.Flexible.Monthly(
                    timesPerMonth = rp.timesPerMonth,
                    preferredDays = rp.preferredDays.map { it.toInt() }.toSet(),
                    scheduledPeriods = rp.scheduledPeriods.entries
                        .associate { it.key.toLong().startOfDayUTC to it.value.map { it.startOfDayUTC } },
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }
        }
    }

    override fun toDatabaseObject(entity: RepeatingQuest): DbRepeatingQuest {
        val rq = DbRepeatingQuest()
        rq.id = entity.id
        rq.name = entity.name
        rq.tagIds = entity.tags.map { it.id to true }.toMap()
        rq.color = entity.color.name
        rq.icon = entity.icon?.name
        rq.duration = entity.duration
        rq.startMinute = entity.startTime?.toMinuteOfDay()?.toLong()
        rq.reminders = entity.reminders.map {
            createDbReminder(it).map
        }
        rq.subQuests = entity.subQuests.map {
            DbSubQuest().apply {
                name = it.name
                completedAtDate = it.completedAtDate?.startOfDayUTC()
                completedAtTime = it.completedAtTime?.toMinuteOfDay()?.toLong()
            }.map
        }
        rq.repeatPattern = createDbRepeatingPattern(entity.repeatPattern).map
        rq.challengeId = entity.challengeId
        rq.note = entity.note
        rq.updatedAt = entity.updatedAt.toEpochMilli()
        rq.createdAt = entity.createdAt.toEpochMilli()
        return rq
    }

    private fun createDbRepeatingPattern(repeatPattern: RepeatPattern): DbRepeatPattern {
        val rp = DbRepeatPattern()
        rp.start = repeatPattern.start.startOfDayUTC()
        rp.end = repeatPattern.end?.startOfDayUTC()

        when (repeatPattern) {
            is RepeatPattern.Daily -> {
                rp.type = DbRepeatPatternType.DAILY.name
            }
            is RepeatPattern.Weekly -> {
                rp.type = DbRepeatPatternType.WEEKLY.name
                rp.daysOfWeek = repeatPattern.daysOfWeek.map {
                    it.name
                }
            }
            is RepeatPattern.Monthly -> {
                rp.type = DbRepeatPatternType.MONTHLY.name
                rp.daysOfMonth = repeatPattern.daysOfMonth.map { it }
            }
            is RepeatPattern.Yearly -> {
                rp.type = DbRepeatPatternType.YEARLY.name
                rp.dayOfMonth = repeatPattern.dayOfMonth
                rp.month = repeatPattern.month.name
            }
            is RepeatPattern.Flexible.Weekly -> {
                rp.type = DbRepeatPatternType.FLEXIBLE_WEEKLY.name
                rp.timesPerWeek = repeatPattern.timesPerWeek
                rp.preferredDays = repeatPattern.preferredDays.map {
                    it.name
                }
                rp.scheduledPeriods = repeatPattern.scheduledPeriods.entries
                    .associate { it.key.startOfDayUTC().toString() to it.value.map { it.startOfDayUTC() } }
                    .toMutableMap()
            }
            is RepeatPattern.Flexible.Monthly -> {
                rp.type = DbRepeatPatternType.FLEXIBLE_MONTHLY.name
                rp.timesPerMonth = repeatPattern.timesPerMonth
                rp.preferredDays = repeatPattern.preferredDays.map { it.toString() }

                rp.scheduledPeriods = repeatPattern.scheduledPeriods.entries
                    .associate { it.key.startOfDayUTC().toString() to it.value.map { it.startOfDayUTC() } }
                    .toMutableMap()
            }
        }
        return rp
    }

    private fun createDbReminder(reminder: Reminder): DbReminder {
        val cr = DbReminder()
        cr.message = reminder.message
        cr.date = reminder.remindDate?.startOfDayUTC()
        cr.minute = reminder.remindTime.toMinuteOfDay()
        return cr
    }
}