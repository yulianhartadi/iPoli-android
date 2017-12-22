package mypoli.android

import com.couchbase.lite.*
import mypoli.android.player.persistence.model.CouchbasePlayer
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/22/17.
 */
class Migration(private val database: Database) {

    fun run() {
        val resultSet = Query.select(SelectResult.expression(Expression.meta().id))
            .from(DataSource.database(database))
            .where(Expression.property("type").equalTo(CouchbasePlayer.TYPE))
            .limit(1).run()

        val playerId = resultSet.next().getString("_id")
        Timber.d("AAAA $playerId")

        val doc = database.getDocument(playerId)
        if (!doc.contains("schemaVersion")) {
            doc.setInt("schemaVersion", Constants.SCHEMA_VERSION)
            doc.setInt("gems", 12)
            database.save(doc)
        }

    }
}