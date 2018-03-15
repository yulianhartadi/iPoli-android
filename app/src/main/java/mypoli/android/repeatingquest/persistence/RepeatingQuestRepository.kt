package mypoli.android.repeatingquest.persistence

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import mypoli.android.common.datetime.Time
import mypoli.android.common.datetime.instant
import mypoli.android.common.datetime.startOfDayUTC
import mypoli.android.common.persistence.BaseCollectionFirestoreRepository
import mypoli.android.common.persistence.CollectionRepository
import mypoli.android.common.persistence.FirestoreModel
import mypoli.android.quest.*
import mypoli.android.quest.data.persistence.DbReminder
import mypoli.android.repeatingquest.entity.RepeatingPattern
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
    fun generateId(): String
    fun purge(id: String)
}

data class DbRepeatingQuest(override val map: MutableMap<String, Any?> = mutableMapOf()) :
    FirestoreModel {
    override var id: String by map
    var name: String by map
    var color: String by map
    var icon: String? by map
    var category: String by map
    var startMinute: Long? by map
    var duration: Int by map
    var reminder: MutableMap<String, Any?>? by map
    var repeatingPattern: MutableMap<String, Any?> by map
    var challengeId: String? by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}

data class DbRepeatingPattern(val map: MutableMap<String, Any?> = mutableMapOf()) {
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

enum class DbRepeatingPatternType {
    DAILY, WEEKLY, MONTHLY, YEARLY, FLEXIBLE_WEEKLY, FLEXIBLE_MONTHLY
}

class FirestoreRepeatingQuestRepository(
    database: FirebaseFirestore,
    coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences
) : BaseCollectionFirestoreRepository<RepeatingQuest, DbRepeatingQuest>(
    database,
    coroutineContext,
    sharedPreferences
), RepeatingQuestRepository {
    override fun purge(id: String) {
        collectionReference.document(id).delete()
    }

    override fun findActiveNotForChallenge(
        challengeId: String,
        currentDate: LocalDate
    ) = findAllActive(currentDate).filter { it.challengeId != challengeId }

    override val collectionReference
        get() = database.collection("players").document(playerId).collection("repeatingQuests")

    override fun findAllActive(currentDate: LocalDate): List<RepeatingQuest> {
        val rqsWithEndDate = collectionReference
            .whereGreaterThanOrEqualTo("repeatingPattern.end", currentDate.startOfDayUTC())
            .entities

        val rqsWithoutEndDate = collectionReference
            .whereEqualTo("repeatingPattern.end", null)
            .entities

        return rqsWithEndDate + rqsWithoutEndDate
    }

    override fun findAllForChallenge(challengeId: String) =
        collectionReference
            .whereEqualTo("challengeId", challengeId)
            .entities

    override fun generateId() = collectionReference.document().id

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
            category = Category(rq.category, Color.GREEN),
            startTime = rq.startMinute?.let { Time.of(it.toInt()) },
            duration = rq.duration,
            reminder = rq.reminder?.let {
                val cr = DbReminder(it)
                Reminder(cr.message, Time.of(cr.minute), cr.date?.startOfDayUTC)
            },
            repeatingPattern = createRepeatingPattern(DbRepeatingPattern(rq.repeatingPattern)),
            challengeId = rq.challengeId,
            updatedAt = rq.updatedAt.instant,
            createdAt = rq.createdAt.instant
        )
    }

    private fun createRepeatingPattern(rp: DbRepeatingPattern): RepeatingPattern {
        val type = DbRepeatingPatternType.valueOf(rp.type)

        return when (type) {
            DbRepeatingPatternType.DAILY -> {
                RepeatingPattern.Daily(
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }
            DbRepeatingPatternType.WEEKLY -> {
                RepeatingPattern.Weekly(
                    daysOfWeek = rp.daysOfWeek.map { DayOfWeek.valueOf(it) }.toSet(),
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }
            DbRepeatingPatternType.MONTHLY -> {
                RepeatingPattern.Monthly(
                    daysOfMonth = rp.daysOfMonth.toSet(),
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }

            DbRepeatingPatternType.YEARLY -> {
                RepeatingPattern.Yearly(
                    dayOfMonth = rp.dayOfMonth,
                    month = Month.valueOf(rp.month),
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }

            DbRepeatingPatternType.FLEXIBLE_WEEKLY -> {
                RepeatingPattern.Flexible.Weekly(
                    timesPerWeek = rp.timesPerWeek,
                    preferredDays = rp.preferredDays.map { DayOfWeek.valueOf(it) }.toSet(),
                    scheduledPeriods = rp.scheduledPeriods.entries
                        .associate { it.key.toLong().startOfDayUTC to it.value.map { it.startOfDayUTC } },
                    start = rp.start.startOfDayUTC,
                    end = rp.end?.startOfDayUTC
                )
            }

            DbRepeatingPatternType.FLEXIBLE_MONTHLY -> {
                RepeatingPattern.Flexible.Monthly(
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
        rq.category = entity.category.name
        rq.color = entity.color.name
        rq.icon = entity.icon?.name
        rq.duration = entity.duration
        rq.startMinute = entity.startTime?.toMinuteOfDay()?.toLong()
        rq.reminder = entity.reminder?.let {
            createDbReminder(it).map
        }
        rq.repeatingPattern = createDbRepeatingPattern(entity.repeatingPattern).map
        rq.challengeId = entity.challengeId
        rq.updatedAt = entity.updatedAt.toEpochMilli()
        rq.createdAt = entity.createdAt.toEpochMilli()
        return rq
    }

    private fun createDbRepeatingPattern(repeatingPattern: RepeatingPattern): DbRepeatingPattern {
        val rp = DbRepeatingPattern()
        rp.start = repeatingPattern.start.startOfDayUTC()
        rp.end = repeatingPattern.end?.startOfDayUTC()

        when (repeatingPattern) {
            is RepeatingPattern.Daily -> {
                rp.type = DbRepeatingPatternType.DAILY.name
            }
            is RepeatingPattern.Weekly -> {
                rp.type = DbRepeatingPatternType.WEEKLY.name
                rp.daysOfWeek = repeatingPattern.daysOfWeek.map {
                    it.name
                }
            }
            is RepeatingPattern.Monthly -> {
                rp.type = DbRepeatingPatternType.MONTHLY.name
                rp.daysOfMonth = repeatingPattern.daysOfMonth.map { it }
            }
            is RepeatingPattern.Yearly -> {
                rp.type = DbRepeatingPatternType.YEARLY.name
                rp.dayOfMonth = repeatingPattern.dayOfMonth
                rp.month = repeatingPattern.month.name
            }
            is RepeatingPattern.Flexible.Weekly -> {
                rp.type = DbRepeatingPatternType.FLEXIBLE_WEEKLY.name
                rp.timesPerWeek = repeatingPattern.timesPerWeek
                rp.preferredDays = repeatingPattern.preferredDays.map {
                    it.name
                }
                rp.scheduledPeriods = repeatingPattern.scheduledPeriods.entries
                    .associate { it.key.startOfDayUTC().toString() to it.value.map { it.startOfDayUTC() } }
                    .toMutableMap()
            }
            is RepeatingPattern.Flexible.Monthly -> {
                rp.type = DbRepeatingPatternType.FLEXIBLE_MONTHLY.name
                rp.timesPerMonth = repeatingPattern.timesPerMonth
                rp.preferredDays = repeatingPattern.preferredDays.map { it.toString() }
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