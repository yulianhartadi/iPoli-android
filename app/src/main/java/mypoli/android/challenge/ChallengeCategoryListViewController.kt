package mypoli.android.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_challenge_category_list.view.*
import mypoli.android.R
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.setToolbar
import mypoli.android.common.view.toolbarTitle
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class ChallengeCategoryListViewController(args: Bundle? = null) :
    MviViewController<ChallengeCategoryListViewState, ChallengeCategoryListViewController, ChallengeCategoryListPresenter, ChallengeCategoryListIntent>(args) {

    private val presenter by required { challengeCategoryListPresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_challenge_category_list, container, false)
        setToolbar(view.toolbar)
        toolbarTitle = "Challenges"
        return view
    }

    override fun render(state: ChallengeCategoryListViewState, view: View) {

    }
}