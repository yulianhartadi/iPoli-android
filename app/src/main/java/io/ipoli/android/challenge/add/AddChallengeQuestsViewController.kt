package io.ipoli.android.challenge.add

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import io.ipoli.android.R
import io.ipoli.android.challenge.*
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.invisible
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.controller_add_challenge_quests.view.*
import kotlinx.android.synthetic.main.list_quest_picker.view.*
import kotlinx.android.synthetic.main.view_empty_list.view.*
import kotlinx.android.synthetic.main.view_loader.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/10/18.
 */
class AddChallengeQuestsViewController(args: Bundle? = null) :
    ReduxViewController<QuestPickerAction, QuestPickerViewState, QuestPickerReducer>(args) {
    override val reducer = QuestPickerReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_add_challenge_quests, container, false)
        view.questList.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        view.questList.adapter = QuestAdapter(listOf(), { id, isChecked ->
            dispatch(QuestPickerAction.Check(id, isChecked))
        })
        view.challengeNext.dispatchOnClick(QuestPickerAction.Next)
        return view
    }

    override fun onCreateLoadAction() = QuestPickerAction.Load()

    override fun colorLayoutBars() {}

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.challenge_quest_picker_menu, menu)
        val searchItem = menu.findItem(R.id.actionSearch)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(newText: String): Boolean {
                dispatch(QuestPickerAction.Filter(newText))
                return true
            }
        })
    }

    override fun render(state: QuestPickerViewState, view: View) {
        when (state.type) {
            QuestPickerViewState.StateType.LOADING -> {
                view.loader.invisible()
                view.emptyContainer.invisible()
                view.questList.invisible()
            }

            QuestPickerViewState.StateType.DATA_CHANGED -> {
                view.questList.visible()
                view.loader.invisible()
                view.emptyContainer.invisible()
                (view.questList.adapter as QuestAdapter).updateAll(state.toViewModels())
            }

            QuestPickerViewState.StateType.EMPTY -> {
                view.emptyContainer.visible()
                view.loader.invisible()
                view.questList.invisible()

                view.emptyImage.setImageResource(R.drawable.quest_picker_empty_state)
                view.emptyTitle.setText(R.string.empty_state_quest_picker_title)
                view.emptyText.setText(R.string.empty_state_quest_picker_text)
            }
        }
    }
}