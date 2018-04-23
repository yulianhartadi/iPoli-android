package io.ipoli.android.challenge.add

import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.R
import io.ipoli.android.challenge.add.EditChallengeViewState.StateType.*
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.colorRes
import io.ipoli.android.common.view.setToolbar
import io.ipoli.android.common.view.showBackButton
import io.ipoli.android.common.view.toolbarTitle
import kotlinx.android.synthetic.main.controller_add_challenge.view.*
import kotlinx.android.synthetic.main.view_no_elevation_toolbar.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/8/18.
 */
class AddChallengeViewController(args: Bundle? = null) :
    ReduxViewController<EditChallengeAction, EditChallengeViewState, EditChallengeReducer>(args) {

    override val reducer = EditChallengeReducer

    companion object {
        const val NAME_INDEX = 0
        const val MOTIVATION_INDEX = 1
        const val END_DATE_INDEX = 2
        const val QUEST_PICKER_INDEX = 3
        const val SUMMARY_INDEX = 4
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_add_challenge, container, false)
        setToolbar(view.toolbar)
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onDetach(view: View) {
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onDetach(view)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            dispatch(EditChallengeAction.Back)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleBack(): Boolean {
        dispatch(EditChallengeAction.Back)
        return true
    }

    override fun colorLayoutBars() {

    }

    override fun render(state: EditChallengeViewState, view: View) {
        when (state.type) {

            INITIAL -> {
                toolbarTitle = "New Challenge"
                colorLayout(view, state)
                changeChildController(view, state.adapterPosition)
            }

            CHANGE_PAGE -> {
                if (state.adapterPosition == SUMMARY_INDEX) {
                    dispatch(EditChallengeAction.UpdateSummary)
                }
                changeChildController(view, state.adapterPosition)
                toolbarTitle = state.toolbarTitle
            }

            CLOSE ->
                router.popCurrentController()

            COLOR_CHANGED ->
                colorLayout(view, state)
        }
    }

    private fun changeChildController(
        view: View,
        adapterPosition: Int
    ) {
        val childRouter = getChildRouter(view.pager)
        childRouter.setRoot(
            RouterTransaction.with(
                createControllerForPosition(adapterPosition)
            )
        )
    }

    private fun createControllerForPosition(position: Int): Controller =
        when (position) {
            NAME_INDEX -> AddChallengeNameViewController()
            MOTIVATION_INDEX -> AddChallengeMotivationViewController()
            END_DATE_INDEX -> AddChallengeEndDateViewController()
            QUEST_PICKER_INDEX -> AddChallengeQuestsViewController()
            SUMMARY_INDEX -> AddChallengeSummaryViewController()
            else -> throw IllegalArgumentException("Unknown controller position $position")
        }

    private fun colorLayout(
        view: View,
        state: EditChallengeViewState
    ) {
        val color500 = colorRes(state.color.androidColor.color500)
        val color700 = colorRes(state.color.androidColor.color700)
        view.appbar.setBackgroundColor(color500)
        view.toolbar.setBackgroundColor(color500)
        view.rootContainer.setBackgroundColor(color500)
        activity?.window?.navigationBarColor = color500
        activity?.window?.statusBarColor = color700
    }

    private val EditChallengeViewState.toolbarTitle: String
        get() = when (adapterPosition) {
            NAME_INDEX -> "New Challenge"
            MOTIVATION_INDEX -> "Thoughts to motivate you later"
            END_DATE_INDEX -> "Achieve it in"
            QUEST_PICKER_INDEX -> "Add some quests"
            SUMMARY_INDEX -> "Summary"
            else -> throw IllegalArgumentException("No controller for position $adapterPosition")
        }
}