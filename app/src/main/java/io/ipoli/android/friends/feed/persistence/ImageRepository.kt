package io.ipoli.android.friends.feed.persistence

import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import java.util.*

interface ImageRepository {

    fun savePostImage(imageData: ByteArray): String
}

class FirebaseStorageImageRepository : ImageRepository {

    override fun savePostImage(imageData: ByteArray): String {
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("posts/${UUID.randomUUID()}.jpg")
        Tasks.await(storageRef.putBytes(imageData))
        return Tasks.await(storageRef.downloadUrl).toString()
    }
}