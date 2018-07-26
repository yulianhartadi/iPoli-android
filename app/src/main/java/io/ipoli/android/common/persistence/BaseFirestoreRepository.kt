package io.ipoli.android.common.persistence

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Millisecond
import io.ipoli.android.quest.Entity

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/05/2018.
 */

abstract class BaseFirestoreRepository<E, out T>(
    protected val database: FirebaseFirestore
) : Repository<E> where E : Entity, T : FirestoreModel {

    abstract val collectionReference: CollectionReference

    protected val playerId: String
        get() = FirebaseAuth.getInstance().currentUser!!.uid

    override fun save(entity: E): E {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(entities: List<E>): List<E> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllForSync(lastSync: Duration<Millisecond>): List<E> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun addToBatch(entities: List<E>, batch: WriteBatch) {
        entities.forEach {
            addToBatch(it, batch)
        }
    }

    fun addToBatch(entity: E, batch: WriteBatch) {
        batch.set(
            collectionReference.document(entity.id),
            toDatabaseObject(entity).map
        )
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
    database: FirebaseFirestore
) : BaseFirestoreRepository<E, T>(
    database
) where E : Entity, T : FirestoreModel {

    abstract val entityReference: DocumentReference

    fun find(): E? =
        extractDocument(entityReference)
}

abstract class BaseCollectionFirestoreRepository<E, out T>(
    database: FirebaseFirestore
) : BaseFirestoreRepository<E, T>(
    database
) where E : Entity, T : FirestoreModel {

    fun findById(id: String): E? =
        extractDocument(documentReference(id))

    fun findAllNotRemoved() = collectionReference.notRemovedEntities

    fun findAll() = collectionReference.entities

    protected fun documentReference(id: String) = collectionReference.document(id)
}