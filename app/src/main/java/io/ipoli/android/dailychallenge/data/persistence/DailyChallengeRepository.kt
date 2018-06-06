package io.ipoli.android.dailychallenge.data.persistence

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.BaseCollectionFirestoreRepository
import io.ipoli.android.common.persistence.CollectionRepository
import io.ipoli.android.common.persistence.FirestoreModel
import io.ipoli.android.dailychallenge.data.DailyChallenge
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.concurrent.ExecutorService
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/28/18.
 */
interface DailyChallengeRepository : CollectionRepository<DailyChallenge> {

    fun findDailyChallengeStreak(currentDate: LocalDate = LocalDate.now()): Int

}

data class DbDailyChallenge(override val map: MutableMap<String, Any?> = mutableMapOf()) :
    FirestoreModel {
    override var id: String by map
    var questIds: List<String> by map
    var isCompleted: Boolean by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}

class FirestoreDailyChallengeRepository(
    database: FirebaseFirestore,
    coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences,
    executor: ExecutorService
) : BaseCollectionFirestoreRepository<DailyChallenge, DbDailyChallenge>(
    database,
    coroutineContext,
    sharedPreferences,
    executor
), DailyChallengeRepository {

    override fun findDailyChallengeStreak(currentDate: LocalDate): Int {

        val lastIncompleteDCDocs = collectionReference
            .whereLessThan("id", currentDate.startOfDayUTC().toString())
            .whereEqualTo("isCompleted", false)
            .orderBy("id", Query.Direction.DESCENDING)
            .limit(1)
            .documents

        if (lastIncompleteDCDocs.isEmpty()) {
            return collectionReference
                .whereEqualTo("isCompleted", true)
                .whereLessThan("id", currentDate.startOfDayUTC().toString())
                .documents
                .size
        }

        val lastCompleteDateMillis = lastIncompleteDCDocs[0].getString("id")!!

        return collectionReference
            .whereLessThan("id", currentDate.startOfDayUTC().toString())
            .whereGreaterThan("id", lastCompleteDateMillis)
            .documents
            .size
    }

    override fun save(entity: DailyChallenge): DailyChallenge {

        val entityData = toDatabaseObject(entity).map.toMutableMap()

        if (entity.id.isEmpty()) {
            val doc = collectionReference.document(LocalDate.now().startOfDayUTC().toString())
            entityData["id"] = doc.id
            entityData["removedAt"] = null
            doc.set(entityData)
        } else {
            entityData["updatedAt"] = Instant.now().toEpochMilli()
            entityData["removedAt"] = null
            collectionReference
                .document(entity.id)
                .set(entityData)
        }

        return toEntityObject(entityData)
    }


    override val collectionReference
        get() = database.collection("players").document(playerId).collection("dailyChallenges")

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): DailyChallenge {
        val dc = DbDailyChallenge(dataMap.withDefault {
            null
        })
        return DailyChallenge(
            id = dc.id,
            questIds = dc.questIds,
            isCompleted = dc.isCompleted,
            createdAt = Instant.ofEpochMilli(dc.createdAt),
            updatedAt = Instant.ofEpochMilli(dc.updatedAt)
        )
    }

    override fun toDatabaseObject(entity: DailyChallenge) =
        DbDailyChallenge().also {
            it.id = entity.id
            it.questIds = entity.questIds
            it.isCompleted = entity.isCompleted
            it.createdAt = entity.createdAt.toEpochMilli()
            it.updatedAt = entity.updatedAt.toEpochMilli()
        }

}