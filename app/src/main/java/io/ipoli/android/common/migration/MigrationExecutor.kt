package io.ipoli.android.common.migration

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import io.ipoli.android.Constants
import io.ipoli.android.player.Player

interface Migration {
    val fromVersion: Int
    val toVersion: Int

    suspend fun execute(database: FirebaseFirestore, playerId: String)
}

abstract class FirestoreMigration : Migration {

    protected fun playerRef(database: FirebaseFirestore, playerId: String) =
        database
            .collection("players")
            .document(playerId)

    protected fun playerCollectionRef(
        collection: String,
        database: FirebaseFirestore,
        playerId: String
    ) =
        database
            .collection("players")
            .document(playerId)
            .collection(collection)
}

class MigrationFrom100To101 : FirestoreMigration() {
    override val fromVersion = 100
    override val toVersion = 101

    override suspend fun execute(database: FirebaseFirestore, playerId: String) {
        val updateData = mutableMapOf(
            "schemaVersion" to Constants.SCHEMA_VERSION,
            "preferences.planDayStartMinute" to Constants.DEFAULT_PLAN_DAY_REMINDER_START_MINUTE,
            "preferences.planDays" to Constants.DEFAULT_PLAN_DAYS.map { it.name },
            "preferences.temperatureUnit" to Player.Preferences.TemperatureUnit.FAHRENHEIT.name
        )
        Tasks.await(
            playerRef(database, playerId)
                .update(updateData)
        )
    }
}

class MigrationFrom101To102 : FirestoreMigration() {

    override val fromVersion = 101
    override val toVersion = 102

    override suspend fun execute(database: FirebaseFirestore, playerId: String) {

        val tagDocs = Tasks.await(
            playerCollectionRef("tags", database, playerId)
                .get(Source.SERVER)
        ).documents

        val qDocs = Tasks.await(
            playerCollectionRef("quests", database, playerId)
                .get(Source.SERVER)
        ).documents

        val rqDocs = Tasks.await(
            playerCollectionRef("repeatingQuests", database, playerId)
                .get(Source.SERVER)
        ).documents

        val csDocs = Tasks.await(
            playerCollectionRef("challenges", database, playerId)
                .get(Source.SERVER)
        ).documents

        val batch = database.batch()

        for (qDoc in qDocs) {
            if (!qDoc.contains("tagIds")) {
                continue
            }

            val qRef = playerCollectionRef("quests", database, playerId)
                .document(qDoc.id)

            val tagIdsData = qDoc.get("tagIds") as Map<String, Any?>

            batch.update(qRef, createUpdateTagsData(tagIdsData, tagDocs))
        }

        for (rqDoc in rqDocs) {
            if (!rqDoc.contains("tagIds")) {
                continue
            }

            val rqRef = playerCollectionRef("repeatingQuests", database, playerId)
                .document(rqDoc.id)

            val tagIdsData = rqDoc.get("tagIds") as Map<String, Any?>

            batch.update(rqRef, createUpdateTagsData(tagIdsData, tagDocs))
        }

        for (cDoc in csDocs) {
            if (!cDoc.contains("tagIds")) {
                continue
            }

            val cRef = playerCollectionRef("challenges", database, playerId)
                .document(cDoc.id)

            val tagIdsData = cDoc.get("tagIds") as Map<String, Any?>

            batch.update(cRef, createUpdateTagsData(tagIdsData, tagDocs))
        }

        batch.update(
            playerRef(database, playerId),
            mapOf("schemaVersion" to Constants.SCHEMA_VERSION)
        )

        Tasks.await(batch.commit())
    }

    private fun createUpdateTagsData(
        tIdData: Map<String, Any?>,
        tagDocs: List<DocumentSnapshot>
    ) =
        mutableMapOf(
            "tags" to tIdData.map {
                val tId = it.key
                val tDoc = tagDocs.first { it.id == tId }
                tId to mutableMapOf(
                    "id" to tId,
                    "name" to tDoc.getString("name"),
                    "color" to tDoc.getString("color"),
                    "icon" to tDoc.getString("icon"),
                    "isFavorite" to tDoc.getBoolean("isFavorite")
                )
            }.toMap(),
            "tagIds" to FieldValue.delete()
        )

}

class MigrationFrom102To103 : FirestoreMigration() {

    override val fromVersion = 102
    override val toVersion = 103

    override suspend fun execute(database: FirebaseFirestore, playerId: String) {

        val qDocs = Tasks.await(
            playerCollectionRef("quests", database, playerId)
                .get(Source.SERVER)
        ).documents

        val rqDocs = Tasks.await(
            playerCollectionRef("repeatingQuests", database, playerId)
                .get(Source.SERVER)
        ).documents

        val csDocs = Tasks.await(
            playerCollectionRef("challenges", database, playerId)
                .get(Source.SERVER)
        ).documents

        val batch = database.batch()

        for (qDoc in qDocs) {
            if (!qDoc.contains("tags")) {

                val qRef = playerCollectionRef("quests", database, playerId)
                    .document(qDoc.id)

                batch.update(qRef, mapOf("tags" to mapOf<String, Any?>()))
            }
        }

        for (rqDoc in rqDocs) {

            if (!rqDoc.contains("tags")) {

                val rqRef = playerCollectionRef("repeatingQuests", database, playerId)
                    .document(rqDoc.id)

                batch.update(rqRef, mapOf("tags" to mapOf<String, Any?>()))
            }
        }

        for (cDoc in csDocs) {
            if (!cDoc.contains("tags")) {

                val cRef = playerCollectionRef("challenges", database, playerId)
                    .document(cDoc.id)

                batch.update(cRef, mapOf("tags" to mapOf<String, Any?>()))
            }
        }

        batch.update(
            playerRef(database, playerId),
            mapOf("schemaVersion" to Constants.SCHEMA_VERSION)
        )

        Tasks.await(batch.commit())
    }

}

class MigrationException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

class MigrationExecutor(
    private val database: FirebaseFirestore,
    private val migrations: List<Migration>
) {

    fun shouldMigrate(playerSchemaVersion: Int) =
        playerSchemaVersion != Constants.SCHEMA_VERSION

    suspend fun migrate(playerSchemaVersion: Int, playerId: String) {
        if (!shouldMigrate(playerSchemaVersion)) {
            return
        }

        var currentSchemaVersion = playerSchemaVersion

        val sortedMigrations = migrations.sortedBy { it.fromVersion }


        for (m in sortedMigrations) {
            if (m.fromVersion == currentSchemaVersion) {
                try {
                    m.execute(database, playerId)
                } catch (e: Throwable) {
                    throw MigrationException(
                        "Unable to migrate Player $playerId from ${m.fromVersion} to ${m.toVersion}",
                        e
                    )
                }
                currentSchemaVersion = m.toVersion
            }
        }


    }
}