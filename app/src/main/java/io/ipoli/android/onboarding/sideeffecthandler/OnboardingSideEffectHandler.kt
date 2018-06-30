package io.ipoli.android.onboarding.sideeffecthandler

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.onboarding.OnboardAction
import io.ipoli.android.player.auth.UsernameValidator
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/7/18.
 */
object OnboardingSideEffectHandler : AppSideEffectHandler() {

    private val playerRepository by required { playerRepository }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is OnboardAction.ValidateUsername -> {
                val usernameValidationError =
                    UsernameValidator(playerRepository).validate(action.name)
                if (usernameValidationError != null) {
                    dispatch(
                        OnboardAction.UsernameValidationFailed(
                            usernameValidationError
                        )
                    )
                } else {
                    dispatch(OnboardAction.UsernameValid(action.name))
                }
            }
        }
    }

    override fun canHandle(action: Action) = action is OnboardAction

}