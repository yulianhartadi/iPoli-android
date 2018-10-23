package io.ipoli.android.friends.persistence

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.persistence.documents
import io.ipoli.android.common.persistence.getSync
import io.ipoli.android.player.data.Avatar
import kotlinx.coroutines.experimental.*
import org.threeten.bp.Instant

data class Friend(
    val id: String,
    val displayName: String,
    val username: String,
    val avatar: Avatar,
    val level: Int,
    val createdAt: Instant,
    val isFollowing: Boolean,
    val isFollower: Boolean
)

interface FriendRepository {

    fun isFriend(friendId: String): Boolean

    fun follow(friendId: String)

    fun unfollow(friendId: String)

    fun friend(friendId: String)

    fun count(): Int

    fun find(friendId: String): Friend

    fun findFollowers(playerId: String): List<Friend>

    fun findFollowing(playerId: String): List<Friend>

    fun isFollowing(friendId: String): Boolean

    fun isFollower(friendId: String): Boolean
}

class FirestoreFriendRepository(
    private val remoteDatabase: FirebaseFirestore
) : FriendRepository {

    override fun follow(friendId: String) {
        val playerId = FirebaseAuth.getInstance().currentUser!!.uid
        val batch = remoteDatabase.batch()

        batch.set(
            followersReference(friendId)
                .document(playerId),
            mapOf(playerId to true)
        )
        batch.set(
            followingReference(playerId)
                .document(friendId),
            mapOf(friendId to true)
        )

        Tasks.await(batch.commit())
    }

    override fun unfollow(friendId: String) {
        val playerId = FirebaseAuth.getInstance().currentUser!!.uid
        val batch = remoteDatabase.batch()

        batch.delete(
            followersReference(friendId)
                .document(playerId)
        )
        batch.delete(
            followingReference(playerId)
                .document(friendId)
        )

        Tasks.await(batch.commit())
    }

    override fun isFriend(friendId: String) =
        isFollower(friendId) && isFollowing(friendId)

    override fun find(friendId: String) =
        getPlayerDocument(friendId)
            .let {
                val dbFriend = DbFriend(it.data!!)
                dbFriend.isFollower = isFollower(dbFriend.id)
                dbFriend.isFollowing = isFollowing(dbFriend.id)
                toEntityObject(dbFriend)
            }

    override fun count(): Int {
        val playerId = FirebaseAuth.getInstance().currentUser!!.uid
        val followers = followersReference(playerId).documents.map { it.id }.toSet()
        val following = followingReference(playerId).documents.map { it.id }.toSet()
        return followers.union(following).size
    }

    private fun getPlayerDocument(playerId: String) =
        remoteDatabase
            .collection("players")
            .document(playerId)
            .getSync()

    private fun executeAndCreateEntities(friendJobs: List<Deferred<DocumentSnapshot>>) =
        runBlocking(Dispatchers.Unconfined) {

            val followers = mutableSetOf<String>()
            val following = mutableSetOf<String>()

            FirebaseAuth.getInstance().currentUser?.let {
                val playerId = it.uid
                followers.addAll(followersReference(playerId).documents.map { it.id })
                following.addAll(followingReference(playerId).documents.map { it.id })
            }
            friendJobs.map {
                val doc = it.await()
                val dbFriend = DbFriend(doc.data!!)
                dbFriend.isFollower = followers.contains(dbFriend.id)
                dbFriend.isFollowing = following.contains(dbFriend.id)
                toEntityObject(dbFriend)
            }
        }

    private fun toEntityObject(dbFriend: DbFriend) =
        Friend(
            id = dbFriend.id,
            displayName = dbFriend.displayName,
            username = dbFriend.username,
            avatar = Avatar.valueOf(dbFriend.avatar),
            level = dbFriend.level.toInt(),
            isFollower = dbFriend.isFollower,
            isFollowing = dbFriend.isFollowing,
            createdAt = dbFriend.createdAt.instant
        )

    override fun friend(friendId: String) {
        val playerId = FirebaseAuth.getInstance().currentUser!!.uid

        val batch = remoteDatabase.batch()

        batch.set(
            followersReference(friendId)
                .document(playerId),
            mapOf(playerId to true)
        )
        batch.set(
            followersReference(playerId)
                .document(friendId),
            mapOf(friendId to true)
        )

        batch.set(
            followingReference(playerId)
                .document(friendId),
            mapOf(friendId to true)
        )
        batch.set(
            followingReference(friendId)
                .document(playerId),
            mapOf(playerId to true)
        )

        Tasks.await(batch.commit())

    }

    override fun findFollowers(playerId: String): List<Friend> {
        val playerIds = followersReference(playerId)
            .documents
            .map { it.id }

        return executeAndCreateEntities(
            playerIds
                .map {
                    GlobalScope.async(Dispatchers.IO) { getPlayerDocument(it) }
                }
        )
    }

    override fun findFollowing(playerId: String): List<Friend> {
        val playerIds = followingReference(playerId)
            .documents
            .map { it.id }

        return executeAndCreateEntities(
            playerIds
                .map {
                    GlobalScope.async(Dispatchers.IO) { getPlayerDocument(it) }
                }
        )
    }

    override fun isFollowing(friendId: String) =
        followingReference(FirebaseAuth.getInstance().currentUser!!.uid).document(friendId).getSync().exists()

    override fun isFollower(friendId: String) =
        followersReference(FirebaseAuth.getInstance().currentUser!!.uid).document(friendId).getSync().exists()

    private fun followersReference(playerId: String) =
        remoteDatabase
            .collection("players")
            .document(playerId)
            .collection("followers")

    private fun followingReference(playerId: String) =
        remoteDatabase
            .collection("players")
            .document(playerId)
            .collection("following")

}

class DbFriend(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var id: String by map
    var displayName: String by map
    var username: String by map
    var avatar: String by map
    var level: Long by map
    var isFollowing: Boolean by map
    var isFollower: Boolean by map
    val createdAt: Long by map
}