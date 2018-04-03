package io.ipoli.android.tag.persistence

import android.content.SharedPreferences
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.persistence.BaseCollectionFirestoreRepository
import io.ipoli.android.common.persistence.CollectionRepository
import io.ipoli.android.common.persistence.FirestoreModel
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/03/2018.
 */
interface TagRepository : CollectionRepository<Tag>

class FirestoreTagRepository(
    database: FirebaseFirestore,
    coroutineContext: CoroutineContext,
    sharedPreferences: SharedPreferences
) : BaseCollectionFirestoreRepository<Tag, DbTag>(
    database,
    coroutineContext,
    sharedPreferences
), TagRepository {
    override val collectionReference: CollectionReference
        get() = database.collection("players").document(playerId).collection("tags")

    override fun toEntityObject(dataMap: MutableMap<String, Any?>) =
        with(
            DbTag(dataMap.withDefault {
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
                updatedAt = updatedAt.instant,
                createdAt = createdAt.instant
            )
        }

    override fun toDatabaseObject(entity: Tag) =
        DbTag().apply {
            id = entity.id
            name = entity.name
            color = entity.color.name
            icon = entity.icon?.name
            updatedAt = entity.updatedAt.toEpochMilli()
            createdAt = entity.createdAt.toEpochMilli()
        }

}

class DbTag(override val map: MutableMap<String, Any?> = mutableMapOf()) : FirestoreModel {
    override var id: String by map
    var name: String by map
    var color: String by map
    var icon: String? by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map
}