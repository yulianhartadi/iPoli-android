package io.ipoli.android.common.migration

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.Constants
import io.ipoli.android.common.di.Module
import io.ipoli.android.myPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

class FirestoreToLocalPlayerMigrator(private val context: Context) :
    Injects<Module> {

    private val dataImporter by required { dataImporter }
    private val remoteDatabase by required { remoteDatabase }

    fun migrate(playerId: String, @Suppress("UNUSED_PARAMETER") playerSchemaVersion: Int) {
        inject(myPoliApp.module(context))

        try {
            val playerRef = remoteDatabase
                .collection("players")
                .document(playerId)

            val batch = remoteDatabase.batch()
            batch.update(
                playerRef,
                mapOf("schemaVersion" to Constants.SCHEMA_VERSION)
            )
            Tasks.await(batch.commit())
            dataImporter.import()
        } catch (e: Throwable) {
            throw MigrationException("Unable to migrate Firestore Player to local db $playerId", e)
        }

        if (FirebaseAuth.getInstance().currentUser!!.isAnonymous) {
            FirebaseAuth.getInstance().signOut()
        }
    }
}

class MigrationException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
