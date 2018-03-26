package io.ipoli.android.challenge.predefined.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.R
import io.ipoli.android.challenge.predefined.category.list.ChallengeListForCategoryViewController
import io.ipoli.android.challenge.predefined.entity.PredefinedChallengeData
import io.ipoli.android.challenge.predefined.entity.PredefinedChallengeData.Category.*
import io.ipoli.android.common.view.*
import kotlinx.android.synthetic.main.controller_challenge_category_list.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class ChallengeCategoryListViewController(args: Bundle? = null) :
    RestoreViewOnCreateController(args) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_challenge_category_list, container, false)
        setToolbar(view.toolbar)
        toolbarTitle = stringRes(R.string.predefined_challenges_title)

        view.healthAndFitness.setOnClickListener { showChallengeList(HEALTH_AND_FITNESS) }
        view.buildSkill.setOnClickListener { showChallengeList(BUILD_SKILL) }
        view.deepWork.setOnClickListener { showChallengeList(DEEP_WORK) }
        view.meTime.setOnClickListener { showChallengeList(ME_TIME) }
        view.organizeLife.setOnClickListener { showChallengeList(ORGANIZE_MY_LIFE) }
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            router.handleBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showChallengeList(category: PredefinedChallengeData.Category) {
        val handler = FadeChangeHandler()
        rootRouter.pushController(
            RouterTransaction
                .with(ChallengeListForCategoryViewController(category))
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }
}