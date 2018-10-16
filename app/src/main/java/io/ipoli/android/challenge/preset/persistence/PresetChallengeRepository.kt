package io.ipoli.android.challenge.preset.persistence

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.preset.PresetChallenge
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.days
import io.ipoli.android.common.datetime.minutes
import io.ipoli.android.common.persistence.documents
import io.ipoli.android.common.persistence.getSync
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import org.threeten.bp.LocalDate
import java.util.*

interface PresetChallengeRepository {
    fun findForCategory(category: PresetChallenge.Category): List<PresetChallenge>
    fun findById(id: String): PresetChallenge
}

class FirestorePresetChallengeRepository(private val database: FirebaseFirestore) :
    PresetChallengeRepository {

    private val collectionReference: CollectionReference
        get() = database.collection("presetChallenges")

    override fun findForCategory(category: PresetChallenge.Category) =
        collectionReference
            .whereEqualTo("category", category.name)
            .documents
            .map {
                toEntityObject(it.data!!)
            }

    override fun findById(id: String) =
        toEntityObject(collectionReference.document(id).getSync().data!!)

    fun toEntityObject(dataMap: MutableMap<String, Any?>): PresetChallenge {

        val c = DbPresetChallenge(dataMap.withDefault {
            null
        })

        val dbSchedule = DbPresetChallenge.Schedule(c.schedule)

        val qs = dbSchedule.quests.map {
            val q = DbPresetChallenge.Schedule.Quest(it)
            PresetChallenge.Quest(
                name = q.name,
                color = Color.valueOf(q.color),
                icon = Icon.valueOf(q.icon),
                day = q.day.toInt(),
                duration = q.duration.minutes,
                subQuests = q.subQuests,
                note = q.note
            )
        }

        val hs = dbSchedule.habits.map {
            val h = DbPresetChallenge.Schedule.Habit(it)
            PresetChallenge.Habit(
                name = h.name,
                color = Color.valueOf(h.color),
                icon = Icon.valueOf(h.icon),
                isGood = h.isGood,
                timesADay = h.timesADay.toInt(),
                isSelected = true
            )
        }

        val schedule = PresetChallenge.Schedule(qs, hs)

        val dbConfig = DbPresetChallenge.Config(c.config)
        val nutritionMacros = dbConfig.nutritionMacros?.let {
            val dbMacros = DbPresetChallenge.NutritionMacros(it)
            val dbFemale = DbPresetChallenge.NutritionDetails(dbMacros.female)
            val dbMale = DbPresetChallenge.NutritionDetails(dbMacros.male)

            val female = PresetChallenge.NutritionDetails(
                caloriesPerKg = dbFemale.caloriesPerKg,
                proteinPerKg = dbFemale.proteinPerKg,
                carbohydratesPerKg = dbFemale.carbohydratesPerKg,
                fatPerKg = dbFemale.fatPerKg
            )

            val male = PresetChallenge.NutritionDetails(
                caloriesPerKg = dbMale.caloriesPerKg,
                proteinPerKg = dbMale.proteinPerKg,
                carbohydratesPerKg = dbMale.carbohydratesPerKg,
                fatPerKg = dbMale.fatPerKg
            )
            PresetChallenge.NutritionMacros(
                female = female,
                male = male
            )
        }

        val defaultStartTime = dbConfig.defaultStartMinute?.let {
            Time.of(it.toInt())
        }

        val config = PresetChallenge.Config(
            defaultStartTime = defaultStartTime,
            nutritionMacros = nutritionMacros
        )

        val trackedValues = c.trackedValues.map {
            val id = UUID.randomUUID().toString()

            DbPresetChallenge.TrackedValue(it).let { tv ->
                when (DbPresetChallenge.TrackedValue.Type.valueOf(tv.type)) {
                    DbPresetChallenge.TrackedValue.Type.PROGRESS ->
                        Challenge.TrackedValue.Progress(
                            id = id,
                            history = emptyMap<LocalDate, Challenge.TrackedValue.Log>().toSortedMap()
                        )

                    DbPresetChallenge.TrackedValue.Type.TARGET ->
                        Challenge.TrackedValue.Target(
                            id = id,
                            name = tv.name!!,
                            units = tv.units!!,
                            startValue = tv.startValue!!.toDouble(),
                            targetValue = tv.targetValue!!.toDouble(),
                            currentValue = 0.0,
                            remainingValue = 0.0,
                            isCumulative = tv.isCumulative!!,
                            history = emptyMap<LocalDate, Challenge.TrackedValue.Log>().toSortedMap()
                        )

                    DbPresetChallenge.TrackedValue.Type.AVERAGE ->
                        Challenge.TrackedValue.Average(
                            id = id,
                            name = tv.name!!,
                            units = tv.units!!,
                            targetValue = tv.targetValue!!.toDouble(),
                            lowerBound = tv.lowerBound!!.toDouble(),
                            upperBound = tv.upperBound!!.toDouble(),
                            history = emptyMap<LocalDate, Challenge.TrackedValue.Log>().toSortedMap()
                        )
                }
            }
        }

        return PresetChallenge(
            id = c.id,
            name = c.name,
            color = Color.valueOf(c.color),
            icon = Icon.valueOf(c.icon),
            category = PresetChallenge.Category.valueOf(c.category),
            imageUrl = c.imageUrl,
            shortDescription = c.shortDescription,
            description = c.description,
            difficulty = Challenge.Difficulty.valueOf(c.difficulty),
            duration = c.duration.days,
            busynessPerWeek = c.busynessPerWeek.minutes,
            requirements = c.requirements,
            level = c.level?.toInt(),
            gemPrice = c.gemPrice.toInt(),
            expectedResults = c.expectedResults,
            note = c.note,
            trackedValues = trackedValues,
            config = config,
            schedule = schedule
        )
    }

    data class DbPresetChallenge(val map: MutableMap<String, Any?> = mutableMapOf()) {
        var id: String by map
        var name: String by map
        var color: String by map
        var icon: String by map
        var category: String by map
        var imageUrl: String by map
        var shortDescription: String by map
        var description: String by map
        var difficulty: String by map
        var requirements: List<String> by map
        var expectedResults: List<String> by map
        var duration: Long by map
        var trackedValues: List<MutableMap<String, Any?>> by map
        var busynessPerWeek: Long by map
        var level: Long? by map
        var gemPrice: Long by map
        var note: String by map
        var config: MutableMap<String, Any?> by map
        var schedule: MutableMap<String, Any?> by map

        data class TrackedValue(val map: MutableMap<String, Any?> = mutableMapOf()) {
            var id: String by map
            var type: String by map
            var name: String? by map
            var units: String? by map
            var targetValue: Float? by map
            var startValue: Float? by map
            var isCumulative: Boolean? by map
            var lowerBound: Float? by map
            var upperBound: Float? by map
            var logs: List<Map<String, Any?>>? by map

            enum class Type {
                PROGRESS, TARGET, AVERAGE
            }
        }

        data class Config(val map: MutableMap<String, Any?> = mutableMapOf()) {
            var defaultStartMinute: Long? by map
            var nutritionMacros: MutableMap<String, Any?>? by map
        }

        data class NutritionMacros(val map: MutableMap<String, Any?> = mutableMapOf()) {
            var female: MutableMap<String, Any?> by map
            var male: MutableMap<String, Any?> by map
        }

        data class NutritionDetails(val map: MutableMap<String, Any?> = mutableMapOf()) {
            var caloriesPerKg: Float by map
            var proteinPerKg: Float by map
            var carbohydratesPerKg: Float by map
            var fatPerKg: Float by map
        }

        data class Schedule(val map: MutableMap<String, Any?> = mutableMapOf()) {
            var quests: List<MutableMap<String, Any?>> by map
            var habits: List<MutableMap<String, Any?>> by map

            data class Quest(val map: MutableMap<String, Any?> = mutableMapOf()) {
                var name: String by map
                var color: String by map
                var icon: String by map
                var day: Long by map
                var duration: Long by map
                var subQuests: List<String> by map
                var note: String by map
            }

            data class Habit(val map: MutableMap<String, Any?> = mutableMapOf()) {
                var name: String by map
                var color: String by map
                var icon: String by map
                var isGood: Boolean by map
                var timesADay: Long by map
            }
        }
    }
}