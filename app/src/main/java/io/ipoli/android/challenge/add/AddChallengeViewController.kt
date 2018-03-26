package io.ipoli.android.challenge.add

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import kotlinx.android.synthetic.main.controller_add_challenge.view.*
import kotlinx.android.synthetic.main.view_no_elevation_toolbar.view.*
import io.ipoli.android.R
import io.ipoli.android.challenge.add.AddChallengeViewState.StateType.*
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.colorRes
import io.ipoli.android.common.view.setToolbar
import io.ipoli.android.common.view.showBackButton
import io.ipoli.android.common.view.toolbarTitle

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/8/18.
 */
class AddChallengeViewController(args: Bundle? = null) :
    ReduxViewController<AddChallengeAction, AddChallengeViewState, AddChallengeReducer>(args) {

    override val reducer = AddChallengeReducer

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
        view.pager.isLocked = true
        view.pager.adapter = AddChallengePagerAdapter(this)
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.actionSearch)?.isVisible = view!!.pager.currentItem == QUEST_PICKER_INDEX
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            dispatch(AddChallengeAction.Back)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleBack(): Boolean {
        dispatch(AddChallengeAction.Back)
        return true
    }

    override fun render(state: AddChallengeViewState, view: View) {
        when(state.type) {
            INITIAL -> {
                toolbarTitle = "New Challenge"
                colorLayout(view, state)
            }
            CHANGE_PAGE -> {
                if(state.adapterPosition == SUMMARY_INDEX) {
                    dispatch(AddChallengeAction.UpdateSummary)
                }
                view.pager.currentItem = state.adapterPosition
                activity!!.invalidateOptionsMenu()
                toolbarTitle = state.toolbarTitle
            }

            CLOSE -> {
                router.popController(this)
            }

            COLOR_CHANGED -> {
                colorLayout(view, state)
            }
        }
    }

    private fun colorLayout(
        view: View,
        state: AddChallengeViewState
    ) {
        val color500 = colorRes(state.color.androidColor.color500)
        val color700 = colorRes(state.color.androidColor.color700)
        view.appbar.setBackgroundColor(color500)
        view.toolbar.setBackgroundColor(color500)
        view.rootContainer.setBackgroundColor(color500)
        activity?.window?.navigationBarColor = color500
        activity?.window?.statusBarColor = color700
    }

    class AddChallengePagerAdapter(
        controller: Controller
    ) :
        RouterPagerAdapter(controller) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                when (position) {
                    NAME_INDEX -> router.setRoot(RouterTransaction.with(AddChallengeNameViewController()))
                    MOTIVATION_INDEX -> router.setRoot(RouterTransaction.with(AddChallengeMotivationViewController()))
                    END_DATE_INDEX -> router.setRoot(RouterTransaction.with(AddChallengeEndDateViewController()))
                    QUEST_PICKER_INDEX -> router.setRoot(RouterTransaction.with(AddChallengeQuestsViewController()))
                    SUMMARY_INDEX -> router.setRoot(RouterTransaction.with(AddChallengeSummaryViewController()))
                }
            }
        }

        override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

        override fun getCount() = 5
    }

    private val AddChallengeViewState.toolbarTitle: String
        get() = when (adapterPosition) {
            NAME_INDEX -> "New Challenge"
            MOTIVATION_INDEX -> "Thoughts to motivate you later"
            END_DATE_INDEX -> "Achieve it in"
            QUEST_PICKER_INDEX -> "Add some quests"
            SUMMARY_INDEX -> "Summary"
            else -> throw IllegalArgumentException("No controller for position $adapterPosition")
        }
}