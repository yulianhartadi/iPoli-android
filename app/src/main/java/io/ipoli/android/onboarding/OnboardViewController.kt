package io.ipoli.android.onboarding

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.onboarding.OnboardViewState.StateType.*
import io.ipoli.android.onboarding.scenes.*
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.auth.AuthViewController
import io.ipoli.android.player.auth.UsernameValidator
import io.ipoli.android.player.auth.UsernameValidator.ValidationError
import io.ipoli.android.player.auth.UsernameValidator.ValidationError.*
import io.ipoli.android.player.data.Avatar
import io.ipoli.android.quest.RepeatingQuest
import kotlinx.android.synthetic.main.controller_onboard.view.*

sealed class OnboardAction : Action {
    data class SelectAvatar(val index: Int) : OnboardAction()
    data class ValidatePetName(val name: String) : OnboardAction()
    data class ValidateUsername(val name: String) : OnboardAction()
    data class UsernameValidationFailed(val error: UsernameValidator.ValidationError) :
        OnboardAction()

    object SelectPet1 : OnboardAction()
    object SelectPet2 : OnboardAction()

    object ShowNext : OnboardAction()
    object LoadAvatars : OnboardAction()
    object LoadPets : OnboardAction()
    object Done : OnboardAction()
    object LoadFirstQuest : OnboardAction()
    object Skip : OnboardAction()

    data class UsernameValid(val username: String) : OnboardAction()
    data class LoadRepeatingQuests(val repeatingQuests: Map<Int, Pair<RepeatingQuest, OnboardViewController.OnboardTag?>>) :
        OnboardAction()

    data class SelectRepeatingQuest(
        val index: Int,
        val repeatingQuest: RepeatingQuest,
        val tag: OnboardViewController.OnboardTag?
    ) :
        OnboardAction()

    data class DeselectRepeatingQuest(val index: Int) : OnboardAction()
}

object OnboardReducer : BaseViewStateReducer<OnboardViewState>() {

    override fun reduce(
        state: AppState,
        subState: OnboardViewState,
        action: Action
    ) =
        when (action) {
            OnboardAction.ShowNext ->
                subState.copy(
                    type = NEXT_PAGE,
                    adapterPosition = subState.adapterPosition + 1
                )

            is OnboardAction.LoadAvatars ->
                subState.copy(
                    type = AVATARS_LOADED
                )

            is OnboardAction.SelectAvatar ->
                subState.copy(
                    type = AVATAR_SELECTED,
                    avatar = subState.avatars[action.index]
                )

            is OnboardAction.LoadPets ->
                subState.copy(
                    type = PETS_LOADED
                )

            is OnboardAction.SelectPet1 ->
                subState.copy(
                    type = PET_SELECTED,
                    pet = subState.pet1,
                    pet1 = subState.pet
                )

            is OnboardAction.SelectPet2 ->
                subState.copy(
                    type = PET_SELECTED,
                    pet = subState.pet2,
                    pet2 = subState.pet
                )

            is OnboardAction.ValidatePetName ->
                subState.copy(
                    type = if (action.name.isBlank()) PET_NAME_EMPTY
                    else PET_NAME_VALID,
                    petName = action.name
                )

            is OnboardAction.UsernameValidationFailed ->
                subState.copy(
                    type = USERNAME_VALIDATION_ERROR,
                    usernameValidationError = action.error
                )

            is OnboardAction.UsernameValid ->
                subState.copy(
                    type = USERNAME_VALID,
                    usernameValidationError = null,
                    username = action.username
                )

            is OnboardAction.LoadFirstQuest ->
                subState.copy(
                    type = FIRST_QUEST_DATA_LOADED
                )

            is OnboardAction.LoadRepeatingQuests ->
                subState.copy(
                    type = REPEATING_QUESTS_LOADED,
                    repeatingQuests = action.repeatingQuests
                )

            is OnboardAction.SelectRepeatingQuest ->
                subState.copy(
                    type = REPEATING_QUESTS_LOADED,
                    repeatingQuests = subState.repeatingQuests +
                        Pair(action.index, Pair(action.repeatingQuest, action.tag))
                )

            is OnboardAction.DeselectRepeatingQuest ->
                subState.copy(
                    type = REPEATING_QUESTS_LOADED,
                    repeatingQuests = subState.repeatingQuests - action.index
                )

            is OnboardAction.Done ->
                subState.copy(
                    type = DONE
                )

            is OnboardAction.Skip ->
                subState.copy(
                    type = DONE
                )

            else -> subState
        }

    override fun defaultState() =
        OnboardViewState(
            INITIAL,
            adapterPosition = 0,
            username = "",
            avatar = Avatar.AVATAR_03,
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
            usernameValidationError = null,
            petName = "",
            pet = PetAvatar.ELEPHANT,
            pet1 = PetAvatar.PIG,
            pet2 = PetAvatar.MONKEY,
            repeatingQuests = mapOf()
        )

    override val stateKey = key<OnboardViewState>()
}

data class OnboardViewState(
    val type: StateType,
    val adapterPosition: Int,
    val username: String,
    val avatar: Avatar,
    val avatars: List<Avatar>,
    val usernameValidationError: ValidationError?,
    val petName: String,
    val pet: PetAvatar,
    val pet1: PetAvatar,
    val pet2: PetAvatar,
    val repeatingQuests: Map<Int, Pair<RepeatingQuest, OnboardViewController.OnboardTag?>>
) : ViewState {
    enum class StateType {
        INITIAL,
        NEXT_PAGE,
        AVATARS_LOADED,
        AVATAR_SELECTED,
        USERNAME_VALIDATION_ERROR,
        USERNAME_VALID,
        PETS_LOADED,
        PET_SELECTED,
        PET_NAME_EMPTY,
        PET_NAME_VALID,
        DONE,
        REPEATING_QUESTS_LOADED,
        FIRST_QUEST_DATA_LOADED
    }
}

fun OnboardViewState.usernameErrorMessage(context: Context) =
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


data class OnboardData(
    val username: String,
    val avatar: Avatar,
    val petName: String,
    val petAvatar: PetAvatar,
    val repeatingQuests: List<Pair<RepeatingQuest, OnboardViewController.OnboardTag?>>
)

class OnboardViewController(args: Bundle? = null) :
    ReduxViewController<OnboardAction, OnboardViewState, OnboardReducer>(
        args
    ) {

    override val reducer = OnboardReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        enterFullScreen()
        return container.inflate(R.layout.controller_onboard)
    }

    override fun handleBack(): Boolean {
        dispatch(OnboardAction.Skip)
        return true
    }

    override fun render(state: OnboardViewState, view: View) {
        when (state.type) {
            NEXT_PAGE,
            INITIAL -> {
                changeChildController(
                    view = view,
                    adapterPosition = state.adapterPosition,
                    animate = false
                )

                renderBottomNavigation(state, view)
            }

            DONE -> {
                val onboardData = OnboardData(
                    username = state.username,
                    avatar = state.avatar,
                    petName = state.petName,
                    petAvatar = state.pet,
                    repeatingQuests = state.repeatingQuests.values.toList()
                )


                val changeHandler = VerticalChangeHandler()
                router.setRoot(
                    RouterTransaction.with(
                        AuthViewController(onboardData)
                    )
                        .pushChangeHandler(changeHandler)
                        .popChangeHandler(changeHandler)
                )
            }
        }
    }

    private fun renderBottomNavigation(
        state: OnboardViewState,
        view: View
    ) {
        if (state.adapterPosition == TIME_BEFORE) {
            view.onboardNavigation.gone()
        } else {
            view.onboardNavigation.visible()
            val selectedPosition =
                if (state.adapterPosition < TIME_BEFORE) STORY_INDEX
                else state.adapterPosition - 1

            view.onboardNavigation.children.forEachIndexed { index, child ->
                val background = child.background as GradientDrawable
                if (index == selectedPosition) background.setColor(colorRes(R.color.md_white))
                else background.setColor(colorRes(R.color.md_light_text_50))
            }
        }
    }

    private fun changeChildController(
        view: View,
        adapterPosition: Int,
        animate: Boolean = true
    ) {
        val childRouter = getChildRouter(view.onboardPager)

        val changeHandler = if (animate) HorizontalChangeHandler() else null

        val transaction = RouterTransaction.with(
            createControllerForPosition(adapterPosition)
        )
            .popChangeHandler(changeHandler)
            .pushChangeHandler(changeHandler)
        childRouter.pushController(transaction)
    }

    private fun createControllerForPosition(position: Int): Controller =
        when (position) {
            STORY_INDEX -> StoryViewController()
            TIME_BEFORE -> TimeBeforeViewController()
            AVATAR_INDEX -> AvatarViewController()
            PET_INDEX -> PetViewController()
            ADD_QUEST_INDEX -> FirstQuestViewController()
            PICK_REPEATING_QUESTS_INDEX -> PickRepeatingQuestsViewController()
            else -> throw IllegalArgumentException("Unknown controller position $position")
        }

    companion object {
        const val STORY_INDEX = 0
        const val TIME_BEFORE = 1
        const val AVATAR_INDEX = 2
        const val PET_INDEX = 3
        const val ADD_QUEST_INDEX = 4
        const val PICK_REPEATING_QUESTS_INDEX = 5

        const val TYPE_SPEED = 50
    }

    enum class OnboardTag { WELLNESS, PERSONAL, WORK }
}