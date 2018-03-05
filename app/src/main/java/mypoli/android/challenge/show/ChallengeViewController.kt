package mypoli.android.challenge.show

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_challenge.view.*
import mypoli.android.R
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.setToolbar

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

        return view
    }

    override fun render(state: ChallengeViewState, view: View) {

    }

}