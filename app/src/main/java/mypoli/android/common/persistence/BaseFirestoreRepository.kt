package mypoli.android.common.persistence

import android.content.SharedPreferences
import com.crashlytics.android.Crashlytics
import com.google.firebase.firestore.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import mypoli.android.Constants
import mypoli.android.quest.Entity
import org.threeten.bp.Instant
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/05/2018.
 */

abstract class BaseFirestoreRepository<E, out T>(
    protected val database: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences
) : Repository<E> where E : Entity, T : FirestoreModel {

    protected val playerId: String
        get() =
            sharedPreferences.getString(Constants.KEY_PLAYER_ID, null)

    protected fun Query.execute(): QuerySnapshot = runBlocking(UI) {
        suspendCoroutine<QuerySnapshot> { continuation ->
            var registration: ListenerRegistration? = null

            registration = addSnapshotListener { querySnapshot, error ->

                registration?.remove()

                if (error != null) {
                    logError(error)
                    return@addSnapshotListener
                }

                continuation.resume(querySnapshot)
            }
        }
    }

    protected val Query.documents: List<DocumentSnapshot> get() = execute().documents

    protected fun DocumentReference.getSync(): DocumentSnapshot = runBlocking(UI) {
        suspendCoroutine<DocumentSnapshot> { continuation ->
            var registration: ListenerRegistration? = null
            registration = addSnapshotListener { querySnapshot, error ->

                registration?.remove()

                if (error != null) {
                    logError(error)
                    return@addSnapshotListener
                }
                continuation.resume(querySnapshot)
            }
        }
    }

    protected fun logError(error: FirebaseFirestoreException) {
        Crashlytics.logException(error)
        Timber.e(error)
    }

    abstract val collectionReference: CollectionReference

    override fun save(entity: E): E {

        val entityData = toDatabaseObject(entity).map.toMutableMap()

        if (entity.id.isEmpty()) {
            val doc = collectionReference.document()
            entityData["id"] = doc.id
            entityData["removedAt"] = null
            val now = Instant.now().toEpochMilli()
            entityData["updatedAt"] = now
            entityData["createdAt"] = now
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

    protected abstract fun toEntityObject(dataMap: MutableMap<String, Any?>): E

    protected abstract fun toDatabaseObject(entity: E): T

    protected fun extractDocument(ref: DocumentReference): E? {
        val result = ref.getSync()
        if (!result.exists()) {
            return null
        }

        return toEntityObject(result.data)
    }

    protected val Query.entities
        get() =
            toEntityObjects(whereEqualTo("removedAt", null).documents)

    protected fun toEntityObjects(snapshots: List<DocumentSnapshot>) =
        snapshots.map { toEntityObject(it.data) }
}

abstract class BaseEntityFirestoreRepository<E, out T>(
    database: FirebaseFirestore,
    private val coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences
) : BaseFirestoreRepository<E, T>(
    database,
    sharedPreferences
), EntityRepository<E> where E : Entity, T : FirestoreModel {

    abstract val entityReference: DocumentReference

    override fun listen(): ReceiveChannel<E?> {
        val c = Channel<E?>()
        var registration: ListenerRegistration? = null
        registration = entityReference
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    logError(error)
                    registration?.remove()
                    return@addSnapshotListener
                }

                if (c.isClosedForReceive) {
                    registration?.remove()
                    return@addSnapshotListener
                }

                val entity = toEntityObject(snapshot.data)
                launch(coroutineContext) {
                    c.send(entity)
                }
            }
        return c
    }

    override fun find(): E? =
        extractDocument(entityReference)
}

abstract class BaseCollectionFirestoreRepository<E, out T>(
    database: FirebaseFirestore,
    private val coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences
) : BaseFirestoreRepository<E, T>(
    database,
    sharedPreferences
), CollectionRepository<E> where E : Entity, T : FirestoreModel {

    override fun findById(id: String): E? =
        extractDocument(documentReference(id))

    override fun listenById(id: String): ReceiveChannel<E?> {

        val c = Channel<E?>()
        var registration: ListenerRegistration? = null
        registration = documentReference(id)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    logError(error)
                    registration?.remove()
                    return@addSnapshotListener
                }

                if (c.isClosedForReceive) {
                    registration?.remove()
                    return@addSnapshotListener
                }

                val entity = toEntityObject(snapshot.data)
                launch(coroutineContext) {
                    c.send(entity)
                }
            }
        return c
    }

    protected fun listenForChanges(query: Query): ReceiveChannel<List<E>> {
        val c = Channel<List<E>>()
        var registration: ListenerRegistration? = null
        registration = query
            .whereEqualTo("removedAt", null)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    logError(error)
                    registration?.remove()
                    return@addSnapshotListener
                }

                if (c.isClosedForReceive) {
                    registration?.remove()
                    return@addSnapshotListener
                }

                val entities = toEntityObjects(snapshot.documents)
                launch(coroutineContext) {
                    c.send(entities)
                }
            }
        return c
    }

    override fun listenForAll() = listenForChanges(collectionReference)

    override fun remove(entity: E) {
        remove(entity.id)
    }

    override fun remove(id: String) {
        val updates = mapOf(
            "removedAt" to Instant.now().toEpochMilli()
        )
        documentReference(id).update(updates)
    }

    override fun undoRemove(id: String) {
        val updates = mapOf(
            "removedAt" to null
        )
        documentReference(id).update(updates)
    }

    protected fun documentReference(id: String) = collectionReference.document(id)
}