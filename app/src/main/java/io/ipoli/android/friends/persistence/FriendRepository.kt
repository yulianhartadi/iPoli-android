package io.ipoli.android.friends.persistence

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.persistence.documents
import io.ipoli.android.common.persistence.getSync
import io.ipoli.android.friends.feed.persistence.DbPost
import io.ipoli.android.friends.feed.persistence.DbReferencePost
import io.ipoli.android.player.data.Avatar
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.threeten.bp.Instant

data class Friend(
    val id: String,
    val displayName: String,
    val username: String,
    val avatar: Avatar,
    val level: Int,
    val createdAt: Instant
)

interface FriendRepository {

    fun isFriend(friendId: String): Boolean

    fun friend(friendId: String)

    fun unfriend(friendId: String)

    fun findAll(): List<Friend>

    fun find(friendId: String): Friend
}

class FirestoreFriendRepository(private val remoteDatabase: FirebaseFirestore) : FriendRepository {

    override fun isFriend(friendId: String) =
        friendsReference(FirebaseAuth.getInstance().currentUser!!.uid)
            .documents
            .map { it.id }
            .toSet()
            .contains(friendId)

    override fun find(friendId: String) =
        getPlayerDocument(friendId)
            .let {
                val dbFriend = DbFriend(it.data!!)
                toEntityObject(dbFriend)
            }

    override fun findAll() =
        friendsReference(FirebaseAuth.getInstance().currentUser!!.uid)
            .documents
            .map { it.id }
            .map { async { getPlayerDocument(it) } }
            .let { executeAndCreateEntities(it) }


    private fun getPlayerDocument(playerId: String) =
        remoteDatabase
            .collection("players")
            .document(playerId)
            .getSync()

    private fun executeAndCreateEntities(friendJobs: List<Deferred<DocumentSnapshot>>) =
        runBlocking {
            friendJobs.map {
                val doc = it.await()
                val dbFriend = DbFriend(doc.data!!)
                toEntityObject(dbFriend)
            }
        }

    private fun toEntityObject(dbFriend: DbFriend) = Friend(
        id = dbFriend.id,
        displayName = dbFriend.displayName,
        username = dbFriend.username,
        avatar = Avatar.valueOf(dbFriend.avatar),
        level = dbFriend.level.toInt(),
        createdAt = dbFriend.createdAt.instant
    )

    override fun friend(friendId: String) {
        val playerId = FirebaseAuth.getInstance().currentUser!!.uid

        var batch = remoteDatabase.batch()

        batch.set(
            friendReference(playerId, friendId),
            mapOf("id" to friendId)
        )

        batch.set(
            friendReference(friendId, playerId),
            mapOf("id" to playerId)
        )

        var cnt = 2

        val playerPosts = remoteDatabase
            .collection("players")
            .document(playerId)
            .collection("posts")
            .documents
            .map { DbPost(it.data!!) }

        val friendPosts = remoteDatabase
            .collection("players")
            .document(friendId)
            .collection("posts")
            .documents
            .map { DbPost(it.data!!) }

        playerPosts.forEach {
            val ref = remoteDatabase
                .collection("players")
                .document(friendId)
                .collection("referencePosts")
                .document(it.id)
            val refPost = DbReferencePost().apply {
                this.playerId = playerId
                postId = it.id
                createdAt = it.createdAt
                updatedAt = it.updatedAt
            }
            batch.set(ref, refPost.map)
            cnt++

            if (cnt == 500) {
                Tasks.await(batch.commit())
                batch = remoteDatabase.batch()
                cnt = 0
            }
        }

        friendPosts.forEach {
            val ref = remoteDatabase
                .collection("players")
                .document(playerId)
                .collection("referencePosts")
                .document(it.id)
            val refPost = DbReferencePost().apply {
                this.playerId = friendId
                postId = it.id
                createdAt = it.createdAt
                updatedAt = it.updatedAt
            }
            batch.set(ref, refPost.map)
            cnt++

            if (cnt == 500) {
                Tasks.await(batch.commit())
                batch = remoteDatabase.batch()
                cnt = 0
            }
        }

        Tasks.await(batch.commit())

    }

    override fun unfriend(friendId: String) {
        val playerId = FirebaseAuth.getInstance().currentUser!!.uid

        var batch = remoteDatabase.batch()

        batch.delete(friendReference(playerId, friendId))
        batch.delete(friendReference(friendId, playerId))

        var cnt = 2

        remoteDatabase
            .collection("players")
            .document(playerId)
            .collection("referencePosts")
            .whereEqualTo("playerId", friendId)
            .documents
            .forEach {
                batch.delete(it.reference)
                cnt++
                if (cnt == 500) {
                    Tasks.await(batch.commit())
                    batch = remoteDatabase.batch()
                    cnt = 0
                }
            }

        remoteDatabase
            .collection("players")
            .document(friendId)
            .collection("referencePosts")
            .whereEqualTo("playerId", playerId)
            .documents
            .forEach {
                batch.delete(it.reference)
                cnt++
                if (cnt == 500) {
                    Tasks.await(batch.commit())
                    batch = remoteDatabase.batch()
                    cnt = 0
                }
            }

        Tasks.await(batch.commit())
    }

    private fun friendReference(
        player1Id: String,
        player2Id: String
    ) =
        friendsReference(player1Id)
            .document(player2Id)

    private fun friendsReference(playerId: String) =
        remoteDatabase
            .collection("players")
            .document(playerId)
            .collection("friends")

}

class DbFriend(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var id: String by map
    var displayName: String by map
    var username: String by map
    var avatar: String by map
    var level: Long by map
    val createdAt: Long by map
}