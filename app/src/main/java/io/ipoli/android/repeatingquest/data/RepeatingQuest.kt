package io.ipoli.android.repeatingquest.data

import io.ipoli.android.Constants
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.TimePreference
import io.ipoli.android.common.datetime.toStartOfDayUTCMillis
import io.ipoli.android.common.persistence.PersistedModel
import io.ipoli.android.quest.data.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import net.fortuna.ical4j.model.Recur
import org.threeten.bp.LocalDate
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/22/17.
 */
open class RepeatingQuest : RealmObject, PersistedModel {

    @PrimaryKey
    override var id: String = ""

    var rawText: String? = null

    var name: String? = null

    var category: String? = null

    var isAllDay: Boolean = false

    var priority: Int? = null
        get() = if (field != null) field else Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT

    var startMinute: Int? = null

    var preferredStartTime: String? = null

    private var createdAt: Long? = null
    private var updatedAt: Long? = null

    private var duration: Int? = null
    var reminders: RealmList<Reminder>? = null
        get() {
            if (field == null) {
                this.reminders = RealmList()
            }
            return field
        }
    var subQuests: RealmList<SubQuest>? = null

    var completedAt: Long? = null

    var recurrence: Recurrence? = null

    var notes: RealmList<Note>? = null
        get() {
            if (field == null) {
                this.notes = RealmList()
            }
            return field
        }

    var challengeId: String? = null

    var source: String? = null

    var sourceMapping: SourceMapping? = null

    constructor()

    constructor(rawText: String) {
        this.rawText = rawText
        createdAt = DateUtils.nowUTC().time
        updatedAt = DateUtils.nowUTC().time
        this.category = Category.PERSONAL.name
        this.source = Constants.API_RESOURCE_SOURCE
    }

    var startTime: Time?
        get() = if (startMinute == null) {
            null
        } else Time.of(startMinute!!)
        set(time) = if (time != null) {
            startMinute = time.toMinuteOfDay()
        } else {
            startMinute = null
        }

    var completedAtDate: Date?
        get() = if (completedAt != null) Date(completedAt!!) else null
        set(completedAtDate) {
            completedAt = completedAtDate?.time
        }

    val isCompleted: Boolean
        get() = completedAtDate != null

    fun shouldBeScheduledAfter(date: LocalDate): Boolean {
        return recurrence!!.dtendDate == null || recurrence!!.dtend!! >= date.toStartOfDayUTCMillis()
    }

    val frequency: Int
        get() {
            val recurrence = recurrence
            if (recurrence!!.isFlexible) {
                return recurrence.flexibleCount
            }
            if (recurrence.recurrenceType === Recurrence.RepeatType.DAILY) {
                return 7
            }
            if (recurrence.recurrenceType === Recurrence.RepeatType.MONTHLY) {
                return 1
            }
            try {
                val recur = Recur(recurrence.rrule)
                return recur.dayList.size
            } catch (e: ParseException) {
                return 0
            }

        }

    fun setDuration(duration: Int?) {
        this.duration = Math.min(TimeUnit.HOURS.toMinutes(Constants.MAX_QUEST_DURATION_HOURS.toLong()), duration!!.toLong()).toInt()
    }

    fun getDuration(): Int {
        return if (duration != null) duration!! else 0
    }

    val isFlexible: Boolean
        get() = recurrence!!.isFlexible


    val textNotes: List<Note>
        get() {
            return notes!!.filter { it.noteType == Note.NoteType.TEXT.name }
        }

    fun addNote(note: Note) {
        notes!!.add(note)
    }

    fun removeTextNote() {
        val txtNotes = textNotes
        notes!!.removeAll(txtNotes)
    }

    var categoryType: Category
        get() = Category.valueOf(this.category!!)
        set(category) {
            this.category = category.name
        }

    fun addReminder(reminder: Reminder) {
        reminders?.add(reminder)
    }

    var startTimePreference: TimePreference?
        get() = if (preferredStartTime.isNullOrEmpty()) {
            TimePreference.ANY
        } else TimePreference.valueOf(preferredStartTime!!)
        set(timePreference) {
            if (timePreference != null) {
                this.preferredStartTime = timePreference.name
            }
        }
}

