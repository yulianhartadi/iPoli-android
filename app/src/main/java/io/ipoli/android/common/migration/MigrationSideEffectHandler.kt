package io.ipoli.android.common.migration

import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.BuildConfig
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import space.traversal.kapsule.required
import timber.log.Timber

object MigrationSideEffectHandler : AppSideEffectHandler() {

    private val internetConnectionChecker by required { internetConnectionChecker }
    private val migrationExecutor by required { migrationExecutor }

    override suspend fun doExecute(action: Action, state: AppState) {

        when (action) {
            is MigrationAction.Load -> {
                startMigrating(action)
            }
        }

    }

    private suspend fun startMigrating(action: MigrationAction.Load) {
        if (internetConnectionChecker.isConnected()) {
            dispatch(MigrationAction.StartMigration)
            try {
                migrationExecutor.migrate(
                    playerSchemaVersion = action.playerSchemaVersion,
                    playerId = FirebaseAuth.getInstance().currentUser!!.uid
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