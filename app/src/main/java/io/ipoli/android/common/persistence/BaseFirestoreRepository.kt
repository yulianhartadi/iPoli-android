package io.ipoli.android.common.persistence

import android.content.SharedPreferences
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import io.ipoli.android.quest.Entity
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import org.threeten.bp.Instant
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/05/2018.
 */

abstract class BaseFirestoreRepository<E, out T>(
    protected val database: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences
) : Repository<E> where E : Entity, T : FirestoreModel {

    abstract val collectionReference: CollectionReference

    private val channelToRegistration = mutableMapOf<SendChannel<*>, ListenerRegistration>()

    protected val playerId: String
        get() =
            FirebaseAuth.getInstance().currentUser!!.uid

    protected fun Query.execute(): QuerySnapshot {
        return try {
            Tasks.await(get(Source.CACHE))
        } catch (e: Throwable) {
            Tasks.await(get())
        }
    }

    protected fun Query.serverExecute(): QuerySnapshot {
        return try {
            Tasks.await(get(Source.SERVER))
        } catch (e: Throwable) {
            Tasks.await(get(Source.CACHE))
        }
    }


    protected val Query.serverDocuments: List<DocumentSnapshot> get() = serverExecute().documents

    protected val Query.documents: List<DocumentSnapshot> get() = execute().documents

    protected fun DocumentReference.getSync(): DocumentSnapshot {
        return try {
            Tasks.await(get(Source.CACHE))
        } catch (e: Throwable) {
            Tasks.await(get())
        }
    }

    protected fun logError(error: FirebaseFirestoreException) {
        Timber.e(error)
        Crashlytics.logException(error)
    }

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

    override fun save(entities: List<E>): List<E> {

        val batch = database.batch()

        val newEntities = entities.map {
            val entityData = toDatabaseObject(it).map.toMutableMap()

            val ref = if (it.id.isEmpty()) {
                val ref = collectionReference.document()
                entityData["id"] = ref.id
                entityData["removedAt"] = null
                val now = Instant.now().toEpochMilli()
                entityData["updatedAt"] = now
                entityData["createdAt"] = now
                ref
            } else {
                entityData["updatedAt"] = Instant.now().toEpochMilli()
                entityData["removedAt"] = null
                collectionReference
                    .document(it.id)
            }

            batch.set(ref, entityData)

            toEntityObject(entityData)
        }

        batch.commit()
        return newEntities
    }

    protected fun shouldNotSendData(
        error: FirebaseFirestoreException?,
        channel: SendChannel<*>
    ): Boolean {

        val r = channelToRegistration[channel] ?: return false

        if (error != null) {
            logError(error)
            r.remove()
            channelToRegistration.remove(channel)
            return true
        }

        if (channel.isClosedForSend) {
            r.remove()
            channelToRegistration.remove(channel)
            return true
        }
        return false
    }

    protected abstract fun toEntityObject(dataMap: MutableMap<String, Any?>): E

    protected abstract fun toDatabaseObject(entity: E): T

    protected fun extractDocument(ref: DocumentReference): E? {
        val result = ref.getSync()
        if (!result.exists()) {
            return null
        }

        return toEntityObject(result.data!!)
    }

    protected fun addRegistrationToChannel(
        registration: ListenerRegistration,
        channel: SendChannel<*>
    ) {
        channelToRegistration[channel] = registration
    }

    protected fun removeOldRegistrationForChannel(channel: Channel<*>) =
        channelToRegistration[channel]?.remove()

    protected val Query.notRemovedEntities
        get() =
            toEntityObjects(whereEqualTo("removedAt", null).documents)

    protected val Query.entities
        get() =
            toEntityObjects(documents)

    protected fun toEntityObjects(snapshots: List<DocumentSnapshot>) =
        snapshots.map { toEntityObject(it.data!!) }
}

abstract class BaseEntityFirestoreRepository<E, out T>(
    database: FirebaseFirestore,
    private val coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences,
    private val executor: ExecutorService
) : BaseFirestoreRepository<E, T>(
    database,
    sharedPreferences
), EntityRepository<E> where E : Entity, T : FirestoreModel {

    abstract val entityReference: DocumentReference

    override suspend fun listen(channel: Channel<E?>): Channel<E?> {
        Timber.d("AAA $executor")
        removeOldRegistrationForChannel(channel)
        addRegistrationToChannel(
            registration = entityReference
                .addSnapshotListener(
                    executor,
                    EventListener { snapshot, error ->
                        if (shouldNotSendData(error, channel)) return@EventListener
                        channel.offer(toEntityObject(snapshot!!.data!!))
                    }),
            channel = channel
        )
        return channel
    }

    override fun find(): E? =
        extractDocument(entityReference)
}

abstract class BaseCollectionFirestoreRepository<E, out T>(
    database: FirebaseFirestore,
    private val coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences,
    private val executor: ExecutorService
) : BaseFirestoreRepository<E, T>(
    database,
    sharedPreferences
), CollectionRepository<E> where E : Entity, T : FirestoreModel {

    override fun findById(id: String): E? =
        extractDocument(documentReference(id))

    override fun findAll() = collectionReference.notRemovedEntities

    override suspend fun listenById(id: String, channel: Channel<E?>): Channel<E?> {
        removeOldRegistrationForChannel(channel)
        addRegistrationToChannel(
            registration = documentReference(id)
                .addSnapshotListener(
                    executor,
                    EventListener { snapshot, error ->

                        if (shouldNotSendData(error, channel)) return@EventListener

                        channel.offer(toEntityObject(snapshot!!.data!!))
                    }),
            channel = channel
        )
        return channel
    }

    private suspend fun listen(query: Query, channel: Channel<List<E>>): Channel<List<E>> {
        removeOldRegistrationForChannel(channel)
        addRegistrationToChannel(
            registration = query
                .whereEqualTo("removedAt", null)
                .addSnapshotListener(
                    executor,
                    EventListener { snapshot, error ->

                        if (shouldNotSendData(error, channel)) return@EventListener

                        channel.offer(toEntityObjects(snapshot!!.documents))
                    }),
            channel = channel
        )
        return channel
    }

    override suspend fun listenForAll(channel: Channel<List<E>>) =
        collectionReference.listenForChanges(channel)

    override fun remove(entity: E) =
        remove(entity.id)

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

    protected suspend fun Query.listenForChanges(channel: Channel<List<E>>) =
        listen(this, channel)
}