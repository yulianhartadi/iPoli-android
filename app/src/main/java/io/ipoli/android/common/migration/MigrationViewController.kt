package io.ipoli.android.common.migration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.EmailUtils

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import kotlinx.android.synthetic.main.controller_migration.view.*

sealed class MigrationAction : Action {

    data class Load(val playerId: String, val playerSchemaVersion: Int) : MigrationAction()
    object StartMigration : MigrationAction()
    object ShowNoInternetConnection : MigrationAction()
    object CompleteMigration : MigrationAction()
    object ShowMigrationError : MigrationAction()
}

data class MigrationViewState(val type: StateType) : BaseViewState() {

    enum class StateType {
        LOADING, NO_INTERNET, MIGRATING, DONE, ERROR
    }
}

object MigrationReducer : BaseViewStateReducer<MigrationViewState>() {
    override fun reduce(
        state: AppState,
        subState: MigrationViewState,
        action: Action
    ) =
        when (action) {
            is MigrationAction.Load -> subState.copy(type = MigrationViewState.StateType.LOADING)
            is MigrationAction.StartMigration -> subState.copy(type = MigrationViewState.StateType.MIGRATING)
            is MigrationAction.ShowNoInternetConnection -> subState.copy(type = MigrationViewState.StateType.NO_INTERNET)
            is MigrationAction.CompleteMigration -> subState.copy(type = MigrationViewState.StateType.DONE)
            is MigrationAction.ShowMigrationError -> subState.copy(type = MigrationViewState.StateType.ERROR)
            else -> subState
        }

    override fun defaultState() = MigrationViewState(type = MigrationViewState.StateType.LOADING)

    override val stateKey = key<MigrationViewState>()

}

class MigrationViewController(args: Bundle? = null) :
    ReduxViewController<MigrationAction, MigrationViewState, MigrationReducer>(
        args = args
    ) {

    override val reducer = MigrationReducer

    private var playerId = ""
    private var playerSchemaVersion = 0

    constructor(playerId: String, playerSchemaVersion: Int) : this() {
        this.playerId = playerId
        this.playerSchemaVersion = playerSchemaVersion
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = container.inflate(R.layout.controller_migration)
        view.migrationAnimation.setAnimation("migration_loading.json")
        view.migrationAnimation.playAnimation()
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        enterFullScreen()
    }

    override fun onDetach(view: View) {
        exitFullScreen()
        super.onDetach(view)
    }

    override fun handleBack(): Boolean {
        return true
    }

    override fun onCreateLoadAction() = MigrationAction.Load(playerId, playerSchemaVersion)

    override fun render(state: MigrationViewState, view: View) {
        when (state.type) {

            MigrationViewState.StateType.MIGRATING -> {
                if (!view.migrationAnimation.isAnimating) {
                    view.migrationAnimation.playAnimation()
                }
                view.migrationTitle.text = stringRes(R.string.migration_title)
                view.migrationMessage.text = stringRes(R.string.migration_message)
            }

            MigrationViewState.StateType.NO_INTERNET -> {
                view.migrationAction.visible()
                view.migrationTitle.text = stringRes(R.string.migration_no_internet_title)
                view.migrationMessage.text = stringRes(R.string.migration_no_internet_message)
                view.migrationMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    IconicsDrawable(view.context).normalIcon(
                        GoogleMaterial.Icon.gmd_warning,
                        R.color.md_orange_500
                    ),
                    null,
                    null,
                    null
                )
                view.postDelayed({ view.migrationAnimation.cancelAnimation() }, 100)
                view.migrationAction.setText(R.string.retry)
                view.migrationAction.onDebounceClick {
                    view.migrationAction.gone()
                    view.migrationMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                    dispatch(MigrationAction.Load(playerId, playerSchemaVersion))
                }
            }

            MigrationViewState.StateType.ERROR -> {
                view.migrationAction.visible()
                view.postDelayed({ view.migrationAnimation.cancelAnimation() }, 100)
                view.migrationAction.setText(R.string.contact_us)
                view.migrationMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    IconicsDrawable(view.context).normalIcon(
                        GoogleMaterial.Icon.gmd_error,
                        R.color.md_red_500
                    ),
                    null,
                    null,
                    null
                )

                view.migrationTitle.text = stringRes(R.string.migration_error_title)
                view.migrationMessage.text = stringRes(R.string.migration_error_message)

                view.migrationAction.onDebounceClick {
                    view.migrationAction.gone()
                    EmailUtils.send(
                        context = it.context,
                        subject = "MIGRATION ERROR: Please, help",
                        playerId = FirebaseAuth.getInstance().currentUser!!.uid,
                        chooserTitle = stringRes(R.string.contact_us_email_chooser_title)
                    )
                    rootRouter.popCurrentController()
                    activity?.finish()
                }
            }

            MigrationViewState.StateType.DONE -> {
                rootRouter.popCurrentController()
                activity?.recreate()
            }

            else -> {
            }
        }
    }

}