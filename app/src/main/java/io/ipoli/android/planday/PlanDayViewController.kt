package io.ipoli.android.planday

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.TimeOfDay
import io.ipoli.android.common.home.HomeViewController
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.text.DateFormatter
import io.ipoli.android.common.view.exitFullScreen
import io.ipoli.android.common.view.inflate
import io.ipoli.android.common.view.rootRouter
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.planday.data.Weather
import io.ipoli.android.planday.persistence.Quote
import io.ipoli.android.planday.scenes.PlanDayMotivationViewController
import io.ipoli.android.planday.scenes.PlanDayReviewViewController
import io.ipoli.android.planday.scenes.PlanDayTodayViewController
import io.ipoli.android.planday.usecase.CalculateAwesomenessScoreUseCase
import io.ipoli.android.player.Player
import io.ipoli.android.quest.Quest
import kotlinx.android.synthetic.main.controller_plan_day.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.*

sealed class PlanDayAction : Action {
    data class CompleteQuest(val questId: String) : PlanDayAction()
    data class UndoCompleteQuest(val questId: String) : PlanDayAction()
    data class ScheduleQuestForToday(val questId: String) : PlanDayAction()
    data class MoveQuestToBucketList(val questId: String) : PlanDayAction()
    data class RemoveQuest(val questId: String) : PlanDayAction()
    data class UndoRemoveQuest(val questId: String) : PlanDayAction()
    data class RescheduleQuest(val questId: String, val date: LocalDate?) : PlanDayAction()
    data class AcceptSuggestion(val questId: String) : PlanDayAction()

    object Load : PlanDayAction()
    object ShowNext : PlanDayAction()
    object GetWeather : PlanDayAction()
    object ImageLoaded : PlanDayAction()
    object RisingSunAnimationDone : PlanDayAction()
    object LoadReviewDay : PlanDayAction()
    object Done : PlanDayAction()
    object Back : PlanDayAction()
    object LoadToday : PlanDayAction()
}

object PlanDayReducer : BaseViewStateReducer<PlanDayViewState>() {

    override fun reduce(
        state: AppState,
        subState: PlanDayViewState,
        action: Action
    ) =
        when (action) {

            is PlanDayAction.Load -> {
                val today = LocalDate.now()
                val weekDayText =
                    today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

                val player = state.dataState.player

                val playerName = player?.let {
                    it.displayName?.let { firstNameOf(it) }
                }

                subState.copy(
                    playerName = playerName,
                    dateText = "$weekDayText, ${DateFormatter.formatDayWithWeek(today)}",
                    petAvatar = player?.pet?.avatar ?: subState.petAvatar,
                    timeFormat = player?.preferences?.timeFormat ?: subState.timeFormat,
                    temperatureUnit = player?.preferences?.temperatureUnit
                            ?: subState.temperatureUnit
                )
            }

            is PlanDayAction.ShowNext ->
                subState.copy(
                    type = PlanDayViewState.StateType.NEXT_PAGE,
                    adapterPosition = subState.adapterPosition + 1
                )

            is DataLoadedAction.PlayerChanged -> {
                val player = action.player
                subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    playerName = player.displayName?.let { firstNameOf(it) },
                    petAvatar = player.pet.avatar,
                    timeFormat = player.preferences.timeFormat,
                    temperatureUnit = player.preferences.temperatureUnit
                )
            }

            is PlanDayAction.LoadReviewDay ->
                createNewStateIfReviewDataIsLoaded(subState)

            is DataLoadedAction.ReviewDayQuestsChanged -> {
                val newState = subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    reviewDayQuests = action.quests,
                    awesomenessScore = action.awesomenessScore
                )
                createNewStateIfReviewDataIsLoaded(newState)
            }

            is DataLoadedAction.TodayQuestsChanged -> {
                val newState = subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    todayQuests = action.quests
                )
                createNewStateIfTodayDataIsLoaded(newState)
            }

            is DataLoadedAction.SuggestionsChanged -> {
                val newState = subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    suggestedQuests = action.quests
                )
                createNewStateIfTodayDataIsLoaded(newState)
            }

            is PlanDayAction.LoadToday ->
                createNewStateIfTodayDataIsLoaded(subState)

            is PlanDayAction.AcceptSuggestion -> {
                val suggestion = subState.suggestedQuests!!.first { it.id == action.questId }
                val newState = subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    suggestedQuests = subState.suggestedQuests - suggestion
                )
                createNewStateIfTodayDataIsLoaded(newState)
            }

            is DataLoadedAction.QuoteChanged -> {
                val newState = subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    quote = action.quote,
                    quoteLoaded = true
                )
                createNewStateIfMotivationalDataIsLoaded(newState)
            }

            is DataLoadedAction.WeatherChanged -> {
                val newState = action.weather?.let {

                    val conditions = it.conditions

                    if (conditions.contains(Weather.Condition.UNKNOWN))
                        subState.copy(
                            type = PlanDayViewState.StateType.DATA_CHANGED,
                            weatherLoaded = true
                        )
                    else
                        subState.copy(
                            type = PlanDayViewState.StateType.DATA_CHANGED,
                            weather = action.weather,
                            weatherLoaded = true
                        )
                } ?: subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    weatherLoaded = true
                )
                createNewStateIfMotivationalDataIsLoaded(newState)
            }

            is DataLoadedAction.MotivationalImageChanged -> {
                val newState = action.motivationalImage?.let {
                    subState.copy(
                        type = PlanDayViewState.StateType.IMAGE_LOADED,
                        imageUrl = it.url + "&fm=jpg&w=1080&crop=entropy&fit=max",
                        imageAuthor = it.author,
                        imageAuthorUrl = it.authorUrl,
                        imageLoaded = true
                    )
                } ?: subState.copy(
                    type = PlanDayViewState.StateType.IMAGE_LOADED,
                    imageAuthor = "Daniel Roe",
                    imageAuthorUrl = "https://unsplash.com/@danielroe",
                    imageLoaded = true
                )

                createNewStateIfMotivationalDataIsLoaded(newState)
            }

            is PlanDayAction.ImageLoaded -> {
                val newState = subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    imageViewLoaded = true
                )

                createNewStateIfMotivationalDataIsLoaded(newState)
            }

            is PlanDayAction.RisingSunAnimationDone -> {
                val newState = subState.copy(
                    type = PlanDayViewState.StateType.DATA_CHANGED,
                    risingSunAnimationDone = true
                )

                createNewStateIfMotivationalDataIsLoaded(newState)
            }

            is PlanDayAction.Back ->
                if (subState.adapterPosition == 2)
                    subState.copy(
                        type = PlanDayViewState.StateType.BACK_TO_REVIEW,
                        adapterPosition = 1
                    )
                else
                    subState.copy(
                        type = PlanDayViewState.StateType.CLOSE
                    )

            else -> subState
        }

    private fun firstNameOf(displayName: String) =
        if (displayName.isBlank())
            null
        else
            displayName.split(" ")[0].capitalize()

    private fun createNewStateIfMotivationalDataIsLoaded(newState: PlanDayViewState) =
        if (hasMotivationalDataLoaded(newState))
            newState.copy(
                type = PlanDayViewState.StateType.MOTIVATION_DATA_LOADED,
                timeOfDay = Time.now().timeOfDay
            )
        else
            newState

    private fun hasMotivationalDataLoaded(state: PlanDayViewState) =
        state.weatherLoaded &&
                state.quoteLoaded &&
                state.imageLoaded &&
                state.imageViewLoaded &&
                state.risingSunAnimationDone

    private fun createNewStateIfTodayDataIsLoaded(newState: PlanDayViewState) =
        if (hasTodayDataLoaded(newState))
            newState.copy(
                type = PlanDayViewState.StateType.TODAY_DATA_LOADED
            )
        else
            newState

    private fun hasTodayDataLoaded(state: PlanDayViewState) =
        state.todayQuests != null && state.suggestedQuests != null

    private fun createNewStateIfReviewDataIsLoaded(newState: PlanDayViewState) =
        if (hasReviewDataLoaded(newState))
            newState.copy(
                type = PlanDayViewState.StateType.REVIEW_DATA_LOADED
            )
        else
            newState

    private fun hasReviewDataLoaded(newState: PlanDayViewState) =
        newState.reviewDayQuests != null && newState.awesomenessScore != null && newState.petAvatar != null

    override fun defaultState() =
        PlanDayViewState(
            type = PlanDayViewState.StateType.INITIAL,
            dateText = null,
            imageUrl = null,
            imageAuthor = "",
            imageAuthorUrl = "",
            quote = null,
            reviewDayQuests = null,
            todayQuests = null,
            suggestedQuests = null,
            adapterPosition = 0,
            playerName = null,
            weather = null,
            timeOfDay = null,
            weatherLoaded = false,
            quoteLoaded = false,
            imageLoaded = false,
            imageViewLoaded = false,
            risingSunAnimationDone = false,
            petAvatar = null,
            awesomenessScore = null,
            temperatureUnit = Constants.DEFAULT_TEMPERATURE_UNIT,
            timeFormat = Constants.DEFAULT_TIME_FORMAT
        )

    override val stateKey = key<PlanDayViewState>()

}

data class PlanDayViewState(
    val type: StateType,
    val dateText: String?,
    val imageUrl: String?,
    val imageAuthor: String,
    val imageAuthorUrl: String,
    val weather: Weather?,
    val quote: Quote?,
    val reviewDayQuests: List<Quest>?,
    val todayQuests: List<Quest>?,
    val suggestedQuests: List<Quest>?,
    val adapterPosition: Int,
    val timeOfDay: TimeOfDay?,
    val playerName: String?,
    val weatherLoaded: Boolean,
    val quoteLoaded: Boolean,
    val imageLoaded: Boolean,
    val imageViewLoaded: Boolean,
    val risingSunAnimationDone: Boolean,
    val petAvatar: PetAvatar?,
    val awesomenessScore: CalculateAwesomenessScoreUseCase.AwesomenessScore?,
    val temperatureUnit: Player.Preferences.TemperatureUnit,
    val timeFormat: Player.Preferences.TimeFormat
) : BaseViewState() {
    enum class StateType {
        INITIAL,
        NEXT_PAGE,
        BACK_TO_REVIEW,
        CLOSE,
        DATA_CHANGED,
        IMAGE_LOADED,
        MOTIVATION_DATA_LOADED,
        REVIEW_DATA_LOADED,
        TODAY_DATA_LOADED
    }
}

class PlanDayViewController(args: Bundle? = null) :
    ReduxViewController<PlanDayAction, PlanDayViewState, PlanDayReducer>(args) {
    override val reducer = PlanDayReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        requestPermissions(
            mapOf(Manifest.permission.ACCESS_FINE_LOCATION to "If you want to see the weather, please give myPoli permission for your location"),
            Constants.RC_LOCATION_PERM
        )
        return container.inflate(R.layout.controller_plan_day)
    }

    override fun onCreateLoadAction() = PlanDayAction.Load

    override fun onPermissionsGranted(requestCode: Int, permissions: List<String>) {
        dispatch(PlanDayAction.GetWeather)
    }


    override fun handleBack(): Boolean {
        dispatch(PlanDayAction.Back)
        return true
    }

    override fun render(state: PlanDayViewState, view: View) {
        when (state.type) {
            PlanDayViewState.StateType.INITIAL ->
                changeChildController(
                    view = view,
                    adapterPosition = state.adapterPosition,
                    animate = false
                )

            PlanDayViewState.StateType.NEXT_PAGE ->
                changeChildController(
                    view = view,
                    adapterPosition = state.adapterPosition,
                    animate = true
                )

            PlanDayViewState.StateType.BACK_TO_REVIEW ->
                getChildRouter(view.planDayPager).popCurrentController()

            PlanDayViewState.StateType.CLOSE -> {
                exitFullScreen()
                rootRouter.setRoot(RouterTransaction.with(HomeViewController()))
            }
        }
    }

    private fun changeChildController(
        view: View,
        adapterPosition: Int,
        animate: Boolean = true
    ) {
        val childRouter = getChildRouter(view.planDayPager)

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
            MOTIVATION_INDEX -> PlanDayMotivationViewController()
            REVIEW_YESTERDAY -> PlanDayReviewViewController()
            PLAN_TODAY -> PlanDayTodayViewController()
            else -> throw IllegalArgumentException("Unknown controller position $position")
        }

    companion object {
        const val MOTIVATION_INDEX = 0
        const val REVIEW_YESTERDAY = 1
        const val PLAN_TODAY = 2
    }

}