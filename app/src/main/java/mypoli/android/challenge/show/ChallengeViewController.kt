package mypoli.android.challenge.show

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_challenge.view.*
import mypoli.android.MainActivity
import mypoli.android.R
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.colorRes
import mypoli.android.common.view.setToolbar
import mypoli.android.common.view.showBackButton
import mypoli.android.common.view.toolbarTitle
import mypoli.android.repeatingquest.show.RepeatingQuestViewController

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */
class ChallengeViewController(args: Bundle? = null) :
    ReduxViewController<ChallengeAction, ChallengeViewState, ChallengeReducer>(args) {

    override val reducer = ChallengeReducer

    private lateinit var challengeId: String

    constructor(
        challengeId: String
    ) : this() {
        this.challengeId = challengeId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(
            R.layout.controller_challenge,
            container,
            false
        )
        setToolbar(view.toolbar)
        val collapsingToolbar = view.collapsingToolbarContainer
        collapsingToolbar.isTitleEnabled = false

        view.appbar.addOnOffsetChangedListener(object :
            RepeatingQuestViewController.AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {

                appBarLayout.post {
                    if (state == State.EXPANDED) {
                        val supportActionBar = (activity as MainActivity).supportActionBar
                        supportActionBar?.setDisplayShowTitleEnabled(false)
                    } else if (state == State.COLLAPSED) {
                        val supportActionBar = (activity as MainActivity).supportActionBar
                        supportActionBar?.setDisplayShowTitleEnabled(true)
                    }
                }

            }
        })

        return view
    }

    override fun onCreateLoadAction() = ChallengeAction.Load(challengeId)

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    override fun onDetach(view: View) {
        (activity as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        super.onDetach(view)
    }

    override fun render(state: ChallengeViewState, view: View) =
        when (state) {
            is ChallengeViewState.Changed -> {
                colorLayout(state, view)

                renderName(state.name, view)

                view.progress.progress = state.completedCount
                view.progress.secondaryProgress = state.completedCount
                view.progress.max = state.totalCount

                view.progressText.text = state.progressText
            }
            else -> {
            }
        }

    private fun colorLayout(
        state: ChallengeViewState.Changed,
        view: View
    ) {
        view.appbar.setBackgroundColor(colorRes(state.color500))
        view.toolbar.setBackgroundColor(colorRes(state.color500))
        view.collapsingToolbarContainer.setContentScrimColor(colorRes(state.color500))
        activity?.window?.navigationBarColor = colorRes(state.color500)
        activity?.window?.statusBarColor = colorRes(state.color700)
    }

    private fun renderName(
        name: String,
        view: View
    ) {
        toolbarTitle = name
        view.name.text = name
    }

    private val ChallengeViewState.Changed.color500
        get() = color.androidColor.color500

    private val ChallengeViewState.Changed.color700
        get() = color.androidColor.color700

    private val ChallengeViewState.Changed.progressText
        get() = "$completedCount of $totalCount ($progressPercent %) done"

}