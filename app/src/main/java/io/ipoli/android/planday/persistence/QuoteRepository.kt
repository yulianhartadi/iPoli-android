package io.ipoli.android.planday.persistence

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/12/2018.
 */
data class Quote(val text: String, val author: String)

interface QuoteRepository {
    fun findRandomQuote(): Quote?
}

class FirestoreQuoteRepository(private val database: FirebaseFirestore) : QuoteRepository {

    override fun findRandomQuote(): Quote? {
        val quotesRef = database.collection("quotes")
        return try {
            chooseQuote(quotesRef)
        } catch (e: Exception) {
            null
        }
    }

    private fun chooseQuote(quotesRef: CollectionReference) =
        Tasks.await(quotesRef.get(Source.SERVER))
            .documents
            .shuffled()
            .first()
            .let {
                toEntityObject(it.data!!)
            }

    private fun toEntityObject(data: MutableMap<String, Any?>) =
        DbQuote(data).let {
            Quote(
                text = it.text,
                author = it.author
            )
        }
}

data class DbQuote(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var text: String by map
    var author: String by map
}