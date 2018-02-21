package mypoli.android.repeatingquest.show

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_repeating_quest.view.*
import mypoli.android.MainActivity
import mypoli.android.R
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.colorRes
import mypoli.android.common.view.setToolbar
import mypoli.android.common.view.toolbarTitle


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */
class RepeatingQuestViewController :
    ReduxViewController<RepeatingQuestAction, RepeatingQuestViewState, RepeatingQuestReducer> {

    override val reducer = RepeatingQuestReducer

    private lateinit var repeatingQuestId: String

    constructor(args: Bundle? = null) : super(args)

    constructor(
        repeatingQuestId: String
    ) : this() {
        this.repeatingQuestId = repeatingQuestId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.controller_repeating_quest,
            container,
            false
        )
        setToolbar(view.toolbar)
        val collapsingToolbar = view.collapsingToolbarContainer
        collapsingToolbar.isTitleEnabled = false

        view.appbar.addOnOffsetChangedListener({ _, verticalOffset ->
            val showTitleThreshold = 2 * ViewCompat.getMinimumHeight(collapsingToolbar)
            val supportActionBar = (activity as MainActivity).supportActionBar
            if (collapsingToolbar.height + verticalOffset < showTitleThreshold) {
                supportActionBar?.setDisplayShowTitleEnabled(true)
            } else {
                supportActionBar?.setDisplayShowTitleEnabled(false)
            }
        })

        return view
    }

    override fun onCreateLoadAction() =
        RepeatingQuestAction.Load(repeatingQuestId)

    override fun render(state: RepeatingQuestViewState, view: View) {
        when (state) {
            is RepeatingQuestViewState.Changed -> {
                toolbarTitle = state.name
                view.questName.text = state.name
                colorLayout(view, state)
            }
        }
    }

    private fun colorLayout(
        view: View,
        state: RepeatingQuestViewState.Changed
    ) {
        view.appbar.setBackgroundColor(colorRes(state.color500))
        view.toolbar.setBackgroundColor(colorRes(state.color500))
        view.collapsingToolbarContainer.setContentScrimColor(colorRes(state.color500))
        activity?.window?.navigationBarColor = colorRes(state.color500)
        activity?.window?.statusBarColor = colorRes(state.color700)
    }

    private val RepeatingQuestViewState.Changed.color500
        get() = AndroidColor.valueOf(color.name).color500

    private val RepeatingQuestViewState.Changed.color700
        get() = AndroidColor.valueOf(color.name).color700
}