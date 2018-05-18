package io.ipoli.android.planday.persistence

import android.os.Looper
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/12/2018.
 */

data class MotivationalImage(val url: String, val author: String, val authorUrl: String)

interface MotivationalImageRepository {
    fun findRandomImage(): MotivationalImage?
}

class FirestoreMotivationalImageRepository(private val database: FirebaseFirestore) :
    MotivationalImageRepository {

    override fun findRandomImage(): MotivationalImage? {
        val imagesRef = database.collection("motivationalImages")
        return try {
            chooseImage(imagesRef)
        } catch (e: Exception) {
            null
        }
    }

    private fun chooseImage(imagesRef: CollectionReference) =
        Tasks.await(imagesRef.get(Source.SERVER))
            .documents
            .shuffled()
            .first()
            .let {
                toEntityObject(it.data!!)
            }

    private fun toEntityObject(data: MutableMap<String, Any?>): MotivationalImage {
        val dbMotivationalImage = DbMotivationalImage(data)
        return MotivationalImage(
            url = dbMotivationalImage.url,
            author = dbMotivationalImage.author,
            authorUrl = dbMotivationalImage.authorUrl
        )
    }
}

data class DbMotivationalImage(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var url: String by map
    var author: String by map
    var authorUrl: String by map
}