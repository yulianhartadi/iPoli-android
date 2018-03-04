package mypoli.android.challenge.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import kotlinx.android.synthetic.main.controller_challenge_category_list.view.*
import mypoli.android.R
import mypoli.android.challenge.category.list.ChallengeListForCategoryViewController
import mypoli.android.challenge.data.Challenge
import mypoli.android.challenge.data.Challenge.Category.*
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.rootRouter
import mypoli.android.common.view.stringRes
import mypoli.android.common.view.toolbarTitle
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class ChallengeCategoryListViewController(args: Bundle? = null) :
    MviViewController<ChallengeCategoryListViewState, ChallengeCategoryListViewController, ChallengeCategoryListPresenter, ChallengeCategoryListIntent>(
        args
    ) {

    private val presenter by required { challengeCategoryListPresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        toolbarTitle = stringRes(R.string.drawer_challenges)
        val view = inflater.inflate(R.layout.controller_challenge_category_list, container, false)
        view.healthAndFitness.setOnClickListener { showChallengeList(HEALTH_AND_FITNESS) }
        view.buildSkill.setOnClickListener { showChallengeList(BUILD_SKILL) }
        view.deepWork.setOnClickListener { showChallengeList(DEEP_WORK) }
        view.meTime.setOnClickListener { showChallengeList(ME_TIME) }
        view.organizeLife.setOnClickListener { showChallengeList(ORGANIZE_MY_LIFE) }
        return view
    }

    private fun showChallengeList(category: Challenge.Category) {
        val handler = FadeChangeHandler()
        rootRouter.pushController(
            RouterTransaction
                .with(ChallengeListForCategoryViewController(category))
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }

    override fun render(state: ChallengeCategoryListViewState, view: View) {

    }
}