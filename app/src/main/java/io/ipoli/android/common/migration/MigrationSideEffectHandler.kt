package io.ipoli.android.common.migration

import com.crashlytics.android.Crashlytics
import io.ipoli.android.BuildConfig
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import space.traversal.kapsule.required
import timber.log.Timber

object MigrationSideEffectHandler : AppSideEffectHandler() {

    private val internetConnectionChecker by required { internetConnectionChecker }
    private val firestoreToLocalPlayerMigrator by required { firestoreToLocalPlayerMigrator }

    override suspend fun doExecute(action: Action, state: AppState) {

        when (action) {
            is MigrationAction.Load -> {
                startMigrating(action)
            }
        }
    }

    private fun startMigrating(action: MigrationAction.Load) {

        if (internetConnectionChecker.isConnected()) {
            dispatch(MigrationAction.StartMigration)
            try {
                firestoreToLocalPlayerMigrator.migrate(
                    playerId = action.playerId,
                    playerSchemaVersion = action.playerSchemaVersion
                )
                dispatch(MigrationAction.CompleteMigration)
            } catch (e: MigrationException) {
                if (BuildConfig.DEBUG) {
                    Timber.e(e)
                } else {
                    Crashlytics.logException(e)
                }
                dispatch(MigrationAction.ShowMigrationError)
            }

        } else {
            dispatch(MigrationAction.ShowNoInternetConnection)
        }
    }

    override fun canHandle(action: Action) = action is MigrationAction

}