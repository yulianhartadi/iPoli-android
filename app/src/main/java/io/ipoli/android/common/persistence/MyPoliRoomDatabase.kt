package io.ipoli.android.common.persistence

import android.arch.persistence.room.*
import android.content.Context
import io.ipoli.android.challenge.persistence.ChallengeDao
import io.ipoli.android.challenge.persistence.RoomChallenge
import io.ipoli.android.dailychallenge.data.persistence.DailyChallengeDao
import io.ipoli.android.dailychallenge.data.persistence.RoomDailyChallenge
import io.ipoli.android.habit.persistence.HabitDao
import io.ipoli.android.habit.persistence.RoomHabit
import io.ipoli.android.player.persistence.PlayerDao
import io.ipoli.android.player.persistence.RoomPlayer
import io.ipoli.android.quest.data.persistence.QuestDao
import io.ipoli.android.quest.data.persistence.RoomQuest
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestDao
import io.ipoli.android.repeatingquest.persistence.RoomRepeatingQuest
import io.ipoli.android.tag.persistence.RoomTag
import io.ipoli.android.tag.persistence.TagDao
import org.json.JSONArray
import org.json.JSONObject


class Converters {

    @TypeConverter
    fun fromListOfStrings(data: List<String>?): String? {
        return data?.let {
            JSONArray(it).toString()
        }
    }

    @TypeConverter
    fun toListOfStrings(data: String?): List<String>? {
        return data?.let {
            @Suppress("unchecked_cast")
            toList(JSONArray(it)) as List<String>
        }
    }

    @TypeConverter
    fun fromListOfLongs(data: List<Long>?): String? {
        return data?.let {
            JSONArray(it).toString()
        }
    }

    @TypeConverter
    fun toListOfLongs(data: String?): List<Long>? {
        return data?.let {
            @Suppress("unchecked_cast")
            toList(JSONArray(it)) as List<Long>
        }
    }

    @TypeConverter
    fun fromMapOfStringToObject(data: Map<String, MutableMap<String, Any?>>?): String? {
        return data?.let {
            JSONObject(it).toString()
        }
    }

    @TypeConverter
    fun toMapOfStringToObject(data: String?): Map<String, MutableMap<String, Any?>>? {
        return data?.let {
            @Suppress("unchecked_cast")
            toMap(JSONObject(it)) as Map<String, MutableMap<String, Any?>>?
        }
    }

    @TypeConverter
    fun fromObjectMap(data: Map<String, Any?>?): String? {
        return data?.let {
            JSONObject(it).toString()
        }
    }

    @TypeConverter
    fun toObjectMap(data: String?): Map<String, Any?>? {
        return data?.let {
            toMap(JSONObject(it))
        }
    }

    @TypeConverter
    fun fromListOfObjectMap(data: List<Map<String, Any?>>?): String? {
        return data?.let {
            JSONArray(it).toString()
        }
    }

    @TypeConverter
    fun toListOfObjectMap(data: String?): List<Map<String, Any?>>? {
        return data?.let {
            @Suppress("unchecked_cast")
            toList(JSONArray(it)) as List<Map<String, Any?>>?
        }
    }

    @TypeConverter
    fun fromMapStringToList(data: Map<String, List<Long>>?): String? {
        return data?.let {
            JSONObject(it).toString()
        }
    }

    @TypeConverter
    fun toMapStringToList(data: String?) =
        data?.let {
            @Suppress("unchecked_cast")
            toMap(JSONObject(it)) as Map<String, List<Long>>
        }

    fun toMap(jsonObject: JSONObject): Map<String, Any?> {
        val map = HashMap<String, Any?>()

        if (jsonObject === JSONObject.NULL) {
            return map
        }

        val keysItr = jsonObject.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            var value = jsonObject.get(key)

            when {
                value is Int -> value = value.toLong()
                value === JSONObject.NULL -> value = null
                value is JSONArray -> value = toList(value)
                value is JSONObject -> value = toMap(value)
            }
            map[key] = value
        }
        return map
    }

    fun toList(array: JSONArray): List<Any> {
        val list = ArrayList<Any>()
        for (i in 0 until array.length()) {
            var value = array.get(i)
            when {
                value is Int -> value = value.toLong()
                value === JSONObject.NULL -> value = null
                value is JSONArray -> value = toList(value)
                value is JSONObject -> value = toMap(value)
            }
            list.add(value)
        }
        return list
    }
}

@Database(
    entities = [
        RoomPlayer::class,
        RoomQuest::class,
        RoomQuest.Companion.RoomTagJoin::class,
        RoomRepeatingQuest::class,
        RoomRepeatingQuest.Companion.RoomTagJoin::class,
        RoomChallenge::class,
        RoomChallenge.Companion.RoomTagJoin::class,
        RoomTag::class,
        RoomHabit::class,
        RoomDailyChallenge::class,
        RoomHabit.Companion.RoomTagJoin::class,
        RoomEntityReminder::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyPoliRoomDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao

    abstract fun questDao(): QuestDao

    abstract fun repeatingQuestDao(): RepeatingQuestDao

    abstract fun challengeDao(): ChallengeDao

    abstract fun tagDao(): TagDao

    abstract fun habitDao(): HabitDao

    abstract fun dailyChallengeDao(): DailyChallengeDao

    abstract fun entityReminderDao(): EntityReminderDao

    companion object {

        @Volatile
        private var INSTANCE: MyPoliRoomDatabase? = null

        fun getInstance(context: Context): MyPoliRoomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                MyPoliRoomDatabase::class.java, "myPoli.db"
            )
                .build()
    }
}