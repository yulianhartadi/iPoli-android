package io.ipoli.android.repeatingquest.persistence

import android.content.SharedPreferences
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.TimePreference
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.BaseCollectionFirestoreRepository
import io.ipoli.android.common.persistence.CollectionRepository
import io.ipoli.android.common.persistence.FirestoreModel
import io.ipoli.android.quest.*
import io.ipoli.android.quest.data.persistence.DbEmbedTag
import io.ipoli.android.quest.data.persistence.DbReminder
import io.ipoli.android.quest.data.persistence.DbSubQuest
import io.ipoli.android.quest.subquest.SubQuest
import io.ipoli.android.repeatingquest.entity.RepeatPattern
import io.ipoli.android.repeatingquest.persistence.DbRepeatPattern.Type.*
import io.ipoli.android.tag.Tag
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
    var tags: Map<String, MutableMap<String, Any?>> by map
    var startMinute: Long? by map
    var duration: Long by map
    var priority: String by map
    var preferredStartTime: String by map
    var reminders: List<MutableMap<String, Any?>> by map
    var repeatPattern: MutableMap<String, Any?> by map
    var subQuests: List<MutableMap<String, Any?>> by map
    var challengeId: String? by map
    var note: String by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}

data class DbRepeatPattern(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var type: String by map
    var startDate: Long by map
    var endDate: Long? by map
    var dayOfMonth: Long by map
    var month: String by map
    var daysOfWeek: List<String> by map
    var daysOfMonth: List<Long> by map
    var timesPerWeek: Long by map
    var timesPerMonth: Long by map
    var preferredDays: List<String> by map
    var scheduledPeriods: MutableMap<String, List<Long>> by map

    enum class Type {
        DAILY, WEEKLY, MONTHLY, YEARLY, FLEXIBLE_WEEKLY, FLEXIBLE_MONTHLY
    }
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

    override fun findActiveNotForChallenge(
        challengeId: String,
        currentDate: LocalDate
    ) = findAllActive(currentDate).filter { it.challengeId != challengeId }

    override val collectionReference: CollectionReference
        get() {
            return database.collection("players").document(playerId).collection("repeatingQuests")
        }

    override fun findAllActive(currentDate: LocalDate): List<RepeatingQuest> {
        val rqsWithEndDate = collectionReference
            .whereGreaterThanOrEqualTo("repeatPattern.endDate", currentDate.startOfDayUTC())
            .entities

        val rqsWithoutEndDate = collectionReference
            .whereEqualTo("repeatPattern.endDate", null)
            .entities

        return rqsWithEndDate + rqsWithoutEndDate
    }

    override fun findAllForChallenge(challengeId: String) =
        collectionReference
            .whereEqualTo("challengeId", challengeId)
            .entities

    override fun findByTag(tagId: String) =
        collectionReference
            .whereEqualTo("tags.$tagId.id", tagId)
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
            tags = rq.tags.values.map {
                createTag(it)
            },
            startTime = rq.startMinute?.let { Time.of(it.toInt()) },
            duration = rq.duration.toInt(),
            priority = Priority.valueOf(rq.priority),
            preferredStartTime = TimePreference.valueOf(rq.preferredStartTime),
            reminders = rq.reminders.map {
                val cr = DbReminder(it)
                val type = DbReminder.Type.valueOf(cr.type)
                when (type) {
                    DbReminder.Type.RELATIVE ->
                        Reminder.Relative(cr.message, cr.minutesFromStart!!)

                    DbReminder.Type.FIXED ->
                        Reminder.Fixed(
                            cr.message,
                            cr.date!!.startOfDayUTC,
                            Time.of(cr.minute!!.toInt())
                        )
                }

            },
            repeatPattern = createRepeatPattern(DbRepeatPattern(rq.repeatPattern)),
            subQuests = rq.subQuests.map {
                val dsq = DbSubQuest(it)
                SubQuest(
                    name = dsq.name,
                    completedAtDate = dsq.completedAtDate?.startOfDayUTC,
                    completedAtTime = dsq.completedAtMinute?.let { Time.of(it.toInt()) }
                )
            },
            challengeId = rq.challengeId,
            note = rq.note,
            updatedAt = rq.updatedAt.instant,
            createdAt = rq.createdAt.instant
        )
    }

    private fun createRepeatPattern(rp: DbRepeatPattern): RepeatPattern {
        val type = valueOf(rp.type)

        return when (type) {
            DAILY -> {
                RepeatPattern.Daily(
                    startDate = rp.startDate.startOfDayUTC,
                    endDate = rp.endDate?.startOfDayUTC
                )
            }
            WEEKLY -> {
                RepeatPattern.Weekly(
                    daysOfWeek = rp.daysOfWeek.map { DayOfWeek.valueOf(it) }.toSet(),
                    startDate = rp.startDate.startOfDayUTC,
                    endDate = rp.endDate?.startOfDayUTC
                )
            }
            MONTHLY -> {
                RepeatPattern.Monthly(
                    daysOfMonth = rp.daysOfMonth.map { it.toInt() }.toSet(),
                    startDate = rp.startDate.startOfDayUTC,
                    endDate = rp.endDate?.startOfDayUTC
                )
            }

            YEARLY -> {
                RepeatPattern.Yearly(
                    dayOfMonth = rp.dayOfMonth.toInt(),
                    month = Month.valueOf(rp.month),
                    startDate = rp.startDate.startOfDayUTC,
                    endDate = rp.endDate?.startOfDayUTC
                )
            }

            FLEXIBLE_WEEKLY -> {
                RepeatPattern.Flexible.Weekly(
                    timesPerWeek = rp.timesPerWeek.toInt(),
                    preferredDays = rp.preferredDays.map { DayOfWeek.valueOf(it) }.toSet(),
                    scheduledPeriods = rp.scheduledPeriods.entries
                        .associate { it.key.toLong().startOfDayUTC to it.value.map { it.startOfDayUTC } },
                    startDate = rp.startDate.startOfDayUTC,
                    endDate = rp.endDate?.startOfDayUTC
                )
            }

            FLEXIBLE_MONTHLY -> {
                RepeatPattern.Flexible.Monthly(
                    timesPerMonth = rp.timesPerMonth.toInt(),
                    preferredDays = rp.preferredDays.map { it.toInt() }.toSet(),
                    scheduledPeriods = rp.scheduledPeriods.entries
                        .associate { it.key.toLong().startOfDayUTC to it.value.map { it.startOfDayUTC } },
                    startDate = rp.startDate.startOfDayUTC,
                    endDate = rp.endDate?.startOfDayUTC
                )
            }
        }
    }

    override fun toDatabaseObject(entity: RepeatingQuest): DbRepeatingQuest {
        val rq = DbRepeatingQuest()
        rq.id = entity.id
        rq.name = entity.name
        rq.tags = entity.tags.map { it.id to createDbTag(it).map }.toMap()
        rq.color = entity.color.name
        rq.icon = entity.icon?.name
        rq.duration = entity.duration.toLong()
        rq.priority = entity.priority.name
        rq.preferredStartTime = entity.preferredStartTime.name
        rq.startMinute = entity.startTime?.toMinuteOfDay()?.toLong()
        rq.reminders = entity.reminders.map {
            createDbReminder(it).map
        }
        rq.subQuests = entity.subQuests.map {
            DbSubQuest().apply {
                name = it.name
                completedAtDate = it.completedAtDate?.startOfDayUTC()
                completedAtMinute = it.completedAtTime?.toMinuteOfDay()?.toLong()
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
        rp.startDate = repeatPattern.startDate.startOfDayUTC()
        rp.endDate = repeatPattern.endDate?.startOfDayUTC()

        when (repeatPattern) {
            is RepeatPattern.Daily -> {
                rp.type = DAILY.name
            }
            is RepeatPattern.Weekly -> {
                rp.type = WEEKLY.name
                rp.daysOfWeek = repeatPattern.daysOfWeek.map {
                    it.name
                }
            }
            is RepeatPattern.Monthly -> {
                rp.type = MONTHLY.name
                rp.daysOfMonth = repeatPattern.daysOfMonth.map { it.toLong() }
            }
            is RepeatPattern.Yearly -> {
                rp.type = YEARLY.name
                rp.dayOfMonth = repeatPattern.dayOfMonth.toLong()
                rp.month = repeatPattern.month.name
            }
            is RepeatPattern.Flexible.Weekly -> {
                rp.type = FLEXIBLE_WEEKLY.name
                rp.timesPerWeek = repeatPattern.timesPerWeek.toLong()
                rp.preferredDays = repeatPattern.preferredDays.map {
                    it.name
                }
                rp.scheduledPeriods = repeatPattern.scheduledPeriods.entries
                    .associate { it.key.startOfDayUTC().toString() to it.value.map { it.startOfDayUTC() } }
                    .toMutableMap()
            }
            is RepeatPattern.Flexible.Monthly -> {
                rp.type = FLEXIBLE_MONTHLY.name
                rp.timesPerMonth = repeatPattern.timesPerMonth.toLong()
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
        when (reminder) {

            is Reminder.Fixed -> {
                cr.type = DbReminder.Type.FIXED.name
                cr.date = reminder.date.startOfDayUTC()
                cr.minute = reminder.time.toMinuteOfDay().toLong()
            }

            is Reminder.Relative -> {
                cr.type = DbReminder.Type.RELATIVE.name
                cr.minutesFromStart = reminder.minutesFromStart
            }
        }
        return cr
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