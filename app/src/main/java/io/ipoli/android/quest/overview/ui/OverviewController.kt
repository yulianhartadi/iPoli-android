package io.ipoli.android.quest.overview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.overview.DisplayOverviewQuestsUseCase
import io.ipoli.android.quest.overview.OverviewPresenter
import io.ipoli.android.quest.overview.di.DaggerOverviewComponent
import io.ipoli.android.quest.overview.di.OverviewComponent
import io.reactivex.Observable
import org.threeten.bp.LocalDate
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/17.
 */
class OverviewController : BaseController<OverviewController, OverviewPresenter>() {

    private var restoringState: Boolean = false

    val controllerComponent: OverviewComponent by lazy {
        val component = DaggerOverviewComponent.builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@OverviewController)
        component
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        controllerComponent
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_overview, container, false)
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    override fun createPresenter(): OverviewPresenter = controllerComponent.createPresenter()

    fun loadQuestsIntent(): Observable<DisplayOverviewQuestsUseCase.Parameters> =
        Observable.just(!restoringState).filter { _ -> true }.
            map {
                DisplayOverviewQuestsUseCase.Parameters(
                    LocalDate.now(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    1
                )
            }

    fun render(state: OverviewViewState) {
        when (state) {
            is OverviewQuestsLoadedState ->
                Timber.d("Loaded " + state.todayQuests.size)
        }
    }

}

interface OverviewStatePartialChange {
    fun computeNewState(prevState: OverviewViewState): OverviewViewState
}

class QuestsLoadedLoadedPartialChange(private val todayQuests: List<OverviewQuestViewModel>,
                                      private val tomorrowQuests: List<OverviewQuestViewModel>,
                                      private val upcomingQuests: List<OverviewQuestViewModel>,
                                      private val completedQuests: List<OverviewQuestViewModel>) : OverviewStatePartialChange {
    override fun computeNewState(prevState: OverviewViewState) =
        OverviewViewState(todayQuests, tomorrowQuests, upcomingQuests, completedQuests)
}

open class OverviewViewState(
    val todayQuests: List<OverviewQuestViewModel> = listOf(),
    val tomorrowQuests: List<OverviewQuestViewModel> = listOf(),
    val upcomingQuests: List<OverviewQuestViewModel> = listOf(),
    val completedQuests: List<OverviewQuestViewModel> = listOf()
)

class OverviewInitialLoadingState : OverviewViewState()

class OverviewQuestsLoadedState(todayQuests: List<OverviewQuestViewModel>,
                                tomorrowQuests: List<OverviewQuestViewModel>,
                                upcomingQuests: List<OverviewQuestViewModel>,
                                completedQuests: List<OverviewQuestViewModel>) :
    OverviewViewState(todayQuests, tomorrowQuests, upcomingQuests, completedQuests)

data class OverviewQuestViewModel(
    val name: String,
    val isRecurrent: Boolean,
    val isFromChallenge: Boolean,
    val isStarted: Boolean
) {
    companion object {
        fun fromQuest(quest: Quest): OverviewQuestViewModel =
            OverviewQuestViewModel(quest.name, quest.isFromRepeatingQuest, quest.isFromChallenge, quest.isStarted)
    }
}
