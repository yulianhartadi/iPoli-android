package io.ipoli.android.repeatingquest.list.ui

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.repeatingquest.data.RepeatingQuest
import io.ipoli.android.repeatingquest.list.RepeatingQuestListPresenter
import io.ipoli.android.repeatingquest.list.di.DaggerRepeatingQuestListComponent
import io.ipoli.android.repeatingquest.list.di.RepeatingQuestListComponent
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/22/17.
 */
class RepeatingQuestListController : BaseController<RepeatingQuestListController, RepeatingQuestListPresenter>() {

    private var restoringState: Boolean = false

    lateinit private var questList: RecyclerView

    val controllerComponent: RepeatingQuestListComponent by lazy {
        val component = DaggerRepeatingQuestListComponent.builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@RepeatingQuestListController)
        component
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        controllerComponent
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_repeating_quest_list, container, false) as ViewGroup

        return view
    }

    override fun createPresenter(): RepeatingQuestListPresenter = controllerComponent.createPresenter()

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    fun loadRepeatingQuestsIntent(): Observable<Boolean> =
        Observable.just(!restoringState).filter { _ -> true }

    fun render(state: RepeatingQuestListViewState) {

    }
}

data class RepeatingQuestViewModel(val name: String) {
    companion object {
        fun create(repeatingQuest: RepeatingQuest): RepeatingQuestViewModel =
            RepeatingQuestViewModel(repeatingQuest.name!!)
    }
}

interface RepeatingQuestListViewState {

    class Loading : RepeatingQuestListViewState

    data class Error(val error: Throwable) : RepeatingQuestListViewState

    data class DataLoaded(val repeatingQuests: List<RepeatingQuestViewModel>) : RepeatingQuestListViewState
}