package io.ipoli.android.challenge.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.invisible
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.common.view.toolbarTitle
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.controller_challenge_list.view.*
import kotlinx.android.synthetic.main.view_empty_list.view.*
import kotlinx.android.synthetic.main.view_loader.view.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */
class ChallengeListViewController(args: Bundle? = null) :
    ReduxViewController<ChallengeListAction, ChallengeListViewState, ChallengeListReducer>(
        args
    ) {

    override val reducer = ChallengeListReducer

    override var helpConfig: HelpConfig? =
        HelpConfig(
            R.string.help_dialog_challenge_list_title,
            R.string.help_dialog_challenge_list_message
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(
            R.layout.controller_challenge_list, container, false
        )
        view.challengeList.layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.challengeList.adapter = ChallengeAdapter()

        view.addChallenge.dispatchOnClick { ChallengeListAction.AddChallenge }
        view.emptyAnimation.setAnimation("empty_challenge_list.json")
        return view
    }

    override fun onCreateLoadAction() = ChallengeListAction.Load

    override fun onAttach(view: View) {
        super.onAttach(view)
        toolbarTitle = stringRes(R.string.drawer_challenges)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.challenge_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.actionPredefinedChallenges) {
            navigateFromRoot().toPresetChallengeCategory()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: ChallengeListViewState, view: View) {
        when (state.type) {

            ChallengeListViewState.StateType.LOADING -> {
                view.loader.visible()
                view.emptyContainer.invisible()
                view.challengeList.invisible()
            }

            ChallengeListViewState.StateType.DATA_CHANGED -> {
                view.challengeList.visible()
                view.loader.invisible()
                view.emptyContainer.invisible()
                view.emptyAnimation.pauseAnimation()

                (view.challengeList.adapter as ChallengeAdapter).updateAll(
                    toChallengeViewModels(
                        view.context,
                        state.challenges,
                        shouldUse24HourFormat,
                        navigateFromRoot()
                    )
                )
            }

            ChallengeListViewState.StateType.EMPTY -> {
                view.emptyContainer.visible()
                view.loader.invisible()
                view.challengeList.invisible()
                view.emptyAnimation.playAnimation()
                view.emptyTitle.setText(R.string.empty_challenges_title)
                view.emptyText.setText(R.string.empty_challenges_text)
            }

            ChallengeListViewState.StateType.SHOW_ADD -> {
                navigateFromRoot().toAddChallenge()
            }
        }
    }
}