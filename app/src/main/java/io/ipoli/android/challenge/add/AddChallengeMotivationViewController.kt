package io.ipoli.android.challenge.add

import android.os.Bundle
import android.view.*
import io.ipoli.android.R
import io.ipoli.android.challenge.add.EditChallengeViewState.StateType.VALIDATION_ERROR_EMPTY_MOTIVATION
import io.ipoli.android.challenge.add.EditChallengeViewState.StateType.VALIDATION_MOTIVATION_SUCCESSFUL
import io.ipoli.android.common.redux.android.BaseViewController
import io.ipoli.android.common.view.stringRes
import kotlinx.android.synthetic.main.controller_add_challenge_motivation.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/9/18.
 */

class AddChallengeMotivationViewController(args: Bundle? = null) :
    BaseViewController<EditChallengeAction, EditChallengeViewState>(
        args
    ) {
    override val stateKey = EditChallengeReducer.stateKey

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.controller_add_challenge_motivation, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.next_wizard_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.actionNext -> {
                dispatch(
                    EditChallengeAction.ValidateMotivation(
                        listOf(
                            view!!.motivation1.text.toString(),
                            view!!.motivation2.text.toString(),
                            view!!.motivation3.text.toString()
                        )
                    )
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun render(state: EditChallengeViewState, view: View) {
        view.motivation1.setText(state.motivation1)
        view.motivation2.setText(state.motivation2)
        view.motivation3.setText(state.motivation3)

        when (state.type) {

            VALIDATION_ERROR_EMPTY_MOTIVATION -> {
                view.motivation1.error = stringRes(R.string.no_motivation_error)
            }

            VALIDATION_MOTIVATION_SUCCESSFUL -> {
                dispatch(EditChallengeAction.ShowNext)
            }

            else -> {
            }
        }

    }

    override fun colorLayoutBars() {}
}