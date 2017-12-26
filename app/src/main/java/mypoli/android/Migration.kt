package mypoli.android

import com.couchbase.lite.*
import com.couchbase.lite.Array
import mypoli.android.player.persistence.model.CouchbasePlayer

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
        val doc = database.getDocument(playerId)

        if (!doc.contains("schemaVersion")) {
            doc.setInt("schemaVersion", Constants.SCHEMA_VERSION)
            doc.setInt("gems", 0)
            database.save(doc)
        } else if (Constants.SCHEMA_VERSION == 2) {
            val inventoryPets = doc.getDictionary("inventory").getArray("pets")
            inventoryPets.forEach {
                (it as Dictionary).setArray("items", Array())
            }
            val pet = doc.getDictionary("pet")
            val equipment = Dictionary()
            equipment.setString("hat", null)
            equipment.setString("mask", null)
            equipment.setString("bodyArmor", null)
            pet.setDictionary("equipment", equipment)
            database.save(doc)
        }

    }
}