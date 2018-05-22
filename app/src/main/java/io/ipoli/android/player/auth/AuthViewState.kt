package io.ipoli.android.player.auth

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.onboarding.OnboardData
import io.ipoli.android.onboarding.OnboardViewController
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.auth.AuthViewState.StateType.*
import io.ipoli.android.player.auth.UsernameValidator.ValidationError
import io.ipoli.android.player.auth.UsernameValidator.ValidationError.*
import io.ipoli.android.player.data.Avatar
import io.ipoli.android.quest.RepeatingQuest

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/5/18.
 */
sealed class AuthAction : Action {
    data class Load(val onboardData: OnboardData?) : AuthAction()
    data class Loaded(
        val hasPlayer: Boolean,
        val isGuest: Boolean,
        val hasUsername: Boolean,
        val onboardData: OnboardData?
    ) : AuthAction()

    data class UserAuthenticated(val user: FirebaseUser) : AuthAction()
    data class UsernameValidationFailed(val error: ValidationError) : AuthAction()
    data class CompleteSetup(
        val username: String,
        val avatar: Avatar
    ) : AuthAction()

    data class ValidateUsername(val username: String) : AuthAction()
    data class ChangeAvatar(val avatar: Avatar) : AuthAction()

    object AccountsLinked : AuthAction()
    object GuestCreated : AuthAction()
    object PlayerSetupCompleted : AuthAction()
    data class PlayerLoggedIn(val shouldMigrate: Boolean, val schemaVersion: Int) : AuthAction()
    data class ExistingPlayerLoggedInFromGuest(val shouldMigrate: Boolean, val schemaVersion: Int) : AuthAction()
    object ShowSetUp : AuthAction()
    object UsernameValid : AuthAction()
}

object AuthReducer : BaseViewStateReducer<AuthViewState>() {

    override val stateKey = key<AuthViewState>()

    override fun reduce(state: AppState, subState: AuthViewState, action: Action) =
        when (action) {
            is AuthAction.Loaded -> {
                val hasPlayer = action.hasPlayer
                val isGuest = action.isGuest
                val hasUsername = action.hasUsername
                val onboardData = action.onboardData
                subState.copy(
                    type = if (!hasPlayer || isGuest) {
                        SHOW_LOGIN
                    } else if (!hasUsername) {
                        SHOW_SETUP
                    } else {
                        throw  IllegalStateException("Player is already authenticated and has username")
                    },
                    isGuest = action.isGuest,
                    username = onboardData?.username ?: subState.username,
                    playerAvatar = onboardData?.avatar ?: subState.playerAvatar,
                    petName = onboardData?.petName ?: subState.petName,
                    petAvatar = onboardData?.petAvatar ?: subState.petAvatar,
                    repeatingQuests = onboardData?.repeatingQuests ?: subState.repeatingQuests
                )
            }

            is AuthAction.UsernameValidationFailed -> {
                subState.copy(
                    type = USERNAME_VALIDATION_ERROR,
                    usernameValidationError = action.error
                )
            }

            AuthAction.UsernameValid -> {
                subState.copy(
                    type = USERNAME_VALID
                )
            }

            AuthAction.GuestCreated -> {
                subState.copy(
                    type = GUEST_CREATED
                )
            }

            AuthAction.PlayerSetupCompleted -> {
                subState.copy(
                    type = PLAYER_SETUP_COMPLETED
                )
            }

            is AuthAction.PlayerLoggedIn -> {
                subState.copy(
                    type = PLAYER_LOGGED_IN,
                    shouldMigrate = action.shouldMigrate,
                    schemaVersion = action.schemaVersion
                )
            }

            is AuthAction.ExistingPlayerLoggedInFromGuest -> {
                subState.copy(
                    type = EXISTING_PLAYER_LOGGED_IN_FROM_GUEST,
                    shouldMigrate = action.shouldMigrate,
                    schemaVersion = action.schemaVersion
                )
            }

            AuthAction.ShowSetUp -> {
                subState.copy(
                    type = SWITCH_TO_SETUP
                )
            }

            AuthAction.AccountsLinked -> {
                subState.copy(
                    type = SWITCH_TO_SETUP
                )
            }

            is AuthAction.ChangeAvatar -> {
                subState.copy(
                    type = AVATAR_CHANGED,
                    playerAvatar = action.avatar
                )
            }

            else -> subState

        }

    override fun defaultState() =
        AuthViewState(
            type = LOADING,
            usernameValidationError = null,
            isGuest = false,
            username = "",
            playerAvatar = Avatar.AVATAR_03,
            avatars = listOf(
                Avatar.AVATAR_03,
                Avatar.AVATAR_02,
                Avatar.AVATAR_01,
                Avatar.AVATAR_04,
                Avatar.AVATAR_05,
                Avatar.AVATAR_06,
                Avatar.AVATAR_07,
                Avatar.AVATAR_11
            ),
            petName = null,
            petAvatar = null,
            repeatingQuests = emptyList(),
            shouldMigrate = false,
            schemaVersion = Constants.SCHEMA_VERSION
        )

}

data class AuthViewState(
    val type: AuthViewState.StateType,
    val usernameValidationError: ValidationError?,
    val isGuest: Boolean,
    val username: String,
    val playerAvatar: Avatar,
    val avatars: List<Avatar>,
    val petName: String?,
    val petAvatar: PetAvatar?,
    val repeatingQuests: List<Pair<RepeatingQuest, OnboardViewController.OnboardTag?>>,
    val shouldMigrate : Boolean,
    val schemaVersion: Int
) : BaseViewState() {
    enum class StateType {
        IDLE,
        LOADING,
        SHOW_LOGIN,
        SHOW_SETUP,
        SWITCH_TO_SETUP,
        USERNAME_VALIDATION_ERROR,
        USERNAME_VALID,
        GUEST_CREATED,
        PLAYER_SETUP_COMPLETED,
        PLAYER_LOGGED_IN,
        EXISTING_PLAYER_LOGGED_IN_FROM_GUEST,
        AVATAR_CHANGED
    }
}

fun AuthViewState.usernameErrorMessage(context: Context) =
    usernameValidationError?.let {
        when (it) {
            EMPTY_USERNAME -> context.getString(R.string.username_is_empty)
            EXISTING_USERNAME -> context.getString(R.string.username_is_taken)
            INVALID_FORMAT -> context.getString(R.string.username_wrong_format)
            INVALID_LENGTH -> context.getString(
                R.string.username_wrong_length,
                Constants.USERNAME_MIN_LENGTH,
                Constants.USERNAME_MAX_LENGTH
            )
        }
    }